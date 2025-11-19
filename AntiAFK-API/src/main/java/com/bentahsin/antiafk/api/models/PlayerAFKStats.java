package com.bentahsin.antiafk.api.models;

import java.util.UUID;

/**
 * Bir oyuncunun AntiAFK istatistiklerini taşıyan, değiştirilemez (immutable) veri sınıfı.
 */
public class PlayerAFKStats {

    private final UUID uuid;
    private final long totalAfkTime;
    private final int punishmentCount;
    private final int turingTestsPassed;
    private final int turingTestsFailed;

    public PlayerAFKStats(UUID uuid, long totalAfkTime, int punishmentCount, int turingTestsPassed, int turingTestsFailed) {
        this.uuid = uuid;
        this.totalAfkTime = totalAfkTime;
        this.punishmentCount = punishmentCount;
        this.turingTestsPassed = turingTestsPassed;
        this.turingTestsFailed = turingTestsFailed;
    }

    public UUID getUuid() {
        return uuid;
    }

    /**
     * Oyuncunun toplam AFK kaldığı süre (saniye cinsinden).
     */
    public long getTotalAfkTime() {
        return totalAfkTime;
    }

    /**
     * Oyuncunun aldığı toplam ceza sayısı.
     */
    public int getPunishmentCount() {
        return punishmentCount;
    }

    /**
     * Başarıyla geçilen bot testi sayısı.
     */
    public int getTuringTestsPassed() {
        return turingTestsPassed;
    }

    /**
     * Başarısız olunan bot testi sayısı.
     */
    public int getTuringTestsFailed() {
        return turingTestsFailed;
    }
}