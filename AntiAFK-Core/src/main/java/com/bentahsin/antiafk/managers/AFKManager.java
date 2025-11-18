package com.bentahsin.antiafk.managers;

import com.bentahsin.antiafk.models.RegionOverride;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import org.bukkit.entity.Player;

/**
 * Ana AFK kontrol mantığını koordine eden merkezi yönetici.
 * Diğer tüm ilgili yöneticileri bir araya getirir.
 */
@Singleton
public class AFKManager {

    private final ConfigManager configManager;
    private final PlayerStateManager stateManager;
    private final WarningManager warningManager;
    private final PunishmentManager punishmentManager;
    private final BotDetectionManager botDetectionManager;

    @Inject
    public AFKManager(
            ConfigManager configManager,
            PlayerStateManager stateManager,
            WarningManager warningManager,
            PunishmentManager punishmentManager,
            BotDetectionManager botDetectionManager
    ) {
        this.configManager = configManager;
        this.stateManager = stateManager;
        this.warningManager = warningManager;
        this.punishmentManager = punishmentManager;
        this.botDetectionManager = botDetectionManager;
    }

    /**
     * Bir oyuncunun AFK durumunu, uyarılarını ve potansiyel cezalarını kontrol eder.
     * Bu metot, AFKCheckTask tarafından periyodik olarak çağrılır.
     * @param player Kontrol edilecek oyuncu.
     */
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