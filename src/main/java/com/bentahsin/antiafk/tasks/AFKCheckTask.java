package com.bentahsin.antiafk.tasks;

import com.bentahsin.antiafk.AntiAFKPlugin;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Oyuncuların AFK durumlarını periyodik olarak kontrol eden, optimize edilmiş görev.
 * Bu görev, sunucuya minimum yük bindirmek için tasarlanmıştır.
 * <p>
 * Optimizasyon Stratejileri:
 * 1. Online oyuncu listesi, her tick'te yeniden oluşturulmak yerine saniyede sadece bir kez
 *    güncellenen bir önbellekte (cache) tutulur. Bu, 'new ArrayList' çağrılarından
 *    kaynaklanan CPU ve bellek kullanımını %95 oranında azaltır.
 * 2. Oyuncu kontrol yükü, 1 saniyelik (20 tick) zaman dilimine eşit olarak dağıtılır.
 *    Bu, ani performans düşüşlerini (spikes) engeller.
 */
public class AFKCheckTask extends BukkitRunnable {

    private final AntiAFKPlugin plugin;
    private int playerIndex = 0;

    private List<Player> onlinePlayersCache;
    private int tickCounter;
    private static final int UPDATE_INTERVAL_TICKS = 20;

    public AFKCheckTask(AntiAFKPlugin plugin) {
        this.plugin = plugin;
        this.onlinePlayersCache = Collections.emptyList();
        this.tickCounter = 0;
    }

    @Override
    public void run() {
        tickCounter++;

        if (tickCounter >= UPDATE_INTERVAL_TICKS) {
            tickCounter = 0;

            this.onlinePlayersCache = new ArrayList<>(Bukkit.getOnlinePlayers());

            if (!onlinePlayersCache.isEmpty() && playerIndex >= onlinePlayersCache.size()) {
                playerIndex = 0;
            }
        }

        if (onlinePlayersCache.isEmpty()) {
            return;
        }

        int playersToCheck = Math.max(1, onlinePlayersCache.size() / UPDATE_INTERVAL_TICKS);

        for (int i = 0; i < playersToCheck; i++) {
            if (playerIndex >= onlinePlayersCache.size()) {
                playerIndex = 0;
            }

            Player player = onlinePlayersCache.get(playerIndex);

            plugin.getAfkManager().checkPlayer(player);
            playerIndex++;
        }
    }
}