package com.bentahsin.antiafk.listeners.handlers;

import com.bentahsin.antiafk.AntiAFKPlugin;
import com.bentahsin.antiafk.learning.PatternAnalysisTask;
import com.bentahsin.antiafk.learning.RecordingManager;
import com.bentahsin.antiafk.learning.collector.LearningDataCollectorTask;
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
        plugin.getAfkManager().onPlayerJoin(event.getPlayer());
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        UUID playerUUID = player.getUniqueId();

        plugin.getAfkManager().onPlayerQuit(player);

        if (plugin.getBehaviorAnalysisManager() != null && plugin.getBehaviorAnalysisManager().isEnabled()) {
            plugin.getBehaviorAnalysisManager().removePlayerData(player);
        }

        plugin.getPlayersInChatInput().remove(playerUUID);
        plugin.getPlayerMenuUtilityMap().remove(playerUUID);
        plugin.getBookInputManager().ifPresent(manager -> manager.onPlayerQuit(player));

        RecordingManager recordingManager = plugin.getRecordingManager();
        if (recordingManager != null) {
            recordingManager.onPlayerQuit(player);
        }

        PatternAnalysisTask patternAnalysisTask = plugin.getPatternAnalysisTask();
        if (patternAnalysisTask != null) {
            patternAnalysisTask.onPlayerQuit(player);
        }

        plugin.getCaptchaManager().ifPresent(manager -> manager.onPlayerQuit(player));

        plugin.getConfigManager().clearPlayerCache(player);
        plugin.getPlayerStatsManager().onPlayerQuit(player);

        LearningDataCollectorTask collectorTask = plugin.getLearningDataCollectorTask();
        if (collectorTask != null) {
            collectorTask.onPlayerQuit(player);
        }
    }
}