package com.bentahsin.antiafk.behavior;

import com.bentahsin.antiafk.AntiAFKPlugin;
import com.bentahsin.antiafk.behavior.util.TrajectoryComparator;
import com.bentahsin.antiafk.managers.AFKManager;
import com.bentahsin.antiafk.managers.ConfigManager;
import com.bentahsin.antiafk.managers.PlayerLanguageManager;
import com.bentahsin.antiafk.turing.CaptchaManager;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

/**
 * DEĞİŞİKLİK: Bu görev artık ağır analizleri asenkron olarak yapar ve sadece sonuçları
 * ana thread'e bildirir. Bu, sunucu performansını korur.
 */
public class BehaviorAnalysisTask extends BukkitRunnable {

    private final AntiAFKPlugin plugin;
    private final BehaviorAnalysisManager analysisManager;
    private final PlayerLanguageManager plLang;
    private final ConfigManager cm;
    private final AFKManager afkMgr;
    private final Optional<CaptchaManager> capm;

    private final int minTrajectoryPoints;
    private final int maxTrajectoryPoints;
    private final double locationTolerance;
    private final double directionTolerance;
    private final double similarityThreshold;
    private final int maxRepeats;
    private final boolean debugEnabled;

    public BehaviorAnalysisTask(AntiAFKPlugin plugin, BehaviorAnalysisManager manager) {
        this.plugin = plugin;
        this.plLang = plugin.getPlayerLanguageManager();
        this.analysisManager = manager;
        this.cm = plugin.getConfigManager();
        this.afkMgr = plugin.getAfkManager();
        this.capm = plugin.getCaptchaManager();

        this.minTrajectoryPoints = plugin.getConfig().getInt("behavioral-analysis.min-trajectory-points", 20);
        this.maxTrajectoryPoints = plugin.getConfig().getInt("behavioral-analysis.max-trajectory-points", 300);
        this.locationTolerance = plugin.getConfig().getDouble("behavioral-analysis.location-tolerance", 1.0);
        this.directionTolerance = plugin.getConfig().getDouble("behavioral-analysis.direction-tolerance", 25.0);
        this.similarityThreshold = plugin.getConfig().getDouble("behavioral-analysis.similarity-threshold", 0.85);
        this.maxRepeats = plugin.getConfig().getInt("behavioral-analysis.max-repeats", 3);
        this.debugEnabled = plugin.getConfig().getBoolean("behavioral-analysis.debug", false);
    }

    @Override
    public void run() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            if (player.hasPermission("antiafk.bypass.behavior") || !player.isOnline()) {
                continue;
            }

            PlayerBehaviorData data = analysisManager.getPlayerData(player);
            LinkedList<Location> history = data.getMovementHistory();
            boolean matchFound = false;

            List<Location> currentTrajectory = new ArrayList<>(maxTrajectoryPoints);
            List<Location> pastTrajectory = new ArrayList<>(maxTrajectoryPoints);

            synchronized (history) {
                for (int currentLength = minTrajectoryPoints; currentLength <= maxTrajectoryPoints; currentLength++) {
                    if (history.size() < currentLength * 2) {
                        continue;
                    }

                    currentTrajectory.clear();
                    currentTrajectory.addAll(history.subList(history.size() - currentLength, history.size()));

                    for (int i = history.size() - (currentLength * 2); i >= 0; i--) {
                        if (i + currentLength > history.size()) continue;
                        pastTrajectory.clear();
                        pastTrajectory.addAll(history.subList(i, i + currentLength));

                        if (TrajectoryComparator.areSimilar(pastTrajectory, currentTrajectory, locationTolerance, directionTolerance, similarityThreshold)) {
                            matchFound = true;
                            long now = System.currentTimeMillis();

                            double expectedTime = (double) currentLength / 20.0 * 1000.0;
                            if (now - data.getLastRepeatTimestamp() < expectedTime * 1.5) {
                                data.setConsecutiveRepeatCount(data.getConsecutiveRepeatCount() + 1);
                            } else {
                                data.setConsecutiveRepeatCount(1);
                            }

                            data.setLastRepeatTimestamp(now);

                            final int repeatCount = data.getConsecutiveRepeatCount();
                            if (cm.isTuringTestEnabled() &&
                                    capm.map(manager -> !manager.isBeingTested(player)).orElse(false) &&
                                    repeatCount == cm.getTriggerOnBehavioralRepeatCount()) {

                                Bukkit.getScheduler().runTask(plugin, () -> capm.ifPresent(manager -> manager.startChallenge(player)));
                            }

                            if (debugEnabled) {
                                final int count = data.getConsecutiveRepeatCount();
                                final double time = (double) currentLength / 20.0;
                                Bukkit.getScheduler().runTask(plugin, () ->
                                        plLang.sendMessage(player, "behavior.debug.repetition",
                                                "%time%", String.format("%.1f", time),
                                                "%count%", String.valueOf(count),
                                                "%max_repeats%", String.valueOf(maxRepeats)
                                        )
                                );
                            }

                            if (data.getConsecutiveRepeatCount() >= maxRepeats) {
                                Bukkit.getScheduler().runTask(plugin, () ->
                                        afkMgr.setManualAFK(player, "Otonom hareket tespiti.")
                                );
                                data.reset();
                                break;
                            }
                            break;
                        }
                    }
                    if (matchFound) break;
                }
            }

            if (!matchFound) {
                data.setConsecutiveRepeatCount(0);
            }
        }
    }
}