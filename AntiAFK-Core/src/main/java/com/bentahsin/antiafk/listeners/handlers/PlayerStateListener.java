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
import org.bukkit.event.entity.EntityDamageEvent;

/**
 * Oyuncunun hasar almazlık gibi durumlarını yönetir.
 */
public class PlayerStateListener extends ActivityListener implements org.bukkit.event.Listener {

    public PlayerStateListener(
            AntiAFKPlugin plugin, AFKManager afkManager, ConfigManager configManager,
            DebugManager debugManager, PlayerLanguageManager languageManager,
            BehaviorAnalysisManager behaviorAnalysisManager
    ) {
        super(plugin, afkManager, configManager, debugManager, languageManager, behaviorAnalysisManager);
    }

    @EventHandler(ignoreCancelled = true)
    public void onPlayerDamage(EntityDamageEvent event) {
        if (!(event.getEntity() instanceof Player)) return;
        Player player = (Player) event.getEntity();

        if (configManager.isSetInvulnerable() && afkManager.getStateManager().isManuallyAFK(player)) {
            event.setCancelled(true);
        }
    }
}