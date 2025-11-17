package com.bentahsin.antiafk.listeners.handlers;

import com.bentahsin.antiafk.AntiAFKPlugin;
import com.bentahsin.antiafk.behavior.BehaviorAnalysisManager;
import com.bentahsin.antiafk.behavior.PlayerBehaviorData;
import com.bentahsin.antiafk.listeners.ActivityListener;
import com.bentahsin.antiafk.managers.AFKManager;
import com.bentahsin.antiafk.managers.ConfigManager;
import com.bentahsin.antiafk.managers.DebugManager;
import com.bentahsin.antiafk.managers.PlayerLanguageManager;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerMoveEvent;

import java.util.LinkedList;

/**
 * Oyuncu hareketi ve dünya değiştirme olaylarını yönetir.
 */
@Singleton
public class PlayerMovementListener extends ActivityListener implements org.bukkit.event.Listener {

    @Inject
    public PlayerMovementListener(
            AntiAFKPlugin plugin, AFKManager afkManager, ConfigManager configManager,
            DebugManager debugManager, PlayerLanguageManager languageManager,
            BehaviorAnalysisManager behaviorAnalysisManager
    ) {
        super(plugin, afkManager, configManager, debugManager, languageManager, behaviorAnalysisManager);
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();
        Location to = event.getTo();
        Location from = event.getFrom();

        if (to == null) {
            return;
        }


        boolean hasMovedBlocks = (from.getBlockX() != to.getBlockX() || from.getBlockY() != to.getBlockY() || from.getBlockZ() != to.getBlockZ());
        boolean hasRotatedSignificantly = Math.abs(from.getYaw() - to.getYaw()) >= 1.0F;

        if (!hasMovedBlocks && !hasRotatedSignificantly) {
            return;
        }

        handleActivity(player, event, true);

        if (hasMovedBlocks) {
            configManager.clearPlayerCache(player);
        }

        if (behaviorAnalysisManager.isEnabled() && !player.hasPermission(configManager.getPermBypassBehavioral())) {
            PlayerBehaviorData data = behaviorAnalysisManager.getPlayerData(player);
            LinkedList<Location> history = data.getMovementHistory();
            synchronized (history) {
                history.add(to);
                while (history.size() > behaviorAnalysisManager.getHistorySizeTicks()) {
                    history.removeFirst();
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerChangedWorld(PlayerChangedWorldEvent event) {
        Player player = event.getPlayer();

        if (behaviorAnalysisManager.isEnabled()) {
            PlayerBehaviorData data = behaviorAnalysisManager.getPlayerData(player);
            if (data != null) {
                data.reset();
            }
        }

        handleWorldChangeAbuse(player);
    }
}