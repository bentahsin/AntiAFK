package com.bentahsin.antiafk.behavior;

import org.bukkit.Location;
import java.util.LinkedList;
import java.util.Objects;

public class PlayerBehaviorData {

    private final LinkedList<Location> movementHistory = new LinkedList<>();

    private volatile Location confinementStartLocation;
    private volatile long confinementStartTime;
    private volatile double totalDistanceTraveled = 0.0;
    private Location lastMoveLocation;

    private volatile long lastRepeatTimestamp;
    private volatile int consecutiveRepeatCount = 0;

    public synchronized void processMovement(Location current, double maxRadius) {
        long now = System.currentTimeMillis();

        if (confinementStartLocation == null) {
            resetConfinement(current, now);
            return;
        }

        if (!isInsideRadius(current, confinementStartLocation, maxRadius)) {
            resetConfinement(current, now);
            return;
        }

        if (lastMoveLocation != null && Objects.equals(lastMoveLocation.getWorld(), current.getWorld())) {
            totalDistanceTraveled += current.distance(lastMoveLocation);
        }

        lastMoveLocation = current;
    }

    private void resetConfinement(Location current, long timestamp) {
        this.confinementStartLocation = current;
        this.lastMoveLocation = current;
        this.confinementStartTime = timestamp;
        this.totalDistanceTraveled = 0.0;
    }

    private boolean isInsideRadius(Location loc1, Location loc2, double radius) {
        if (!Objects.equals(loc1.getWorld(), loc2.getWorld())) return false;
        return loc1.distanceSquared(loc2) <= (radius * radius);
    }

    public synchronized long getConfinementDuration() {
        return (confinementStartLocation == null) ? 0 : System.currentTimeMillis() - confinementStartTime;
    }

    public synchronized double getTotalDistanceTraveled() {
        return totalDistanceTraveled;
    }

    public LinkedList<Location> getMovementHistory() {
        return movementHistory;
    }

    public long getLastRepeatTimestamp() { return lastRepeatTimestamp; }
    public void setLastRepeatTimestamp(long lastRepeatTimestamp) { this.lastRepeatTimestamp = lastRepeatTimestamp; }

    public int getConsecutiveRepeatCount() { return consecutiveRepeatCount; }
    public void setConsecutiveRepeatCount(int consecutiveRepeatCount) { this.consecutiveRepeatCount = consecutiveRepeatCount; }

    public synchronized void reset() {
        this.movementHistory.clear();
        this.lastRepeatTimestamp = 0;
        this.consecutiveRepeatCount = 0;
        this.confinementStartLocation = null;
        this.totalDistanceTraveled = 0;
        this.lastMoveLocation = null;
    }
}