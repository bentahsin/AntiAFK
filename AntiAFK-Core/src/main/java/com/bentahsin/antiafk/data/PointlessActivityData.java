package com.bentahsin.antiafk.data;

import org.bukkit.Location;
import java.util.ArrayList;
import java.util.List;

/**
 * Bir oyuncunun, bot benzeri davranışlarını (anlamsız tekrarlar, auto-clicker)
 * tespit etmek için gerekli verileri tutar.
 */
public class PointlessActivityData {

    private Location lastPlayerLocation;
    private Location lastInteractedBlockLocation;
    private int pointlessActivityCounter;

    private long lastClickTime;
    private final List<Long> clickIntervals = new ArrayList<>();
    private int autoClickerDetections;

    /**
     * Boş bir veri nesnesi oluşturur.
     * Tüm sayaçlar ve zaman damgaları varsayılan olarak 0'dır.
     */
    public PointlessActivityData() {
        this.pointlessActivityCounter = 0;
        this.lastClickTime = 0;
        this.autoClickerDetections = 0;
    }

    public Location getLastPlayerLocation() {
        return lastPlayerLocation;
    }

    public Location getLastInteractedBlockLocation() {
        return lastInteractedBlockLocation;
    }

    public int getPointlessActivityCounter() {
        return pointlessActivityCounter;
    }

    public void incrementPointlessActivityCounter() {
        this.pointlessActivityCounter++;
    }

    /**
     * Anlamsız aktivite sayacını sıfırlar ve yeni konumları ayarlar.
     * @param playerLocation Yeni aktivitede oyuncunun konumu.
     * @param interactedBlockLocation Yeni aktivitede etkileşimde bulunulan bloğun konumu (olmayabilir, null olabilir).
     */
    public void resetAndSetPointlessActivity(Location playerLocation, Location interactedBlockLocation) {
        this.lastPlayerLocation = playerLocation;
        this.lastInteractedBlockLocation = interactedBlockLocation;
        this.pointlessActivityCounter = 1;
    }

    /**
     * Sadece anlamsız aktivite verilerini sıfırlar (oyuncu hareket ettiğinde).
     */
    public void resetPointlessActivityData() {
        this.lastPlayerLocation = null;
        this.lastInteractedBlockLocation = null;
        this.pointlessActivityCounter = 0;
    }

    public long getLastClickTime() {
        return lastClickTime;
    }

    public void setLastClickTime(long lastClickTime) {
        this.lastClickTime = lastClickTime;
    }

    public List<Long> getClickIntervals() {
        return clickIntervals;
    }

    public int getAutoClickerDetections() {
        return autoClickerDetections;
    }

    public void incrementAutoClickerDetections() {
        this.autoClickerDetections++;
    }

    public void resetAutoClickerDetections() {
        this.autoClickerDetections = 0;
    }
}