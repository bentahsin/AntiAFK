package com.bentahsin.antiafk.listeners;

import com.bentahsin.antiafk.AntiAFKPlugin;
import com.bentahsin.antiafk.data.PointlessActivityData;
import com.bentahsin.antiafk.managers.AFKManager;
import com.bentahsin.antiafk.managers.ConfigManager;
import com.bentahsin.antiafk.managers.DebugManager;
import com.bentahsin.antiafk.managers.PlayerLanguageManager;
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
    private final DebugManager debugMgr;
    private final PlayerLanguageManager languageManager;

    private static final Map<UUID, Long> lastWorldChangeTime = new HashMap<>();
    private static final Map<UUID, Integer> worldChangeCounts = new HashMap<>();

    public ActivityListener(AntiAFKPlugin plugin) {
        this.plugin = plugin;
        this.afkManager = plugin.getAfkManager();
        this.configManager = plugin.getConfigManager();
        this.debugMgr = plugin.getDebugManager();
        this.languageManager = plugin.getPlayerLanguageManager();
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
        final String eventType = event != null ? event.getEventName() : (isMovementEvent ? "PlayerMoveEvent" : "Unknown");
        debugMgr.log(DebugManager.DebugModule.ACTIVITY_LISTENER, "Activity '%s' detected for player %s.", eventType, player.getName());

        if (afkManager.getStateManager().isSuspicious(player)) {
            debugMgr.log(DebugManager.DebugModule.ACTIVITY_LISTENER, "Activity for %s ignored: Player is suspicious (awaiting captcha).", player.getName());
            return;
        }

        if (player.hasPermission(configManager.getPermBypassAll())) {
            if (afkManager.getStateManager().isEffectivelyAfk(player)) {
                afkManager.getStateManager().unsetAfkStatus(player);
            }
            afkManager.getStateManager().updateActivity(player);
            afkManager.getWarningManager().clearWarningCache(player);
            debugMgr.log(DebugManager.DebugModule.ACTIVITY_LISTENER, "Activity for %s processed with bypass_all permission.", player.getName());
            return;
        }

        if (afkManager.getStateManager().isMarkedAsAutonomous(player) && !isMovementEvent && !isHighValueActivity(event)) {
            debugMgr.log(DebugManager.DebugModule.ACTIVITY_LISTENER, "Low-value activity for %s ignored: Player is marked as autonomous.", player.getName());
            return;
        }

        int maxPointlessActivities = configManager.getMaxPointlessActivities();
        if (!isMovementEvent && maxPointlessActivities > 0 && !player.hasPermission(configManager.getPermBypassPointless())) {

            Location playerLocation = player.getLocation().getBlock().getLocation();
            PointlessActivityData activityData = afkManager.getBotDetectionManager().getBotDetectionData(player.getUniqueId());

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

                    if (activityData.getPointlessActivityCounter() >= maxPointlessActivities) {
                        debugMgr.log(DebugManager.DebugModule.ACTIVITY_LISTENER, "Pointless activity limit reached for %s. Marking as AFK.", player.getName());
                        afkManager.getBotDetectionManager().triggerSuspicionAndChallenge(player, "behavior.pointless_activity_detected");
                        afkManager.getBotDetectionManager().resetBotDetectionData(player.getUniqueId());
                        return;
                    }
                } else {
                    activityData.resetAndSetPointlessActivity(playerLocation, interactedBlock);
                }
            } else {
                activityData.resetAndSetPointlessActivity(playerLocation, interactedBlock);
            }
        } else if (isMovementEvent) {
            PointlessActivityData activityData = afkManager.getBotDetectionManager().getBotDetectionData(player.getUniqueId());
            if (activityData != null) {
                activityData.resetPointlessActivityData();
            }
        }

        if (afkManager.getStateManager().isEffectivelyAfk(player)) {
            debugMgr.log(DebugManager.DebugModule.ACTIVITY_LISTENER, "Player %s is no longer AFK due to '%s'.", player.getName(), eventType);
            afkManager.getStateManager().unsetAfkStatus(player);
        }

        afkManager.getStateManager().updateActivity(player);
        debugMgr.log(DebugManager.DebugModule.ACTIVITY_LISTENER, "AFK timer reset for %s.", player.getName());

        if (!isMovementEvent && plugin.getBehaviorAnalysisManager().isEnabled()) {
            plugin.getBehaviorAnalysisManager().getPlayerData(player).reset();
            debugMgr.log(DebugManager.DebugModule.BEHAVIORAL_ANALYSIS, "Behavioral analysis data reset for %s due to non-movement activity.", player.getName());
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
            debugMgr.log(DebugManager.DebugModule.ACTIVITY_LISTENER, "Rapid world change detected for %s. Marking as AFK.", player.getName());
            afkManager.getBotDetectionManager().triggerSuspicionAndChallenge(player, "behavior.rapid_world_change");
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
    protected PlayerLanguageManager getLanguageManager() { return languageManager; }
}