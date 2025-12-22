package com.bentahsin.antiafk.api.implementation;

import com.bentahsin.antiafk.api.learning.MovementVector;
import com.bentahsin.antiafk.api.managers.BehaviorAPI;
import com.bentahsin.antiafk.behavior.BehaviorAnalysisManager;
import com.bentahsin.antiafk.behavior.PlayerBehaviorData;
import com.bentahsin.antiafk.data.PointlessActivityData;
import com.bentahsin.antiafk.managers.AFKManager;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

@Singleton
public class BehaviorAPIImpl implements BehaviorAPI {

    private final BehaviorAnalysisManager behaviorManager;
    private final AFKManager afkManager;

    @Inject
    public BehaviorAPIImpl(BehaviorAnalysisManager behaviorManager, AFKManager afkManager) {
        this.behaviorManager = behaviorManager;
        this.afkManager = afkManager;
    }

    @Override
    public List<MovementVector> getTrajectory(Player player, int ticks) {
        if (!behaviorManager.isEnabled()) {
            return new ArrayList<>();
        }

        PlayerBehaviorData data = behaviorManager.getPlayerData(player);
        if (data == null) return new ArrayList<>();

        LinkedList<Location> history = data.getMovementHistory();
        List<MovementVector> trajectory = new ArrayList<>();

        synchronized (history) {
            int size = history.size();
            if (size < 2) return trajectory;

            int vectorsToCreate = Math.min(ticks, size - 1);
            int startIndex = size - 1 - vectorsToCreate;

            for (int i = startIndex; i < size - 1; i++) {
                Location from = history.get(i);
                Location to = history.get(i + 1);

                double dX = to.getX() - from.getX();
                double dZ = to.getZ() - from.getZ();
                double dYaw = to.getYaw() - from.getYaw();
                double dPitch = to.getPitch() - from.getPitch();

                if (dYaw > 180.0) dYaw -= 360.0;
                if (dYaw < -180.0) dYaw += 360.0;

                MovementVector.PlayerAction action = MovementVector.PlayerAction.IDLE;
                if (to.getY() > from.getY()) {
                    action = MovementVector.PlayerAction.JUMP;
                } else if (dX != 0 || dZ != 0) {
                    action = MovementVector.PlayerAction.NONE;
                }

                trajectory.add(new MovementVector(
                        dX, dZ,
                        dYaw, dPitch,
                        action,
                        1
                ));
            }
        }

        return trajectory;
    }

    @Override
    public int getPointlessActivityCount(Player player) {
        PointlessActivityData data = afkManager.getBotDetectionManager().getBotDetectionData(player.getUniqueId());
        return data != null ? data.getPointlessActivityCounter() : 0;
    }

    @Override
    public void resetBehaviorData(Player player) {
        if (behaviorManager.isEnabled()) {
            behaviorManager.getPlayerData(player).reset();
        }
        afkManager.getBotDetectionManager().resetBotDetectionData(player.getUniqueId());
    }
}