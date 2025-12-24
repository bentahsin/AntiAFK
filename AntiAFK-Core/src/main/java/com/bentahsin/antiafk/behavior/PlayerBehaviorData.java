package com.bentahsin.antiafk.behavior;

import org.bukkit.Location;

import java.util.LinkedList;

/**
 * Her oyuncu için davranış analizi verilerini tutan veri sınıfı.
 * Bu sınıf, oyuncunun hareket geçmişini ve tespit edilen otonom hareket
 * tekrarlarını depolamak için kullanılır.
 */
public class PlayerBehaviorData {

    /**
     * Oyuncunun son 'X' saniyelik tüm anlamlı hareketlerini tutan ana veri havuzu.
     * LinkedList, listenin başından (en eski veri) eleman silme işlemlerinde
     * (FIFO - First-In, First-Out) ArrayList'e göre daha performanslıdır.
     */
    private final LinkedList<Location> movementHistory = new LinkedList<>();

    private Location confinementStartLocation;
    private long confinementStartTime;
    private double totalDistanceTraveled = 0.0;

    public void updateConfinement(Location current, double confinementRadius) {
        if (confinementStartLocation == null) {
            resetConfinement(current);
            return;
        }

        if (!isInsideRadius(current, confinementStartLocation, confinementRadius)) {
            resetConfinement(current);
            return;
        }

        double distance = current.distance(confinementStartLocation);
    }

    private Location lastMoveLocation;

    public void processMovement(Location current, double maxRadius) {
        long now = System.currentTimeMillis();

        if (confinementStartLocation == null) {
            confinementStartLocation = current;
            lastMoveLocation = current;
            confinementStartTime = now;
            totalDistanceTraveled = 0;
            return;
        }

        if (current.getWorld() != confinementStartLocation.getWorld() ||
                current.distanceSquared(confinementStartLocation) > (maxRadius * maxRadius)) {
            resetConfinement(current);
            return;
        }

        if (lastMoveLocation != null && lastMoveLocation.getWorld() == current.getWorld()) {
            totalDistanceTraveled += current.distance(lastMoveLocation);
        }

        lastMoveLocation = current;
    }

    private void resetConfinement(Location current) {
        this.confinementStartLocation = current;
        this.lastMoveLocation = current;
        this.confinementStartTime = System.currentTimeMillis();
        this.totalDistanceTraveled = 0.0;
    }

    private boolean isInsideRadius(Location loc1, Location loc2, double radius) {
        if (loc1.getWorld() != loc2.getWorld()) return false;
        return loc1.distanceSquared(loc2) <= (radius * radius);
    }

    public long getConfinementDuration() {
        return (confinementStartLocation == null) ? 0 : System.currentTimeMillis() - confinementStartTime;
    }

    public double getTotalDistanceTraveled() {
        return totalDistanceTraveled;
    }

    /**
     * Tespit edilen son tekrarın ne zaman gerçekleştiğini milisaniye cinsinden tutar.
     * Bu, tekrarların ardışık olup olmadığını anlamak için kullanılır.
     */
    private long lastRepeatTimestamp;

    /**
     * Birbirini takip eden (ardışık) tekrar sayısını tutar.
     * Oyuncu kalıbı bozduğunda bu sayaç sıfırlanır.
     */
    private int consecutiveRepeatCount = 0;

    public LinkedList<Location> getMovementHistory() {
        return movementHistory;
    }

    public long getLastRepeatTimestamp() {
        return lastRepeatTimestamp;
    }

    public void setLastRepeatTimestamp(long lastRepeatTimestamp) {
        this.lastRepeatTimestamp = lastRepeatTimestamp;
    }

    public int getConsecutiveRepeatCount() {
        return consecutiveRepeatCount;
    }

    public void setConsecutiveRepeatCount(int consecutiveRepeatCount) {
        this.consecutiveRepeatCount = consecutiveRepeatCount;
    }

    /**
     * Oyuncunun tüm analiz verilerini sıfırlar.
     * Bu metot, oyuncu AFK olarak işaretlendiğinde veya bilinçli bir aktivite
     * (sohbet, envanter açma vb.) göstererek kalıbı bozduğunda çağrılır.
     */
    public void reset() {
        this.movementHistory.clear();
        this.lastRepeatTimestamp = 0;
        this.consecutiveRepeatCount = 0;
        this.confinementStartLocation = null;
        this.totalDistanceTraveled = 0;
        this.lastMoveLocation = null;
    }
}