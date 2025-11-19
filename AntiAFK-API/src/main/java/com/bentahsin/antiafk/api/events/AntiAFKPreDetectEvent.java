package com.bentahsin.antiafk.api.events;

import com.bentahsin.antiafk.api.enums.DetectionType;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

/**
 * AntiAFK bir oyuncudan şüphelendiğinde ve işlem yapmadan (Captcha/AFK) HEMEN ÖNCE tetiklenir.
 * Eğer bu olay iptal edilirse, AntiAFK oyuncuyu görmezden gelir.
 */
@SuppressWarnings("unused")
public class AntiAFKPreDetectEvent extends Event implements Cancellable {

    private static final HandlerList handlers = new HandlerList();
    private final Player player;
    private final DetectionType detectionType;
    private final String debugReason;
    private boolean cancelled;

    public AntiAFKPreDetectEvent(Player player, DetectionType detectionType, String debugReason) {
        this.player = player;
        this.detectionType = detectionType;
        this.debugReason = debugReason;
    }

    public Player getPlayer() {
        return player;
    }

    public DetectionType getDetectionType() {
        return detectionType;
    }

    /**
     * Loglarda görünecek olan detaylı sebep.
     */
    public String getDebugReason() {
        return debugReason;
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