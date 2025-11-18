package com.bentahsin.antiafk.storage;

import com.bentahsin.antiafk.AntiAFKPlugin;
import com.bentahsin.antiafk.managers.DebugManager;
import com.bentahsin.antiafk.models.PlayerStats;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import org.bukkit.entity.Player;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

/**
 * Oyuncu istatistiklerini (PlayerStats) veritabanı ve bir önbellek (cache) katmanı
 * arasında yönetir. Bu, veritabanına yapılan okuma isteklerini büyük ölçüde azaltır.
 */
@Singleton
public class PlayerStatsManager {

    private final DatabaseManager databaseManager;
    private final DebugManager debugMgr;

    private final Cache<UUID, PlayerStats> statsCache;

    @Inject
    public PlayerStatsManager(AntiAFKPlugin plugin, DatabaseManager databaseManager, DebugManager debugManager) {
        this.databaseManager = databaseManager;
        this.debugMgr = debugManager;

        this.statsCache = Caffeine.newBuilder()
                .maximumSize(1000)
                .expireAfterAccess(15, TimeUnit.MINUTES)
                .build();
    }

    /**
     * Bir oyuncunun istatistiklerini önbellekten veya gerekirse veritabanından getirir.
     * Bu metot asenkron olarak çalışır.
     *
     * @param uuid Oyuncunun UUID'si.
     * @param username Oyuncunun kullanıcı adı (eğer veritabanında yoksa oluşturmak için).
     * @return Oyuncunun istatistiklerini içeren bir CompletableFuture.
     */
    public CompletableFuture<PlayerStats> getPlayerStats(UUID uuid, String username) {
        PlayerStats cachedStats = statsCache.getIfPresent(uuid);
        if (cachedStats != null) {
            debugMgr.log(DebugManager.DebugModule.DATABASE_QUERIES,
                    "Cache HIT for player stats: %s", username
            );
            return CompletableFuture.completedFuture(cachedStats);
        }

        debugMgr.log(DebugManager.DebugModule.DATABASE_QUERIES,
                "Cache MISS for player stats: %s. Querying database...", username
        );

        return CompletableFuture.supplyAsync(() -> {
            PlayerStats dbStats = databaseManager.getPlayerStats(uuid, username);
            statsCache.put(uuid, dbStats);

            debugMgr.log(DebugManager.DebugModule.DATABASE_QUERIES,
                    "Fetched and cached stats for %s from database.", username
            );
            return dbStats;
        });
    }

    /**
     * Bir oyuncunun önbellekteki verilerini geçersiz kılar.
     * Bu, veritabanında bir güncelleme yapıldıktan sonra çağrılmalıdır.
     * @param uuid Verisi geçersiz kılınacak oyuncu.
     */
    public void invalidateCache(UUID uuid) {
        debugMgr.log(DebugManager.DebugModule.DATABASE_QUERIES,
                "Invalidating stats cache for UUID: %s", uuid.toString()
        );
        statsCache.invalidate(uuid);
    }

    public void onPlayerQuit(Player player) {
        invalidateCache(player.getUniqueId());
    }
}