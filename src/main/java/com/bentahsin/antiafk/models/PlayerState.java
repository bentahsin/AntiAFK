package com.bentahsin.antiafk.models;

import java.util.UUID;

public class PlayerState {
    private final UUID uuid;
    private final String originalDisplayName;

    private boolean manualAfk;
    private boolean autoAfk;
    private boolean autonomous;
    private boolean systemPunished;
    private boolean suspicious;
    private String afkReason;

    public PlayerState(UUID uuid, String originalDisplayName) {
        this.uuid = uuid;
        this.originalDisplayName = originalDisplayName;
    }

    public UUID getUuid() {
        return uuid;
    }

    public String getOriginalDisplayName() {
        return originalDisplayName;
    }

    public boolean isManualAfk() {
        return manualAfk;
    }

    public void setManualAfk(boolean manualAfk) {
        this.manualAfk = manualAfk;
    }

    @SuppressWarnings("unused")
    public boolean isAutoAfk() {
        return autoAfk;
    }

    public void setAutoAfk(boolean autoAfk) {
        this.autoAfk = autoAfk;
    }

    public boolean isAutonomous() {
        return autonomous;
    }

    public void setAutonomous(boolean autonomous) {
        this.autonomous = autonomous;
    }

    @SuppressWarnings("unused")
    public boolean isSystemPunished() {
        return systemPunished;
    }

    public void setSystemPunished(boolean systemPunished) {
        this.systemPunished = systemPunished;
    }

    public boolean isSuspicious() {
        return suspicious;
    }

    public void setSuspicious(boolean suspicious) {
        this.suspicious = suspicious;
    }

    public String getAfkReason() {
        return afkReason;
    }

    public void setAfkReason(String afkReason) {
        this.afkReason = afkReason;
    }

    public boolean isEffectivelyAfk() {
        return manualAfk || autoAfk || systemPunished;
    }
}