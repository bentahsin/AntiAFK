package com.bentahsin.antiafk.managers;

import com.bentahsin.antiafk.AntiAFKPlugin;
import com.bentahsin.antiafk.models.PunishmentLevel;
import com.bentahsin.antiafk.models.RegionOverride;
import com.bentahsin.antiafk.storage.DatabaseManager;
import com.bentahsin.antiafk.utils.DiscordWebhookUtil;
import com.bentahsin.antiafk.utils.PlaceholderUtil;
import com.bentahsin.antiafk.utils.TimeUtil;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.google.inject.Singleton;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * Oyunculara verilecek cezaları (artan, bölgesel) hesaplayan,
 * eylemleri yürüten ve yeniden giriş korumasını yöneten yönetici.
 */
@Singleton
public class PunishmentManager {

    private final AntiAFKPlugin plugin;
    private final ConfigManager configManager;
    private final DatabaseManager databaseManager;
    private final PlayerStateManager stateManager;
    private final PlayerLanguageManager plLang;
    private final DebugManager debugMgr;
    private final DiscordWebhookUtil discordWebhookUtil;

    private final Cache<UUID, Long> rejoinProtectedPlayers;

    public PunishmentManager(AntiAFKPlugin plugin, ConfigManager configManager, DatabaseManager databaseManager,
                             PlayerStateManager stateManager, PlayerLanguageManager plLang, DebugManager debugMgr,
                             DiscordWebhookUtil discordWebhookUtil) {
        this.plugin = plugin;
        this.configManager = configManager;
        this.databaseManager = databaseManager;
        this.stateManager = stateManager;
        this.plLang = plLang;
        this.debugMgr = debugMgr;
        this.discordWebhookUtil = discordWebhookUtil;

        this.rejoinProtectedPlayers = Caffeine.newBuilder()
                .expireAfterWrite(Math.max(10, configManager.getRejoinCooldownSeconds() * 2), TimeUnit.SECONDS)
                .build();
    }

    /**
     * Bir oyuncuya, ceza geçmişine ve bulunduğu bölgeye göre uygun cezayı uygular.
     * Bu metod, AFKManager'daki checkPlayer döngüsünden çağrılmalıdır.
     *
     * @param player           Cezalandırılacak oyuncu.
     * @param regionOverride   Oyuncunun bulunduğu bölge (null olabilir).
     */
    public void applyPunishment(Player player, RegionOverride regionOverride) {
        List<Map<String, String>> actionsToExecute;
        UUID playerUUID = player.getUniqueId();

        if (configManager.isProgressivePunishmentEnabled()) {
            int currentPunishmentCount = databaseManager.getPunishmentCount(playerUUID);
            int nextPunishmentCount = currentPunishmentCount + 1;

            int highestPunishmentCount = configManager.getHighestPunishmentCount();
            if (highestPunishmentCount > 0 && currentPunishmentCount >= highestPunishmentCount) {
                actionsToExecute = configManager.getPunishmentLevels().stream()
                        .filter(level -> level.getCount() == 1)
                        .findFirst()
                        .map(PunishmentLevel::getActions)
                        .orElse(configManager.getActions());

                databaseManager.resetPunishmentCount(playerUUID);

            } else {
                List<PunishmentLevel> levels = configManager.getPunishmentLevels();
                actionsToExecute = configManager.getActions();

                for (PunishmentLevel level : levels) {
                    if (nextPunishmentCount >= level.getCount()) {
                        actionsToExecute = level.getActions();
                        break;
                    }
                }

                databaseManager.incrementPunishmentCount(playerUUID);
            }

        } else if (regionOverride != null) {
            actionsToExecute = regionOverride.getActions();
        } else {
            actionsToExecute = configManager.getActions();
        }

        executeActions(player, actionsToExecute, stateManager);

        stateManager.setSystemPunished(player, true);

        if (configManager.isRejoinProtectionEnabled()) {
            rejoinProtectedPlayers.put(playerUUID, System.currentTimeMillis());
        }
    }

    /**
     * Sunucuya katılan bir oyuncunun yeniden giriş koruması altında olup olmadığını kontrol eder.
     * Eğer koruma altındaysa ve süre dolmamışsa, oyuncuyu tekrar cezalandırır.
     * PlayerJoinEvent tarafından çağrılmalıdır.
     *
     * @param player Sunucuya katılan oyuncu.
     */
    public void checkRejoin(Player player) {
        if (!configManager.isRejoinProtectionEnabled()) {
            debugMgr.log(DebugManager.DebugModule.ACTIVITY_LISTENER, "Rejoin check for %s skipped: feature disabled.", player.getName());
            return;
        }

        UUID uuid = player.getUniqueId();
        Long punishedTime = rejoinProtectedPlayers.getIfPresent(uuid);

        debugMgr.log(DebugManager.DebugModule.ACTIVITY_LISTENER, "Running rejoin check for %s. Punished time found: %s", player.getName(), (punishedTime != null));

        if (punishedTime != null) {
            long currentTime = System.currentTimeMillis();
            long cooldownMillis = configManager.getRejoinCooldownSeconds() * 1000;

            if ((currentTime - punishedTime) < cooldownMillis) {
                long timeLeftSeconds = (cooldownMillis - (currentTime - punishedTime)) / 1000;

                debugMgr.log(DebugManager.DebugModule.ACTIVITY_LISTENER, "Rejoin protection is active for %s. Time left: %d seconds. Applying punishment.", player.getName(), timeLeftSeconds);

                String message = plLang.getMessage("rejoin_kick", "%time_left%", TimeUtil.formatTime(timeLeftSeconds));

                Bukkit.getScheduler().runTaskLater(plugin, () -> {
                    if (player.isOnline()) {
                        player.kickPlayer(message);
                    }
                }, 2L);

            } else {
                debugMgr.log(DebugManager.DebugModule.ACTIVITY_LISTENER, "Rejoin protection for %s has expired. Invalidating.", player.getName());
                rejoinProtectedPlayers.invalidate(uuid);
            }
        }
    }

    /**
     * Bir oyuncu için belirlenen eylem listesini (komutlar, discord mesajları vb.) yürütür.
     *
     * @param player  Eylemlerin uygulanacağı oyuncu.
     * @param actions Yürütülecek eylemlerin listesi.
     */
    public void executeActions(Player player, List<Map<String, String>> actions, PlayerStateManager stateManager) {
        long maxAfkTimeForAction;
        RegionOverride override = configManager.getRegionOverrideForPlayer(player);
        if (override != null && override.getMaxAfkTime() > 0) {
            maxAfkTimeForAction = override.getMaxAfkTime();
        } else {
            maxAfkTimeForAction = configManager.getMaxAfkTimeSeconds();
        }

        for (Map<String, String> action : actions) {
            String type = action.get("type");

            if ("DISCORD_WEBHOOK".equalsIgnoreCase(type)) {
                String message = action.get("message");
                if (message == null || message.isEmpty()) {
                    message = action.get("command");
                }
                message = PlaceholderUtil.applyPlaceholders(
                        plugin, stateManager, player, message, 0, maxAfkTimeForAction
                );
                discordWebhookUtil.sendMessage(message);
                continue;
            }

            String command = PlaceholderUtil.applyPlaceholders(
                    plugin, stateManager, player, action.get("command"), 0, maxAfkTimeForAction
            );

            if ("PLAYER".equalsIgnoreCase(type)) {
                Bukkit.getScheduler().runTask(plugin, () -> player.performCommand(command));
            } else if ("CONSOLE".equalsIgnoreCase(type)) {
                String cleanCommand = ChatColor.stripColor(ChatColor.translateAlternateColorCodes('&', command));
                Bukkit.getScheduler().runTask(plugin, () -> Bukkit.dispatchCommand(Bukkit.getConsoleSender(), cleanCommand));
            }
        }
    }
}