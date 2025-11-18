package com.bentahsin.antiafk.listeners.handlers;

import com.bentahsin.antiafk.AntiAFKPlugin;
import com.bentahsin.antiafk.behavior.BehaviorAnalysisManager;
import com.bentahsin.antiafk.gui.book.BookInputManager;
import com.bentahsin.antiafk.learning.PatternAnalysisTask;
import com.bentahsin.antiafk.learning.RecordingManager;
import com.bentahsin.antiafk.learning.collector.LearningDataCollectorTask;
import com.bentahsin.antiafk.managers.AFKManager;
import com.bentahsin.antiafk.managers.ConfigManager;
import com.bentahsin.antiafk.storage.PlayerStatsManager;
import com.bentahsin.antiafk.turing.CaptchaManager;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.UUID;

/**
 * Oyuncu giriş ve çıkış olaylarını yönetir.
 * Oyuncu ayrıldığında tüm ilgili servislerdeki verilerini temizler.
 */
@Singleton
public class PlayerConnectionListener implements Listener {

    private final AntiAFKPlugin plugin;
    private final AFKManager afkManager;
    private final BehaviorAnalysisManager behaviorAnalysisManager;
    private final BookInputManager bookInputManager;
    private final RecordingManager recordingManager;
    private final PatternAnalysisTask patternAnalysisTask;
    private final CaptchaManager captchaManager;
    private final ConfigManager configManager;
    private final PlayerStatsManager playerStatsManager;
    private final LearningDataCollectorTask collectorTask;

    @Inject
    public PlayerConnectionListener(
            AntiAFKPlugin plugin,
            AFKManager afkManager,
            BehaviorAnalysisManager behaviorAnalysisManager,
            BookInputManager bookInputManager,
            RecordingManager recordingManager,
            PatternAnalysisTask patternAnalysisTask,
            CaptchaManager captchaManager,
            ConfigManager configManager,
            PlayerStatsManager playerStatsManager,
            LearningDataCollectorTask collectorTask
    ) {
        this.plugin = plugin;
        this.afkManager = afkManager;
        this.behaviorAnalysisManager = behaviorAnalysisManager;
        this.bookInputManager = bookInputManager;
        this.recordingManager = recordingManager;
        this.patternAnalysisTask = patternAnalysisTask;
        this.captchaManager = captchaManager;
        this.configManager = configManager;
        this.playerStatsManager = playerStatsManager;
        this.collectorTask = collectorTask;
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        afkManager.onPlayerJoin(event.getPlayer());
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        UUID playerUUID = player.getUniqueId();

        afkManager.onPlayerQuit(player);

        if (behaviorAnalysisManager != null && behaviorAnalysisManager.isEnabled()) {
            behaviorAnalysisManager.removePlayerData(player);
        }

        plugin.getPlayersInChatInput().remove(playerUUID);
        plugin.getPlayerMenuUtilityMap().remove(playerUUID);

        if (bookInputManager != null) {
            bookInputManager.onPlayerQuit(player);
        }

        if (recordingManager != null) {
            recordingManager.onPlayerQuit(player);
        }

        if (patternAnalysisTask != null) {
            patternAnalysisTask.onPlayerQuit(player);
        }

        if (captchaManager != null) {
            captchaManager.onPlayerQuit(player);
        }

        configManager.clearPlayerCache(player);
        playerStatsManager.onPlayerQuit(player);

        if (collectorTask != null) {
            collectorTask.onPlayerQuit(player);
        }
    }
}