package com.bentahsin.antiafk.behavior;

import com.bentahsin.antiafk.AntiAFKPlugin;
import com.bentahsin.antiafk.language.Lang;
import com.bentahsin.antiafk.language.SystemLanguageManager;
import com.bentahsin.antiafk.managers.ConfigManager;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.PluginManager;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;

/**
 * Oyuncu davranışlarını analiz eden modülü yönetir.
 * Görevi, oyuncu verilerini depolamak, periyodik analiz görevini yönetmek
 * ve bellek sızıntılarını önlemek için oyuncu çıkışlarını dinlemektir.
 */
@Singleton
public class BehaviorAnalysisManager implements Listener {

    private final AntiAFKPlugin plugin;
    private final PluginManager pluginManager;
    private final Logger logger;
    private final SystemLanguageManager systemLanguageManager;
    private final BehaviorAnalysisTask analysisTask; // Artık 'new' ile oluşturmuyoruz.

    private final Map<UUID, PlayerBehaviorData> playerDataMap = new ConcurrentHashMap<>();
    private final int historySizeTicks;
    private final boolean enabled;

    @Inject
    public BehaviorAnalysisManager(
            AntiAFKPlugin plugin,
            SystemLanguageManager systemLanguageManager,
            ConfigManager configManager,
            BehaviorAnalysisTask analysisTask
    ) {
        this.plugin = plugin;
        this.pluginManager = plugin.getServer().getPluginManager();
        this.logger = plugin.getLogger();
        this.systemLanguageManager = systemLanguageManager;
        this.analysisTask = analysisTask;
        this.historySizeTicks = configManager.getBehaviorHistorySizeTicks();
        this.enabled = configManager.isBehaviorAnalysisEnabled();

        if (enabled) {
            initialize();
        }
    }

    /**
     * Modül etkinse, analiz görevini başlatır ve olay dinleyicisini kaydeder.
     */
    private void initialize() {
        logger.info(systemLanguageManager.getSystemMessage(Lang.BEHAVIOR_ANALYSIS_ENABLED_AND_INIT));

        analysisTask.runTaskTimerAsynchronously(plugin, 100L, 5L);

        pluginManager.registerEvents(this, plugin);
    }

    public void shutdown() {
        if (!enabled) {
            return;
        }

        if (analysisTask != null && !analysisTask.isCancelled()) {
            analysisTask.cancel();
            logger.info(systemLanguageManager.getSystemMessage(Lang.BEHAVIOR_ANALYSIS_TASK_STOPPED));
        }
        playerDataMap.clear();
        logger.info(systemLanguageManager.getSystemMessage(Lang.BEHAVIOR_ANALYSIS_DATA_CLEARED));
    }

    public PlayerBehaviorData getPlayerData(Player player) {
        return playerDataMap.computeIfAbsent(player.getUniqueId(), uuid -> new PlayerBehaviorData());
    }

    public void removePlayerData(Player player) {
        playerDataMap.remove(player.getUniqueId());
    }

    public boolean isEnabled() {
        return enabled;
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        removePlayerData(event.getPlayer());
    }

    public int getHistorySizeTicks() {
        return historySizeTicks;
    }
}