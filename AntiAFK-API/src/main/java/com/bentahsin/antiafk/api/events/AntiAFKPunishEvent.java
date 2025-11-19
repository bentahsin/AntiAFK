package com.bentahsin.antiafk.api.events;

import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

/**
 * Bir oyuncuya AFK cezası (kick, komut vb.) uygulanmadan hemen önce tetiklenir.
 * İptal edilirse ceza uygulanmaz.
 */
@SuppressWarnings("unused")
public class AntiAFKPunishEvent extends Event implements Cancellable {

    private static final HandlerList handlers = new HandlerList();
    private final Player player;
    private final String punishmentType;
    private boolean cancelled;

    public AntiAFKPunishEvent(Player player, String punishmentType) {
        this.player = player;
        this.punishmentType = punishmentType;
    }

    public Player getPlayer() {
        return player;
    }

    public String getPunishmentType() {
        return punishmentType;
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