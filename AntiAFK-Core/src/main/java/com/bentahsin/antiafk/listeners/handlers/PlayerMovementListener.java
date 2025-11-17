package com.bentahsin.antiafk.listeners.handlers;

import com.bentahsin.antiafk.AntiAFKPlugin;
import com.bentahsin.antiafk.behavior.BehaviorAnalysisManager;
import com.bentahsin.antiafk.behavior.PlayerBehaviorData;
import com.bentahsin.antiafk.listeners.ActivityListener;
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
public class PlayerMovementListener extends ActivityListener implements org.bukkit.event.Listener {

    private final BehaviorAnalysisManager analysisManager;

    public PlayerMovementListener(AntiAFKPlugin plugin) {
        super(plugin);
        this.analysisManager = plugin.getBehaviorAnalysisManager();
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
            getConfigManager().clearPlayerCache(player);
        }

        if (analysisManager.isEnabled() && !player.hasPermission(getConfigManager().getPermBypassBehavioral())) {
            PlayerBehaviorData data = analysisManager.getPlayerData(player);
            LinkedList<Location> history = data.getMovementHistory();
            synchronized (history) {
                history.add(to);
                while (history.size() > analysisManager.getHistorySizeTicks()) {
                    history.removeFirst();
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerChangedWorld(PlayerChangedWorldEvent event) {
        Player player = event.getPlayer();

        if (analysisManager.isEnabled()) {
            PlayerBehaviorData data = analysisManager.getPlayerData(player);
            if (data != null) {
                data.reset();
            }
        }

        handleWorldChangeAbuse(player);
    }
}