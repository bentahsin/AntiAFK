package com.bentahsin.antiafk.listeners.handlers;

import com.bentahsin.antiafk.AntiAFKPlugin;
import com.bentahsin.antiafk.behavior.BehaviorAnalysisManager;
import com.bentahsin.antiafk.listeners.ActivityListener;
import com.bentahsin.antiafk.managers.AFKManager;
import com.bentahsin.antiafk.managers.ConfigManager;
import com.bentahsin.antiafk.managers.DebugManager;
import com.bentahsin.antiafk.managers.PlayerLanguageManager;
import com.google.inject.Inject;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityPickupItemEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class PlayerItemPickupListener extends ActivityListener implements org.bukkit.event.Listener {

    private static final long NOTIFY_COOLDOWN_MILLIS = 3000L;
    private final Map<UUID, Long> lastNotified = new HashMap<>();

    @Inject
    public PlayerItemPickupListener(
            AntiAFKPlugin plugin, AFKManager afkManager, ConfigManager configManager,
            DebugManager debugManager, PlayerLanguageManager languageManager,
            BehaviorAnalysisManager behaviorAnalysisManager
    ) {
        super(plugin, afkManager, configManager, debugManager, languageManager, behaviorAnalysisManager);
    }

    @EventHandler(ignoreCancelled = true)
    public void onEntityPickupItem(EntityPickupItemEvent event) {
        if (!configManager.isItemPickupBlockerEnabled()) return;
        if (!(event.getEntity() instanceof Player)) return;

        Player player = (Player) event.getEntity();
        if (player.hasPermission(configManager.getPermBypassAll())) return;
        if (configManager.getDisabledWorlds().contains(player.getWorld().getName())) return;
        if (configManager.getExemptGameModes().contains(player.getGameMode().name())) return;
        if (!afkManager.getStateManager().isEffectivelyAfk(player)) return;

        event.setCancelled(true);
        notifyPlayer(player);
    }

    private void notifyPlayer(Player player) {
        long now = System.currentTimeMillis();
        UUID uuid = player.getUniqueId();
        Long last = lastNotified.get(uuid);
        if (last != null && now - last < NOTIFY_COOLDOWN_MILLIS) return;

        lastNotified.put(uuid, now);
        languageManager.sendMessage(player, "gameplay_control.item_pickup_blocker.blocked");
    }
}
