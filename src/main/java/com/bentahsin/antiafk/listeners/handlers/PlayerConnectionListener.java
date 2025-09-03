package com.bentahsin.antiafk.listeners.handlers;

import com.bentahsin.antiafk.AntiAFKPlugin;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.UUID;

/**
 * Oyuncu giriş ve çıkış olaylarını yönetir.
 */
public class PlayerConnectionListener implements Listener {

    private final AntiAFKPlugin plugin;

    public PlayerConnectionListener(AntiAFKPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        plugin.getAfkManager().checkRejoin(event.getPlayer());
        plugin.getAfkManager().addPlayer(event.getPlayer());
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        UUID playerUUID = player.getUniqueId();

        plugin.getConfigManager().clearPlayerCache(player);
        plugin.getAfkManager().removePlayer(player);
        if (plugin.getBehaviorAnalysisManager() != null && plugin.getBehaviorAnalysisManager().isEnabled()) {
            plugin.getBehaviorAnalysisManager().removePlayerData(player);
        }

        plugin.getPlayersInChatInput().remove(playerUUID);
        plugin.getPlayerMenuUtilityMap().remove(playerUUID);
        plugin.getBookInputManager().ifPresent(manager -> manager.onPlayerQuit(player));
        plugin.getCaptchaManager().ifPresent(manager -> manager.onPlayerQuit(player));
    }
}