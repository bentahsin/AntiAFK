package com.bentahsin.antiafk.managers;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

class RejoinLogicTest {

    @Test
    @DisplayName("Rejoin Koruması: Süre Dolmadan Giriş Engellenmeli")
    void testRejoinCooldown() throws InterruptedException {
        long cooldownSeconds = 2;
        UUID playerUUID = UUID.randomUUID();

        Cache<UUID, Long> rejoinCache = Caffeine.newBuilder()
                .expireAfterWrite(cooldownSeconds * 2, TimeUnit.SECONDS)
                .build();

        long punishTime = System.currentTimeMillis();
        rejoinCache.put(playerUUID, punishTime);

        Thread.sleep(1000);

        long currentTime = System.currentTimeMillis();
        long timePassed = (currentTime - punishTime) / 1000;

        boolean shouldKick = timePassed < cooldownSeconds;

        assertTrue(shouldKick, "Oyuncu süresi dolmadan girmeye çalıştı ama sistem engellemedi!");

        Thread.sleep(2000);

        currentTime = System.currentTimeMillis();
        timePassed = (currentTime - punishTime) / 1000;

        shouldKick = timePassed < cooldownSeconds;

        assertFalse(shouldKick, "Süre dolmasına rağmen oyuncu hala engelleniyor!");
    }

    @Test
    @DisplayName("Cache Temizliği: Süresi Dolan Veri Silinmeli")
    void testCacheExpiration() throws InterruptedException {
        Cache<UUID, Long> cache = Caffeine.newBuilder()
                .expireAfterWrite(100, TimeUnit.MILLISECONDS)
                .build();

        UUID uuid = UUID.randomUUID();
        cache.put(uuid, System.currentTimeMillis());

        assertNotNull(cache.getIfPresent(uuid));

        Thread.sleep(200);

        cache.cleanUp();
        assertNull(cache.getIfPresent(uuid), "Cache süresi dolan veriyi silmedi!");
    }
}