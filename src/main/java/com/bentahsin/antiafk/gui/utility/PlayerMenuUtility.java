package com.bentahsin.antiafk.gui.utility;

import org.bukkit.entity.Player;

import java.util.UUID;

public class PlayerMenuUtility {
    private final Player owner;
    private UUID targetPlayerUUID;
    private int lastPlayerListPage = 0;
    private String regionToEdit;
    private int actionIndexToEdit = -1;

    public PlayerMenuUtility(Player owner) {
        this.owner = owner;
    }

    public Player getOwner() {
        return owner;
    }

    public UUID getTargetPlayerUUID() {
        return targetPlayerUUID;
    }

    public void setTargetPlayerUUID(UUID targetPlayerUUID) {
        this.targetPlayerUUID = targetPlayerUUID;
    }

    public int getLastPlayerListPage() {
        return lastPlayerListPage;
    }

    public void setLastPlayerListPage(int lastPlayerListPage) {
        this.lastPlayerListPage = lastPlayerListPage;
    }

    public String getRegionToEdit() {
        return regionToEdit;
    }

    public void setRegionToEdit(String regionToEdit) {
        this.regionToEdit = regionToEdit;
    }

    public int getActionIndexToEdit() {
        return actionIndexToEdit;
    }

    public void setActionIndexToEdit(int actionIndexToEdit) {
        this.actionIndexToEdit = actionIndexToEdit;
    }
}