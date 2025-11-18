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
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerToggleSneakEvent;
import org.bukkit.event.player.PlayerEditBookEvent;

/**
 * Oyuncunun dünya ile olan etkileşimlerini yönetir (tıklama, vurma, eğilme vb.).
 */
public class PlayerInteractionListener extends ActivityListener implements org.bukkit.event.Listener {


    public PlayerInteractionListener(
            AntiAFKPlugin plugin, AFKManager afkManager, ConfigManager configManager,
            DebugManager debugManager, PlayerLanguageManager languageManager,
            BehaviorAnalysisManager behaviorAnalysisManager
    ) {
        super(plugin, afkManager, configManager, debugManager, languageManager, behaviorAnalysisManager);
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        if (configManager.isCheckInteraction()) {
            handleActivity(player, event, false);
        }

        if (configManager.isAutoClickerEnabled() &&
                (event.getAction() == Action.LEFT_CLICK_AIR || event.getAction() == Action.LEFT_CLICK_BLOCK) &&
                shouldTrackAutoClicker(player)) {
            afkManager.getBotDetectionManager().trackClick(player);
        }
    }

    /**
     * Oyuncunun eğilip kalkma eylemlerini dinler.
     * Bu, hem klasik AFK tespiti hem de "Öğrenme Modu" için veri sağlar.
     */
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerToggleSneak(PlayerToggleSneakEvent event) {
        Player player = event.getPlayer();

        if (configManager.isCheckToggleSneak()) {
            handleActivity(player, event, false);
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerAttack(EntityDamageByEntityEvent event) {
        if (event.getDamager() instanceof Player) {
            Player player = (Player) event.getDamager();
            if (configManager.isCheckPlayerAttack()) {
                handleActivity(player, null, false);
            }

            if (configManager.isAutoClickerEnabled() && shouldTrackAutoClicker(player)) {
                afkManager.getBotDetectionManager().trackClick(player);
            }
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onBookEdit(PlayerEditBookEvent event) {
        if (configManager.isCheckBookActivity()) {
            handleActivity(event.getPlayer(), null, false);
        }
    }
}