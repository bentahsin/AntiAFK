package com.bentahsin.antiafk.managers;

import com.bentahsin.antiafk.AntiAFKPlugin;
import com.bentahsin.antiafk.language.Lang;
import com.bentahsin.antiafk.language.SystemLanguageManager;
import com.bentahsin.antiafk.models.PlayerState;
import com.bentahsin.antiafk.models.PunishmentLevel;
import com.bentahsin.antiafk.models.RegionOverride;
import com.bentahsin.antiafk.storage.DatabaseManager;
import com.bentahsin.antiafk.utils.DiscordWebhookUtil;
import com.bentahsin.antiafk.utils.TimeUtil;
import com.bentahsin.antiafk.data.PointlessActivityData;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import me.clip.placeholderapi.PlaceholderAPI;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public class AFKManager {
    private final AntiAFKPlugin plugin;
    private final Logger logger;
    private final SystemLanguageManager sysLang;
    private final ConfigManager configManager;
    private final DebugManager debugMgr;
    private final PlayerLanguageManager plLang;
    private final DatabaseManager databaseManager;

    private final Cache<UUID, Long> lastActivity;
    private final Cache<UUID, Long> lastWarningTime;
    private final Cache<UUID, Long> rejoinProtectedPlayers;
    private final Cache<UUID, PointlessActivityData> botDetectionData;
    private final Cache<UUID, PlayerState> playerStates;

    private final Map<UUID, Long> afkStartTime = new ConcurrentHashMap<>();

    public AFKManager(AntiAFKPlugin plugin) {
        this.plugin = plugin;
        this.logger = plugin.getLogger();
        this.sysLang = plugin.getSystemLanguageManager();
        this.configManager = plugin.getConfigManager();
        this.debugMgr = plugin.getDebugManager();
        this.plLang = plugin.getPlayerLanguageManager();
        this.databaseManager = plugin.getDatabaseManager();

        this.lastActivity = buildCache(1, TimeUnit.HOURS);
        this.lastWarningTime = buildCache(30, TimeUnit.MINUTES);
        this.botDetectionData = buildCache(1, TimeUnit.HOURS);

        this.rejoinProtectedPlayers = Caffeine.newBuilder()
                .expireAfterWrite(Math.max(10, configManager.getRejoinCooldownSeconds() * 2), TimeUnit.SECONDS)
                .build();
        this.playerStates = Caffeine.newBuilder()
                .expireAfterAccess(1, TimeUnit.HOURS)
                .build();

    }

    private <K, V> Cache<K, V> buildCache(long duration, TimeUnit unit) {
        return Caffeine.newBuilder().expireAfterAccess(duration, unit).build();
    }

    private PlayerState getState(Player player) {
        return playerStates.get(
                player.getUniqueId(),
                uuid -> new PlayerState(uuid, player.getDisplayName())
        );
    }

    public void updateActivity(Player player) {
        lastActivity.put(player.getUniqueId(), System.currentTimeMillis());
        lastWarningTime.invalidate(player.getUniqueId());
    }

    public void addPlayer(Player player) {
        updateActivity(player);
        getState(player);
    }

    public void removePlayer(Player player) {
        if (isEffectivelyAfk(player)) {
            saveTotalAfkTime(player);
        }
        UUID uuid = player.getUniqueId();
        lastActivity.invalidate(uuid);
        lastWarningTime.invalidate(uuid);
        botDetectionData.invalidate(uuid);
        playerStates.invalidate(uuid);
    }

    /**
     * Bir oyuncuyu AFK olarak işaretlemenin temel teknik işlemlerini yapar:
     * oyuncuya standart AFK mesajını gönderir ve AFK süresi sayacını başlatır.
     * Bu metot, AFK sebebini belirlemekle İLGİLENMEZ.
     * @param state Değiştirilecek oyuncu durumu.
     */
    private void setPlayerAfk(PlayerState state) {
        Player player = Bukkit.getPlayer(state.getUuid());
        if (player != null) {
            plLang.sendMessage(player, "command.afk.now_afk");
        }

        afkStartTime.put(state.getUuid(), System.currentTimeMillis());
    }

    public long getAfkTime(Player player) {
        Long last = lastActivity.getIfPresent(player.getUniqueId());
        if (last == null) {
            updateActivity(player);
            return 0;
        }
        return (System.currentTimeMillis() - last) / 1000;
    }

    /**
     * Oyuncunun manuel veya otomatik olarak AFK durumunda olup olmadığını kontrol eder.
     */
    public boolean isEffectivelyAfk(Player player) {
        return getState(player).isEffectivelyAfk();
    }

    /**
     * Sadece oyuncunun kendi /afk komutuyla mı AFK olduğunu kontrol eder.
     */
    public boolean isManuallyAFK(Player player) {
        return getState(player).isManualAfk();
    }

    public String getAfkReason(Player player) {
        return getState(player).getAfkReason();
    }

    public void checkPlayer(Player player) {
        if (player.hasPermission(configManager.getPermBypassAll())) return;
        if (player.hasPermission(configManager.getPermBypassClassic())) return;
        if (configManager.getDisabledWorlds().contains(player.getWorld().getName())) return;
        if (configManager.getExemptGameModes().contains(player.getGameMode().name())) return;

        RegionOverride override = configManager.getRegionOverrideForPlayer(player);
        long effectiveMaxAfkTime;

        if (override != null) {
            if (override.getMaxAfkTime() < 0) return;
            effectiveMaxAfkTime = override.getMaxAfkTime();
        } else {
            effectiveMaxAfkTime = configManager.getMaxAfkTimeSeconds();
        }

        long afkSeconds = getAfkTime(player);

        if (afkSeconds >= effectiveMaxAfkTime) {
            if (!isEffectivelyAfk(player)) {
                applyPunishment(player, override);
            }
            return;
        }

        long autoAfkThreshold = configManager.getAutoSetAfkSeconds();
        if (autoAfkThreshold > 0 && afkSeconds >= autoAfkThreshold) {
            if (!isEffectivelyAfk(player)) {
                setAutoAfkStatus(player);
            }
        }

        if (!isEffectivelyAfk(player)) {
            checkAndSendWarning(player, afkSeconds, effectiveMaxAfkTime);
        }
    }

    /**
     * Bir oyuncuya, ceza geçmişine ve bulunduğu bölgeye göre uygun cezayı uygular.
     * @param player Cezalandırılacak oyuncu.
     * @param regionOverride Oyuncunun bulunduğu bölge (null olabilir).
     */
    private void applyPunishment(Player player, RegionOverride regionOverride) {
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

        executeActions(player, actionsToExecute);

        getState(player).setSystemPunished(true);
        if (configManager.isRejoinProtectionEnabled()) {
            rejoinProtectedPlayers.put(playerUUID, System.currentTimeMillis());
        }
    }

    /**
     * Sunucuya katılan bir oyuncunun yeniden giriş koruması altında olup olmadığını kontrol eder.
     * Eğer koruma altındaysa ve süre dolmamışsa, oyuncuyu tekrar cezalandırır.
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

    private void checkAndSendWarning(Player player, long afkSeconds, long maxAfkTime) {
        for (Map<String, Object> warning : configManager.getWarnings()) {
            long warningTime = (long) warning.get("time");
            long timeLeft = maxAfkTime - afkSeconds;

            if (timeLeft <= warningTime) {
                Long lastWarned = lastWarningTime.getIfPresent(player.getUniqueId());
                if (lastWarned != null && lastWarned > warningTime) {
                    continue;
                }
                sendWarning(player, warning, timeLeft, maxAfkTime);
                lastWarningTime.put(player.getUniqueId(), warningTime);
                break;
            }
        }
    }

    private void sendWarning(Player player, Map<String, Object> warning, long timeLeft, long maxAfkTime) {
        String type = (String) warning.get("type");
        String message = ChatColor.translateAlternateColorCodes('&', applyPlaceholders(player, (String) warning.get("message"), timeLeft, maxAfkTime));
        String subtitle = ChatColor.translateAlternateColorCodes('&', applyPlaceholders(player, (String) warning.get("subtitle"), timeLeft, maxAfkTime));

        switch (type.toUpperCase()) {
            case "TITLE":
                player.sendTitle(plLang.getPrefix() + message, plLang.getPrefix() + subtitle, 10, 70, 20);
                break;
            case "ACTION_BAR":
                player.spigot().sendMessage(ChatMessageType.ACTION_BAR, new TextComponent(plLang.getPrefix() + message));
                break;
            case "CHAT":
            default:
                player.sendMessage(plLang.getPrefix() + message);
                break;
        }

        String soundName = (String) warning.get("sound");
        if (soundName != null && !soundName.isEmpty()) {
            try {
                player.playSound(player.getLocation(), Sound.valueOf(soundName.toUpperCase()), 1.0f, 1.0f);
            } catch (IllegalArgumentException e) {
                logger.warning(sysLang.getSystemMessage(Lang.INVALID_SOUND_IN_CONFIG, soundName));
            }
        }
    }

    /**
     * Sistemi, oyuncuyu otomatik olarak AFK durumuna geçirir.
     */
    private void setAutoAfkStatus(Player player) {
        PlayerState state = getState(player);
        if (state.isEffectivelyAfk()) return;

        state.setAutoAfk(true);
        state.setAfkReason("command.afk.auto_afk_reason");

        setPlayerAfk(state);
    }

    public void setManualAFK(Player player, String reasonOrKey) {
        PlayerState state = getState(player);
        if (state.isEffectivelyAfk()) return;

        state.setManualAfk(true);

        if (reasonOrKey.startsWith("behavior.")) {
            state.setAutonomous(true);
        }

        state.setAfkReason(reasonOrKey);

        setPlayerAfk(state);

        if (configManager.isBroadcastOnAfkEnabled()) {
            String rawTemplate = plLang.getRawMessage("command.afk.on_afk_broadcast");
            if (rawTemplate != null && !rawTemplate.isEmpty()) {

                String displayReason;
                if (reasonOrKey.startsWith("behavior.") || reasonOrKey.startsWith("command.afk")) {
                    displayReason = plLang.getMessage(reasonOrKey).replace(plLang.getPrefix(), "");
                } else {
                    displayReason = reasonOrKey;
                }

                String msg = rawTemplate
                        .replace("%player_displayname%", player.getDisplayName())
                        .replace("%reason%", displayReason);
                plLang.broadcastFormattedMessage(msg);
            }
        }
    }

    public void unsetAfkStatus(Player player) {
        PlayerState state = getState(player);
        if (!state.isEffectivelyAfk()) return;

        saveTotalAfkTime(player);

        boolean wasManual = state.isManualAfk();
        state.setManualAfk(false);
        state.setAutoAfk(false);
        state.setAutonomous(false);
        state.setSystemPunished(false);
        state.setAfkReason(null);

        plLang.sendMessage(player, "command.afk.not_afk_now");

        if (wasManual && configManager.isBroadcastOnReturnEnabled()) {
            String rawTemplate = plLang.getRawMessage("command.afk.on_return_broadcast");
            if (rawTemplate != null && !rawTemplate.isEmpty()) {
                plLang.broadcastMessage("command.afk.on_return_broadcast",
                        "%player_displayname%", player.getDisplayName());
            }
        }
    }

    /**
     * Bir oyuncunun son AFK periyodunda ne kadar süre kaldığını hesaplar
     * ve veritabanına ekler.
     */
    private void saveTotalAfkTime(Player player) {
        Long startTime = afkStartTime.remove(player.getUniqueId());
        if (startTime != null) {
            long afkDurationSeconds = (System.currentTimeMillis() - startTime) / 1000;
            if (afkDurationSeconds > 0) {
                databaseManager.updateTotalAfkTime(player.getUniqueId(), afkDurationSeconds);
            }
        }
    }

    public void executeActions(Player player, List<Map<String, String>> actions) {
        long maxAfkTimeForAction;
        RegionOverride override = configManager.getRegionOverrideForPlayer(player);
        if(override != null && override.getMaxAfkTime() > 0) {
            maxAfkTimeForAction = override.getMaxAfkTime();
        } else {
            maxAfkTimeForAction = configManager.getMaxAfkTimeSeconds();
        }

        for (Map<String, String> action : actions) {
            String type = action.get("type");

            if ("DISCORD_WEBHOOK".equalsIgnoreCase(type)) {
                String message = applyPlaceholders(player, action.get("message"), 0, maxAfkTimeForAction);
                DiscordWebhookUtil.sendMessage(plugin, message);
                continue;
            }

            String command = applyPlaceholders(player, action.get("command"), 0, maxAfkTimeForAction);

            if ("PLAYER".equalsIgnoreCase(type)) {
                Bukkit.getScheduler().runTask(plugin, () -> player.performCommand(command));
            } else if ("CONSOLE".equalsIgnoreCase(type)) {
                String cleanCommand = ChatColor.stripColor(ChatColor.translateAlternateColorCodes('&', command));
                Bukkit.getScheduler().runTask(plugin, () -> Bukkit.dispatchCommand(Bukkit.getConsoleSender(), cleanCommand));
            }
        }
    }

    private String applyPlaceholders(Player player, String text, long timeLeft, long maxAfkTime) {
        if (text == null || text.isEmpty()) return "";

        String currentDisplayName = getState(player).getOriginalDisplayName() != null
                ? getState(player).getOriginalDisplayName()
                : player.getDisplayName();

        text = text.replace("%player%", player.getName())
                .replace("%player_displayname%", currentDisplayName)
                .replace("%world%", player.getWorld().getName())
                .replace("%time_left%", TimeUtil.formatTime(timeLeft))
                .replace("%max_time%", TimeUtil.formatTime(maxAfkTime));

        if (plugin.isPlaceholderApiEnabled()) {
            text = PlaceholderAPI.setPlaceholders(player, text);
        }

        return text;
    }

    public boolean isMarkedAsAutonomous(Player player) {
        return getState(player).isAutonomous();
    }

    public void trackClick(Player player) {
        long currentTime = System.currentTimeMillis();

        PointlessActivityData data = botDetectionData.get(player.getUniqueId(), k -> new PointlessActivityData());

        if (data == null) return;

        if (data.getLastClickTime() > 0) {
            long interval = currentTime - data.getLastClickTime();
            data.getClickIntervals().add(interval);

            while (data.getClickIntervals().size() > configManager.getAutoClickerCheckAmount()) {
                data.getClickIntervals().remove(0);
            }

            if (data.getClickIntervals().size() == configManager.getAutoClickerCheckAmount()) {
                if (isConsistentClickPattern(data.getClickIntervals())) {
                    data.incrementAutoClickerDetections();
                    if (data.getAutoClickerDetections() >= configManager.getAutoClickerDetectionsToPunish()) {
                        triggerSuspicionAndChallenge(player, "behavior.autoclicker_detected");

                        data.resetAutoClickerDetections();
                        data.getClickIntervals().clear();
                    }
                } else {

                    data.resetAutoClickerDetections();
                }
            }
        }
        data.setLastClickTime(currentTime);
    }

    /**
     * Verilen tıklama aralıklarının tutarlı bir desen oluşturup oluşturmadığını kontrol eder.
     */
    private boolean isConsistentClickPattern(List<Long> intervals) {
        if (intervals.isEmpty()) return false;

        long total = 0;
        for (long interval : intervals) {
            total += interval;
        }
        long average = total / intervals.size();

        for (long interval : intervals) {
            if (Math.abs(interval - average) > configManager.getAutoClickerMaxDeviation()) {
                return false;
            }
        }
        return true;
    }

    public PointlessActivityData getBotDetectionData(UUID uuid) {
        return botDetectionData.get(uuid, k -> new PointlessActivityData());
    }

    public void resetBotDetectionData(UUID uuid) {
        botDetectionData.invalidate(uuid);
    }


    public void resetSuspicion(Player player) {
        PlayerState state = getState(player);
        state.setSuspicious(false);
        resetBotDetectionData(player.getUniqueId());

        if (plugin.getBehaviorAnalysisManager().isEnabled()) {
            plugin.getBehaviorAnalysisManager().getPlayerData(player).reset();
        }
    }

    public boolean isSuspicious(Player player) {
        return getState(player).isSuspicious();
    }

    public void setSuspicious(Player player) {
        getState(player).setSuspicious(true);
    }

    /**
     * Bir oyuncuyu şüpheli olarak işaretler ve (eğer aktifse) bir Captcha testi başlatır.
     * Bu, tüm bot tespit mekanizmaları için merkezi giriş noktasıdır.
     * @param player Şüpheli bulunan oyuncu.
     * @param reasonKey Konsola loglanacak olan şüphe sebebi (mesaj anahtarı).
     */
    public void triggerSuspicionAndChallenge(Player player, String reasonKey) {
        if (isSuspicious(player) || player.hasPermission(configManager.getPermBypassAll())) {
            return;
        }

        if (!configManager.isTuringTestEnabled() || !plugin.getCaptchaManager().isPresent()) {
            setManualAFK(player, reasonKey);
            return;
        }

        debugMgr.log(DebugManager.DebugModule.ACTIVITY_LISTENER, "Player %s is being marked as suspicious. Reason: %s", player.getName(), reasonKey);
        setSuspicious(player);
        plugin.getCaptchaManager().ifPresent(manager -> manager.startChallenge(player));
    }

    public List<Player> getAfkPlayers() {
        return Bukkit.getOnlinePlayers().stream()
                .filter(this::isEffectivelyAfk)
                .collect(Collectors.toList());
    }
}