package com.bentahsin.antiafk.learning.collector;

import com.bentahsin.antiafk.api.learning.MovementVector;
import com.bentahsin.antiafk.learning.PatternAnalysisTask;
import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.Singleton;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Singleton
public class LearningDataCollectorTask extends BukkitRunnable {

    private final Provider<PatternAnalysisTask> analysisTaskProvider;
    private final Map<UUID, LearningData> playerData = new ConcurrentHashMap<>();

    @Inject
    public LearningDataCollectorTask(Provider<PatternAnalysisTask> analysisTaskProvider) {
        this.analysisTaskProvider = analysisTaskProvider;
    }

    @Override
    public void run() {
        PatternAnalysisTask analysisTask = analysisTaskProvider.get();
        if (analysisTask == null) return;

        for (Player player : Bukkit.getOnlinePlayers()) {
            LearningData data = playerData.computeIfAbsent(player.getUniqueId(), k -> new LearningData());

            if (data.skip || !player.isOnline()) continue;

            Location currentLocation = player.getLocation();
            if (data.lastLocation == null) {
                data.lastLocation = currentLocation;
                continue;
            }

            MovementVector.PlayerAction newAction = determineAction(data.lastLocation, currentLocation, player);

            if (newAction != data.currentState) {
                if (data.ticksInCurrentState > 0) {
                    createAndQueueVector(player, data.lastLocation, currentLocation, data.currentState, data.ticksInCurrentState);
                }

                data.currentState = newAction;
                data.ticksInCurrentState = 1;
            } else {
                data.ticksInCurrentState++;
            }

            data.lastLocation = currentLocation;
        }
    }

    private MovementVector.PlayerAction determineAction(Location from, Location to, Player p) {
        if (p.isSneaking() && from.getY() == to.getY()) {
            return p.isSneaking() ? MovementVector.PlayerAction.SNEAK_ON : MovementVector.PlayerAction.SNEAK_OFF;
        }
        if (to.getY() > from.getY() && !p.isFlying() && (to.getY() - from.getY()) > 0.1) {
            return MovementVector.PlayerAction.JUMP;
        }
        if (from.getX() != to.getX() || from.getZ() != to.getZ() || from.getYaw() != to.getYaw()) {
            return MovementVector.PlayerAction.NONE;
        }
        return MovementVector.PlayerAction.IDLE;
    }

    private void createAndQueueVector(Player p, Location from, Location to, MovementVector.PlayerAction action, int duration) {
        double dX = to.getX() - from.getX();
        double dZ = to.getZ() - from.getZ();
        double dYaw = to.getYaw() - from.getYaw();
        if (dYaw > 180.0) dYaw -= 360.0;
        if (dYaw < -180.0) dYaw += 360.0;
        double dPitch = to.getPitch() - from.getPitch();

        if (action == MovementVector.PlayerAction.IDLE) {
            dX = dZ = dYaw = dPitch = 0;
        }

        analysisTaskProvider.get().queueMovementData(p.getUniqueId(), dX, dZ, dYaw, dPitch, action, duration);
    }

    public void onPlayerQuit(Player player) {
        playerData.remove(player.getUniqueId());
    }
}