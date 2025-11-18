package com.bentahsin.antiafk.api.implementation;

import com.bentahsin.antiafk.api.AntiAFKAPI;
import com.bentahsin.antiafk.managers.AFKManager;
import com.bentahsin.antiafk.managers.PlayerStateManager;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import org.bukkit.entity.Player;

@Singleton
public class AntiAFKAPIImpl implements AntiAFKAPI {

    private final AFKManager afkManager;
    private final PlayerStateManager stateManager;

    @Inject
    public AntiAFKAPIImpl(AFKManager afkManager, PlayerStateManager stateManager) {
        this.afkManager = afkManager;
        this.stateManager = stateManager;
    }

    @Override
    public boolean isAfk(Player player) {
        return stateManager.isEffectivelyAfk(player);
    }

    @Override
    public long getAfkTime(Player player) {
        return stateManager.getAfkTime(player);
    }

    @Override
    public void setAfk(Player player, String reason) {
        stateManager.setManualAFK(player, reason);
    }

    @Override
    public void setActive(Player player) {
        stateManager.unsetAfkStatus(player);
    }
}