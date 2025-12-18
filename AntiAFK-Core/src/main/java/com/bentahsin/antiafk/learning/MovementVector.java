package com.bentahsin.antiafk.learning;

import com.bentahsin.antiafk.learning.util.CustomVector;
import java.io.Serializable;

public final class MovementVector implements Serializable {

    public enum PlayerAction {
        NONE, JUMP, SNEAK_ON, SNEAK_OFF, IDLE
    }

    private CustomVector positionChange;
    private CustomVector rotationChange;
    private PlayerAction action;
    private int durationTicks;

    private MovementVector() {
        this.positionChange = new CustomVector();
        this.rotationChange = new CustomVector();
    }

    public MovementVector(double dx, double dz, double dYaw, double dPitch, PlayerAction action, int durationTicks) {
        this.positionChange = new CustomVector(dx, dz);
        this.rotationChange = new CustomVector(dYaw, dPitch);
        this.action = action;
        this.durationTicks = durationTicks;
    }

    public void reinitialize(double dx, double dz, double dYaw, double dPitch, PlayerAction action, int durationTicks) {
        this.positionChange.set(dx, dz);
        this.rotationChange.set(dYaw, dPitch);
        this.action = action;
        this.durationTicks = durationTicks;
    }

    public CustomVector getPositionChange() { return positionChange; }
    public CustomVector getRotationChange() { return rotationChange; }
    public PlayerAction getAction() { return action; }
    public int getDurationTicks() { return durationTicks; }
}