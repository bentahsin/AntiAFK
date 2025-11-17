package com.bentahsin.antiafk.learning.collector;

import com.bentahsin.antiafk.learning.MovementVector;
import org.bukkit.Location;

/**
 * Bir oyuncunun anlık öğrenme verilerini tutar.
 */
public class LearningData {
    public boolean skip = false;

    public Location lastLocation;

    public int ticksInCurrentState = 0;

    public MovementVector.PlayerAction currentState = MovementVector.PlayerAction.IDLE;
}