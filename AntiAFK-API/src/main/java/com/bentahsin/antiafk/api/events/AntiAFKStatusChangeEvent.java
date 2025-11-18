package com.bentahsin.antiafk.api.events;

import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

/**
 * Bir oyuncunun AFK durumu değiştiğinde (AFK oldu veya geri döndü) tetiklenir.
 */
public class AntiAFKStatusChangeEvent extends Event implements Cancellable {

    private static final HandlerList handlers = new HandlerList();
    private final Player player;
    private final boolean isAfk;
    private final String reason;
    private boolean cancelled;

    public AntiAFKStatusChangeEvent(Player player, boolean isAfk, String reason) {
        this.player = player;
        this.isAfk = isAfk;
        this.reason = reason;
    }

    public Player getPlayer() {
        return player;
    }

    /**
     * @return Oyuncu AFK moduna giriyorsa true, çıkıyorsa false.
     */
    public boolean isAfk() {
        return isAfk;
    }

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

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }
}