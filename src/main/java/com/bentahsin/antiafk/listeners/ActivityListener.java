package com.bentahsin.antiafk.listeners;

import com.bentahsin.antiafk.AntiAFKPlugin;
import com.bentahsin.antiafk.data.PointlessActivityData;
import com.bentahsin.antiafk.managers.AFKManager;
import com.bentahsin.antiafk.managers.ConfigManager;
import com.bentahsin.antiafk.managers.LanguageManager;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.player.PlayerInteractEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

/**
 * Tüm alt dinleyici sınıfları için ortak mantığı ve yöneticilere erişimi
 * sağlayan bir üst (abstract) sınıf. Bu, kod tekrarını önler.
 */
public abstract class ActivityListener {

    private final AntiAFKPlugin plugin;
    private final AFKManager afkManager;
    private final ConfigManager configManager;
    private final LanguageManager languageManager;

    private static final Map<UUID, Long> lastWorldChangeTime = new HashMap<>();
    private static final Map<UUID, Integer> worldChangeCounts = new HashMap<>();

    public ActivityListener(AntiAFKPlugin plugin) {
        this.plugin = plugin;
        this.afkManager = plugin.getAfkManager();
        this.configManager = plugin.getConfigManager();
        this.languageManager = plugin.getLanguageManager();
    }

    /**
     * Her türlü oyuncu aktivitesini işleyen ana metot.
     * Bu metot, "Şüpheli Mod" mantığını, bypass izinlerini ve çeşitli koruma
     * katmanlarını doğru sırada ve koşulda işler.
     *
     * @param player Aktiviteyi yapan oyuncu.
     * @param event  Aktiviteyi tetikleyen olay (null olabilir).
     * @param isMovementEvent Aktivitenin bir hareket olup olmadığı.
     */
    protected void handleActivity(Player player, Event event, boolean isMovementEvent) {

        if (afkManager.isSuspicious(player)) {
            return;
        }

        if (player.hasPermission(configManager.getPermBypassAll())) {
            if (afkManager.isEffectivelyAfk(player)) {
                afkManager.unsetAfkStatus(player);
            }
            afkManager.updateActivity(player);
            return;
        }

        if (afkManager.isMarkedAsAutonomous(player) && !isMovementEvent && !isHighValueActivity(event)) {
            return;
        }

        int maxPointlessActivities = configManager.getMaxPointlessActivities();
        if (!isMovementEvent && maxPointlessActivities > 0 && !player.hasPermission(configManager.getPermBypassPointless())) {

            Location playerLocation = player.getLocation().getBlock().getLocation();
            PointlessActivityData activityData = afkManager.getBotDetectionData(player.getUniqueId());

            Location interactedBlock = null;
            if (event instanceof PlayerInteractEvent) {
                PlayerInteractEvent interactEvent = (PlayerInteractEvent) event;
                if (interactEvent.hasBlock()) {
                    interactedBlock = Objects.requireNonNull(interactEvent.getClickedBlock()).getLocation();
                }
            }

            if (playerLocation.equals(activityData.getLastPlayerLocation())) {
                boolean isSameTarget = (interactedBlock != null && interactedBlock.equals(activityData.getLastInteractedBlockLocation()))
                        || (interactedBlock == null && activityData.getLastInteractedBlockLocation() == null);

                if (isSameTarget) {
                    activityData.incrementPointlessActivityCounter();

                    if (configManager.isTuringTestEnabled() &&
                            plugin.getCaptchaManager().map(manager -> !manager.isBeingTested(player)).orElse(false) &&
                            activityData.getPointlessActivityCounter() == configManager.getTriggerOnPointlessActivityCount()) {

                        plugin.getCaptchaManager().ifPresent(manager -> manager.startChallenge(player));
                    }

                    if (activityData.getPointlessActivityCounter() >= maxPointlessActivities) {
                        afkManager.setManualAFK(player, "behavior.pointless_activity_detected");
                        afkManager.resetBotDetectionData(player.getUniqueId());
                        return;
                    }
                } else {
                    activityData.resetAndSetPointlessActivity(playerLocation, interactedBlock);
                }
            } else {
                activityData.resetAndSetPointlessActivity(playerLocation, interactedBlock);
            }
        } else if (isMovementEvent) {
            PointlessActivityData activityData = afkManager.getBotDetectionData(player.getUniqueId());
            if (activityData != null) {
                activityData.resetPointlessActivityData();
            }
        }

        if (afkManager.isEffectivelyAfk(player)) {
            afkManager.unsetAfkStatus(player);
        }

        afkManager.updateActivity(player);

        if (!isMovementEvent && plugin.getBehaviorAnalysisManager().isEnabled()) {
            plugin.getBehaviorAnalysisManager().getPlayerData(player).reset();
        }
    }

    /**
     * Hızlı dünya değiştirme istismarını kontrol eder.
     */
    protected void handleWorldChangeAbuse(Player player) {
        if (!configManager.isCheckWorldChangeEnabled() || player.hasPermission(configManager.getPermBypassAll())) {
            return;
        }

        long currentTime = System.currentTimeMillis();
        UUID uuid = player.getUniqueId();
        long lastChange = lastWorldChangeTime.getOrDefault(uuid, 0L);
        int count = worldChangeCounts.getOrDefault(uuid, 0);

        if (currentTime - lastChange < configManager.getWorldChangeCooldown() * 1000L) {
            count++;
        } else {
            count = 1;
        }

        worldChangeCounts.put(uuid, count);
        lastWorldChangeTime.put(uuid, currentTime);

        if (count >= configManager.getMaxWorldChanges()) {
            afkManager.setManualAFK(player, "behavior.rapid_world_change");
            worldChangeCounts.remove(uuid);
            lastWorldChangeTime.remove(uuid);
        }
    }

    /**
     * Bir oyuncunun auto-clicker aktivitesinin takip edilip edilmeyeceğini belirler.
     */
    protected boolean shouldTrackAutoClicker(Player player) {
        return !player.hasPermission(configManager.getPermBypassAll()) &&
                !player.hasPermission(configManager.getPermBypassAutoclicker());
    }

    /**
     * Bir aktivitenin, şüpheli bir oyuncuyu temize çıkaracak kadar
     * "değerli" olup olmadığını belirler.
     *
     * @param event Aktiviteyi tetikleyen olay.
     * @return Aktivite yüksek değerli ise true.
     */
    private boolean isHighValueActivity(Event event) {
        if (event == null) return false;

        return event instanceof org.bukkit.event.player.AsyncPlayerChatEvent ||
                event instanceof org.bukkit.event.player.PlayerCommandPreprocessEvent ||
                event instanceof org.bukkit.event.inventory.InventoryOpenEvent;
    }

    protected AntiAFKPlugin getPlugin() { return plugin; }
    protected AFKManager getAfkManager() { return afkManager; }
    protected ConfigManager getConfigManager() { return configManager; }
    protected LanguageManager getLanguageManager() { return languageManager; }
}