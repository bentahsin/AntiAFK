package com.bentahsin.antiafk.listeners.handlers;

import com.bentahsin.antiafk.AntiAFKPlugin;
import com.bentahsin.antiafk.behavior.BehaviorAnalysisManager;
import com.bentahsin.antiafk.listeners.ActivityListener;
import com.bentahsin.antiafk.managers.AFKManager;
import com.bentahsin.antiafk.managers.ConfigManager;
import com.bentahsin.antiafk.managers.DebugManager;
import com.bentahsin.antiafk.managers.PlayerLanguageManager;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.event.player.PlayerItemHeldEvent;

/**
 * Oyuncunun envanteri ve eşyaları ile olan etkileşimlerini yönetir.
 */
public class PlayerInventoryListener extends ActivityListener implements org.bukkit.event.Listener {

    public PlayerInventoryListener(
            AntiAFKPlugin plugin, AFKManager afkManager, ConfigManager configManager,
            DebugManager debugManager, PlayerLanguageManager languageManager,
            BehaviorAnalysisManager behaviorAnalysisManager
    ) {
        super(plugin, afkManager, configManager, debugManager, languageManager, behaviorAnalysisManager);
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerDropItem(PlayerDropItemEvent event) {
        if (configManager.isCheckItemDrop()) {
            handleActivity(event.getPlayer(), null, false);
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onInventoryClick(InventoryClickEvent event) {
        if (configManager.isCheckInventoryActivity()) {
            if (event.getWhoClicked() instanceof Player) {
                handleActivity((Player) event.getWhoClicked(), null, false);
            }
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onInventoryOpen(InventoryOpenEvent event) {
        if (configManager.isCheckInventoryActivity()) {
            handleActivity((Player) event.getPlayer(), null, false);
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerConsume(PlayerItemConsumeEvent event) {
        if (configManager.isCheckItemConsume()) {
            handleActivity(event.getPlayer(), null, false);
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onHeldItemChange(PlayerItemHeldEvent event) {
        if (configManager.isCheckHeldItemChange()) {
            handleActivity(event.getPlayer(), null, false);
        }
    }
}