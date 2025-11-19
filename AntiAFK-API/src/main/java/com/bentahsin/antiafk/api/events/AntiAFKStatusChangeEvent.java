package com.bentahsin.antiafk.api.events;

import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

/**
 * Bir oyuncunun AFK durumu değiştiğinde (AFK oldu veya geri döndü) tetiklenir.
 * Bu olay iptal edilebilir (Cancellable).
 */
@SuppressWarnings("unused")
public class AntiAFKStatusChangeEvent extends Event implements Cancellable {

    private static final HandlerList handlers = new HandlerList();
    private final Player player;
    private final boolean isAfk;
    private final String reason;
    private boolean cancelled;

    /**
     * @param player Durumu değişen oyuncu.
     * @param isAfk  True ise oyuncu AFK oluyor, False ise AFK'dan çıkıyor.
     * @param reason AFK olma sebebi (AFK'dan çıkarken null olabilir).
     */
    public AntiAFKStatusChangeEvent(Player player, boolean isAfk, String reason) {
        this.player = player;
        this.isAfk = isAfk;
        this.reason = reason;
    }

    public Player getPlayer() {
        return player;
    }

    /**
     * Oyuncunun yeni durumunu döndürür.
     * @return Oyuncu AFK moduna giriyorsa true, çıkıyorsa false.
     */
    public boolean isBecomingAfk() {
        return isAfk;
    }

    /**
     * Eğer oyuncu AFK oluyorsa sebebini döndürür.
     * @return Sebep metni veya null.
     */
    public String getReason() {
        return reason;
    }

    @Override
    public boolean isCancelled() {
        return cancelled;
    }

    @Override
    public void setCancelled(boolean cancel) {
        this.cancelled = cancel;
    }

    @NotNull
    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }
}