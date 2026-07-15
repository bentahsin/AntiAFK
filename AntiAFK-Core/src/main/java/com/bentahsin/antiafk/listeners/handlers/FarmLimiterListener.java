package com.bentahsin.antiafk.listeners.handlers;

import com.bentahsin.antiafk.AntiAFKPlugin;
import com.bentahsin.antiafk.behavior.BehaviorAnalysisManager;
import com.bentahsin.antiafk.listeners.ActivityListener;
import com.bentahsin.antiafk.managers.AFKManager;
import com.bentahsin.antiafk.managers.ConfigManager;
import com.bentahsin.antiafk.managers.DebugManager;
import com.bentahsin.antiafk.managers.PlayerLanguageManager;
import com.google.inject.Inject;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.BlockGrowEvent;
import org.bukkit.event.block.BlockSpreadEvent;
import org.bukkit.event.entity.CreatureSpawnEvent;

public class FarmLimiterListener extends ActivityListener implements org.bukkit.event.Listener {

    @Inject
    public FarmLimiterListener(
            AntiAFKPlugin plugin, AFKManager afkManager, ConfigManager configManager,
            DebugManager debugManager, PlayerLanguageManager languageManager,
            BehaviorAnalysisManager behaviorAnalysisManager
    ) {
        super(plugin, afkManager, configManager, debugManager, languageManager, behaviorAnalysisManager);
    }

    @EventHandler(ignoreCancelled = true)
    public void onCreatureSpawn(CreatureSpawnEvent event) {
        if (shouldSuppressFarm(event.getLocation())) {
            event.setCancelled(true);
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onBlockGrow(BlockGrowEvent event) {
        if (shouldSuppressFarm(event.getBlock().getLocation())) {
            event.setCancelled(true);
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onBlockSpread(BlockSpreadEvent event) {
        if (shouldSuppressFarm(event.getBlock().getLocation())) {
            event.setCancelled(true);
        }
    }

    private boolean shouldSuppressFarm(Location location) {
        if (!configManager.isFarmLimiterEnabled()) return false;

        World world = location.getWorld();
        if (world == null) return false;

        double radius = configManager.getFarmLimiterRadius();
        double radiusSquared = radius * radius;

        boolean afkNearby = false;
        boolean activeNearby = false;

        for (Player player : world.getPlayers()) {
            if (player.getLocation().distanceSquared(location) > radiusSquared) continue;

            if (afkManager.getStateManager().isEffectivelyAfk(player)) {
                afkNearby = true;
            } else {
                activeNearby = true;
            }
        }

        if (!afkNearby) return false;
        return !(configManager.isFarmLimiterCheckFriends() && activeNearby);
    }
}
