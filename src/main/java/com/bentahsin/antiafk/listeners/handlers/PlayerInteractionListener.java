package com.bentahsin.antiafk.listeners.handlers;

import com.bentahsin.antiafk.AntiAFKPlugin;
import com.bentahsin.antiafk.listeners.ActivityListener;
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


    public PlayerInteractionListener(AntiAFKPlugin plugin) {
        super(plugin);
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        if (getConfigManager().isCheckInteraction()) {
            handleActivity(player, event, false);
        }

        if (getConfigManager().isAutoClickerEnabled() &&
                (event.getAction() == Action.LEFT_CLICK_AIR || event.getAction() == Action.LEFT_CLICK_BLOCK) &&
                shouldTrackAutoClicker(player)) {
            getAfkManager().trackClick(player);
        }
    }

    /**
     * Oyuncunun eğilip kalkma eylemlerini dinler.
     * Bu, hem klasik AFK tespiti hem de "Öğrenme Modu" için veri sağlar.
     */
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerToggleSneak(PlayerToggleSneakEvent event) {
        Player player = event.getPlayer();

        if (getConfigManager().isCheckToggleSneak()) {
            handleActivity(player, event, false);
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerAttack(EntityDamageByEntityEvent event) {
        if (event.getDamager() instanceof Player) {
            Player player = (Player) event.getDamager();
            if (getConfigManager().isCheckPlayerAttack()) {
                handleActivity(player, null, false);
            }

            if (getConfigManager().isAutoClickerEnabled() && shouldTrackAutoClicker(player)) {
                getAfkManager().trackClick(player);
            }
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onBookEdit(PlayerEditBookEvent event) {
        if (getConfigManager().isCheckBookActivity()) {
            handleActivity(event.getPlayer(), null, false);
        }
    }
}