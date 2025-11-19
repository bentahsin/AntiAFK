package com.bentahsin.antiafk.api.events;

import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

/**
 * Bir oyuncuya AFK uyarısı gönderilmeden önce tetiklenir.
 * İptal edilirse uyarı gönderilmez.
 */
@SuppressWarnings("unused")
public class AntiAFKWarningEvent extends Event implements Cancellable {

    private static final HandlerList handlers = new HandlerList();
    private final Player player;
    private final long timeLeftSeconds;
    private final long maxAfkTimeSeconds;
    private boolean cancelled;

    public AntiAFKWarningEvent(Player player, long timeLeftSeconds, long maxAfkTimeSeconds) {
        this.player = player;
        this.timeLeftSeconds = timeLeftSeconds;
        this.maxAfkTimeSeconds = maxAfkTimeSeconds;
    }

    public Player getPlayer() {
        return player;
    }

    /**
     * Cezaya kalan süreyi saniye cinsinden döndürür.
     */
    public long getTimeLeftSeconds() {
        return timeLeftSeconds;
    }

    public long getMaxAfkTimeSeconds() {
        return maxAfkTimeSeconds;
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