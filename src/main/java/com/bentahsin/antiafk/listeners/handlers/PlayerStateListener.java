package com.bentahsin.antiafk.listeners.handlers;

import com.bentahsin.antiafk.AntiAFKPlugin;
import com.bentahsin.antiafk.listeners.ActivityListener;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDamageEvent;

/**
 * Oyuncunun hasar almazlık gibi durumlarını yönetir.
 */
public class PlayerStateListener extends ActivityListener implements org.bukkit.event.Listener {

    public PlayerStateListener(AntiAFKPlugin plugin) {
        super(plugin);
    }

    @EventHandler(ignoreCancelled = true)
    public void onPlayerDamage(EntityDamageEvent event) {
        if (!(event.getEntity() instanceof Player)) return;
        Player player = (Player) event.getEntity();

        if (getConfigManager().isSetInvulnerable() && getAfkManager().getStateManager().isManuallyAFK(player)) {
            event.setCancelled(true);
        }
    }
}