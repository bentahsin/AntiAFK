package com.bentahsin.antiafk.models;

import java.util.UUID;

/**
 * Bir oyuncunun veritabanında saklanan tüm sabıka ve istatistik verilerini
 * temsil eden, Java 8 uyumlu, değişmez (immutable) bir veri sınıfı.
 */
public final class PlayerStats {

    private final UUID uuid;
    private final String username;
    private final long totalAfkTime;
    private final int timesPunished;
    private final long lastPunishmentTime;
    private final int turingTestsPassed;
    private final int turingTestsFailed;
    private final String mostFrequentReason;

    public PlayerStats(UUID uuid, String username, long totalAfkTime, int timesPunished,
                       long lastPunishmentTime, int turingTestsPassed, int turingTestsFailed, String mostFrequentReason) {
        this.uuid = uuid;
        this.username = username;
        this.totalAfkTime = totalAfkTime;
        this.timesPunished = timesPunished;
        this.lastPunishmentTime = lastPunishmentTime;
        this.turingTestsPassed = turingTestsPassed;
        this.turingTestsFailed = turingTestsFailed;
        this.mostFrequentReason = mostFrequentReason;
    }


    public UUID getUuid() { return uuid; }
    public String getUsername() { return username; }
    public long getTotalAfkTime() { return totalAfkTime; }
    public int getTimesPunished() { return timesPunished; }
    public long getLastPunishmentTime() { return lastPunishmentTime; }
    public int getTuringTestsPassed() { return turingTestsPassed; }
    public int getTuringTestsFailed() { return turingTestsFailed; }
    public String getMostFrequentReason() { return mostFrequentReason; }
    public static PlayerStats createDefault(UUID uuid, String username) {
        return new PlayerStats(uuid, username, 0, 0, 0, 0, 0, "YOK");
    }
}