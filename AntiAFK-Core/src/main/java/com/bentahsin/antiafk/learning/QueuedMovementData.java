package com.bentahsin.antiafk.learning;

import com.bentahsin.antiafk.api.learning.MovementVector;

import java.util.UUID;

/**
 * Bir MovementVector nesnesi oluşturmak için gereken ham verileri tutar.
 * Bu, ana iş parçacığında nesne oluşturma maliyetini ortadan kaldırır.
 */
public final class QueuedMovementData {
    public final UUID playerUuid;
    public final double deltaX, deltaZ, deltaYaw, deltaPitch;
    public final MovementVector.PlayerAction action;
    public final int durationTicks;

    public QueuedMovementData(UUID playerUuid, double dX, double dZ, double dYaw, double dPitch, MovementVector.PlayerAction action, int durationTicks) {
        this.playerUuid = playerUuid;
        this.deltaX = dX;
        this.deltaZ = dZ;
        this.deltaYaw = dYaw;
        this.deltaPitch = dPitch;
        this.action = action;
        this.durationTicks = durationTicks;
    }
}