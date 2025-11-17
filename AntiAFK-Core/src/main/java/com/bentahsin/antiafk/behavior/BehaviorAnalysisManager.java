package com.bentahsin.antiafk.behavior;

import com.bentahsin.antiafk.AntiAFKPlugin;
import com.bentahsin.antiafk.language.Lang;
import com.bentahsin.antiafk.language.SystemLanguageManager;
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
public class BehaviorAnalysisManager implements Listener {

    private final AntiAFKPlugin plugin;
    private final PluginManager plMgr;
    private final Logger logger;
    private final SystemLanguageManager sysLang;

    private final Map<UUID, PlayerBehaviorData> playerDataMap = new ConcurrentHashMap<>();
    private final int historySizeTicks;

    private BehaviorAnalysisTask analysisTask;
    private final boolean enabled;

    public BehaviorAnalysisManager(AntiAFKPlugin plugin) {
        this.plugin = plugin;
        this.plMgr = plugin.getServer().getPluginManager();
        this.logger = plugin.getLogger();
        this.sysLang = plugin.getSystemLanguageManager();

        this.historySizeTicks = plugin.getConfig().getInt("behavioral-analysis.history-size-ticks", 600);
        this.enabled = plugin.getConfig().getBoolean("behavioral-analysis.enabled", false);

        if (enabled) {
            initialize();
        }
    }

    /**
     * Modül etkinse, analiz görevini başlatır ve olay dinleyicisini kaydeder.
     */
    private void initialize() {
        logger.info(sysLang.getSystemMessage(Lang.BEHAVIOR_ANALYSIS_ENABLED_AND_INIT));

        analysisTask = new BehaviorAnalysisTask(plugin, this);
        analysisTask.runTaskTimerAsynchronously(plugin, 100L, 5L);

        plMgr.registerEvents(this, plugin);
    }

    /**
     * Eklenti devre dışı bırakıldığında çağrılır.
     * Aktif görevi durdurur ve kaynakları temizler.
     */
    public void shutdown() {
        if (!enabled) {
            return;
        }

        if (analysisTask != null && !analysisTask.isCancelled()) {
            analysisTask.cancel();
            logger.info(sysLang.getSystemMessage(Lang.BEHAVIOR_ANALYSIS_TASK_STOPPED));
        }
        playerDataMap.clear();
        logger.info(sysLang.getSystemMessage(Lang.BEHAVIOR_ANALYSIS_DATA_CLEARED));
    }

    /**
     * Bir oyuncuya ait davranış verilerini döndürür.
     * Eğer oyuncu için veri mevcut değilse, yeni bir tane oluşturur ve haritaya ekler.
     * @param player Verisi alınacak oyuncu.
     * @return Oyuncunun PlayerBehaviorData nesnesi.
     */
    public PlayerBehaviorData getPlayerData(Player player) {

        return playerDataMap.computeIfAbsent(player.getUniqueId(), uuid -> new PlayerBehaviorData());
    }

    /**
     * Bir oyuncunun davranış verilerini haritadan kaldırır.
     * @param player Verisi kaldırılacak oyuncu.
     */
    public void removePlayerData(Player player) {
        playerDataMap.remove(player.getUniqueId());
    }

    /**
     * Davranışsal analiz modülünün aktif olup olmadığını döndürür.
     * @return Modül aktifse true, değilse false.
     */
    public boolean isEnabled() {
        return enabled;
    }

    /**
     * Bir oyuncu sunucudan ayrıldığında, onunla ilgili tüm verileri temizleyerek
     * bellek sızıntısını (memory leak) önler.
     * @param event PlayerQuitEvent
     */
    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        removePlayerData(event.getPlayer());
    }
    /**
     * Hareket geçmişinde saklanacak maksimum tick sayısını döndürür.
     * @return Hareket geçmişi boyutu (tick cinsinden).
     */

    public int getHistorySizeTicks() {
        return historySizeTicks;
    }
}