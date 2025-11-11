package com.bentahsin.antiafk.managers;

import com.bentahsin.antiafk.AntiAFKPlugin;
import com.bentahsin.antiafk.language.SystemLanguageManager;
import com.bentahsin.antiafk.models.RegionOverride;
import com.bentahsin.antiafk.storage.DatabaseManager;
import org.bukkit.entity.Player;

public class AFKManager {

    private final ConfigManager configManager;

    private final PlayerStateManager stateManager;
    private final WarningManager warningManager;
    private final PunishmentManager punishmentManager;
    private final BotDetectionManager botDetectionManager;

    public AFKManager(AntiAFKPlugin plugin) {
        this.configManager = plugin.getConfigManager();
        DebugManager debugMgr = plugin.getDebugManager();
        DatabaseManager databaseManager = plugin.getDatabaseManager();
        PlayerLanguageManager plLang = plugin.getPlayerLanguageManager();
        SystemLanguageManager sysLang = plugin.getSystemLanguageManager();
        this.warningManager = new WarningManager(plugin, configManager, plLang, sysLang);
        this.stateManager = new PlayerStateManager(plugin, warningManager);
        this.punishmentManager = new PunishmentManager(plugin, configManager, databaseManager, stateManager, plLang, debugMgr);
        this.botDetectionManager = new BotDetectionManager(plugin, configManager, stateManager, debugMgr);
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

        long afkSeconds = stateManager.getAfkTime(player);

        if (afkSeconds >= effectiveMaxAfkTime) {
            if (!stateManager.isEffectivelyAfk(player)) {
                punishmentManager.applyPunishment(player, override);
            }
            return;
        }

        long autoAfkThreshold = configManager.getAutoSetAfkSeconds();
        if (autoAfkThreshold > 0 && afkSeconds >= autoAfkThreshold) {
            if (!stateManager.isEffectivelyAfk(player)) {
                stateManager.setAutoAfkStatus(player);
            }
        }

        if (!stateManager.isEffectivelyAfk(player)) {
            warningManager.checkAndSendWarning(player, afkSeconds, effectiveMaxAfkTime, stateManager);
        }
    }

    public void onPlayerJoin(Player player) {
        stateManager.addPlayer(player);
        punishmentManager.checkRejoin(player);
    }

    public void onPlayerQuit(Player player) {
        stateManager.removePlayer(player);
        botDetectionManager.resetBotDetectionData(player.getUniqueId());
    }

    public PlayerStateManager getStateManager() { return stateManager; }
    public WarningManager getWarningManager() { return warningManager; }
    public PunishmentManager getPunishmentManager() { return punishmentManager; }
    public BotDetectionManager getBotDetectionManager() { return botDetectionManager; }
}