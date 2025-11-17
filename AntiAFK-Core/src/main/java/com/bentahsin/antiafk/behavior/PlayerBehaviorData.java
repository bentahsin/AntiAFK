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
    }
}