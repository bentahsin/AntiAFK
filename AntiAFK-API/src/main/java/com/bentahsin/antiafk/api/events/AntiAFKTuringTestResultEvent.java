package com.bentahsin.antiafk.api.events;

import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

/**
 * Bir oyuncu Turing (Captcha) testini tamamladığında (başarılı veya başarısız) tetiklenir.
 */
@SuppressWarnings("unused")
public class AntiAFKTuringTestResultEvent extends Event {

    private static final HandlerList handlers = new HandlerList();
    private final Player player;
    private final Result result;

    public enum Result {
        PASSED,
        FAILED
    }

    public AntiAFKTuringTestResultEvent(Player player, Result result) {
        this.player = player;
        this.result = result;
    }

    public Player getPlayer() {
        return player;
    }

    public Result getResult() {
        return result;
    }

    public boolean hasPassed() {
        return result == Result.PASSED;
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