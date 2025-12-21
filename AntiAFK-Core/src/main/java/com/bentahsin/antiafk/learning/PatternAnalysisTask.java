package com.bentahsin.antiafk.learning;

import com.bentahsin.antiafk.AntiAFKPlugin;
import com.bentahsin.antiafk.api.enums.DetectionType;
import com.bentahsin.antiafk.learning.dtw.MovementVectorDistanceFn;
import com.bentahsin.antiafk.learning.pool.VectorPoolManager;
import com.bentahsin.antiafk.learning.util.LimitedQueue;
import com.bentahsin.antiafk.managers.AFKManager;
import com.bentahsin.antiafk.managers.ConfigManager;
import com.bentahsin.antiafk.managers.DebugManager;
import com.bentahsin.fastdtw.dtw.FastDTW;
import com.bentahsin.fastdtw.dtw.TimeWarpInfo;
import com.bentahsin.fastdtw.timeseries.TimeSeries;
import com.bentahsin.fastdtw.util.DistanceFunction;
import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.Singleton;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.List;
import java.util.Queue;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * Oyuncuların anlık hareketlerini, bilinen bot desenleriyle asenkron olarak karşılaştırır.
 * Bu sınıf, kendi nesne havuzunu yöneterek ve ham veriyi işleyerek GC yükünü minimize eder.
 */
@Singleton
public class PatternAnalysisTask extends BukkitRunnable {

    private final AntiAFKPlugin plugin;
    private final Provider<AFKManager> afkMgrProvider;
    private final DebugManager debugMgr;
    private final PatternManager patternManager;
    private final RecordingManager recordingManager;
    private final VectorPoolManager vectorPoolManager;

    private final Queue<QueuedMovementData> pendingData = new ConcurrentLinkedQueue<>();
    private final ConcurrentHashMap<UUID, List<MovementVector>> observationWindows;
    private static final int WINDOW_SIZE = 300;
    private static final int MIN_OBSERVATION_SIZE = 60;

    private final double similarityThreshold;
    private final int searchRadius;
    private final double preFilterSizeRatio;
    private final DistanceFunction distanceFunction;

    @Inject
    public PatternAnalysisTask(AntiAFKPlugin plugin, Provider<AFKManager> afkMgrProvider, DebugManager debugMgr,
                               PatternManager patternManager, RecordingManager recordingManager,
                               ConfigManager configManager, VectorPoolManager vectorPoolManager) {
        this.plugin = plugin;
        this.afkMgrProvider = afkMgrProvider;
        this.debugMgr = debugMgr;
        this.patternManager = patternManager;
        this.recordingManager = recordingManager;
        this.vectorPoolManager = vectorPoolManager;
        this.observationWindows = new ConcurrentHashMap<>();

        this.similarityThreshold = configManager.getLearningSimilarityThreshold();
        this.searchRadius = configManager.getLearningSearchRadius();
        this.preFilterSizeRatio = configManager.getPreFilterSizeRatio();
        this.distanceFunction = new MovementVectorDistanceFn();
    }

    @Override
    public void run() {
        processPendingData();

        if (patternManager.getKnownPatterns().isEmpty()) {
            return;
        }

        for (Player player : Bukkit.getOnlinePlayers()) {
            List<MovementVector> playerWindow = observationWindows.get(player.getUniqueId());

            if (playerWindow == null || playerWindow.size() < MIN_OBSERVATION_SIZE) {
                continue;
            }

            debugMgr.log(DebugManager.DebugModule.LEARNING_MODE,
                    "Analyzing player %s with an observation window of %d vectors.",
                    player.getName(), playerWindow.size()
            );

            TimeSeries playerTimeSeries = convertToTimeSeries(playerWindow);

            for (Pattern knownPattern : patternManager.getKnownPatterns()) {
                if (playerWindow.size() < knownPattern.getVectors().size() * preFilterSizeRatio) {
                    debugMgr.log(DebugManager.DebugModule.LEARNING_MODE,
                            "Skipping pattern '%s' for %s (Player window too small: %d < %d * %.2f).",
                            knownPattern.getName(), player.getName(), playerWindow.size(), knownPattern.getVectors().size(), preFilterSizeRatio
                    );
                    continue;
                }

                TimeSeries patternTimeSeries = convertToTimeSeries(knownPattern.getVectors());
                TimeWarpInfo info = FastDTW.getWarpInfoBetween(playerTimeSeries, patternTimeSeries, searchRadius, distanceFunction);

                debugMgr.log(DebugManager.DebugModule.LEARNING_MODE,
                        "Comparison for %s against pattern '%s': DTW Distance = %.2f (Threshold: %.2f)",
                        player.getName(), knownPattern.getName(), info.getDistance(), similarityThreshold
                );

                if (info.getDistance() < similarityThreshold) {
                    debugMgr.log(DebugManager.DebugModule.LEARNING_MODE,
                            "Pattern match FOUND for %s with '%s'. Distance: %.2f",
                            player.getName(), knownPattern.getName(), info.getDistance()
                    );
                    Bukkit.getScheduler().runTask(plugin, () ->
                            afkMgrProvider.get().getBotDetectionManager().triggerSuspicionAndChallenge(player, "behavior.learned_pattern_detected", DetectionType.LEARNED_PATTERN)
                    );
                    break;
                }
            }
        }
    }

    /**
     * Kuyrukta birikmiş tüm ham hareket verilerini çeker, onları MovementVector nesnelerine
     * dönüştürür ve ilgili oyuncuların gözlem pencerelerine/kayıtlarına ekler.
     */
    private void processPendingData() {
        QueuedMovementData data;
        while ((data = pendingData.poll()) != null) {
            MovementVector vector = vectorPoolManager.borrowVector(
                    data.deltaX, data.deltaZ,
                    data.deltaYaw, data.deltaPitch,
                    data.action,
                    data.durationTicks
            );

            List<MovementVector> window = observationWindows.computeIfAbsent(data.playerUuid, k -> new LimitedQueue<>(WINDOW_SIZE, vectorPoolManager));
            window.add(vector);

            recordingManager.addVectorIfRecording(data.playerUuid, vector);
        }
    }

    /**
     * Dışarıdan (PlayerMovementListener'dan) çağrılarak kuyruğa yeni ham veri ekler.
     * Bu metot kilitlenmesizdir ve son derece hızlıdır.
     */
    public void queueMovementData(UUID uuid, double dx, double dz, double dyaw, double dpitch, MovementVector.PlayerAction action, int durationTicks) {
        pendingData.add(new QueuedMovementData(uuid, dx, dz, dyaw, dpitch, action, durationTicks));
    }

    /**
     * Eklenti devre dışı bırakıldığında, yönettiği nesne havuzunu kapatır.
     */
    public void shutdown() {
        if (vectorPoolManager != null) {
            vectorPoolManager.close();
        }
    }

    /**
     * Oyuncu çıktığında, onunla ilgili hareket verilerini ve kayıtlarını temizler.
     */
    public void onPlayerQuit(Player player) {
        List<MovementVector> window = observationWindows.remove(player.getUniqueId());
        if (window != null) {
            window.forEach(vectorPoolManager::returnVector);
        }
    }

    private TimeSeries convertToTimeSeries(List<MovementVector> vectors) {
        int size = vectors.size();
        double[][] data = new double[size][6];

        for (int i = 0; i < size; i++) {
            MovementVector v = vectors.get(i);
            data[i][0] = v.getPositionChange().getX();
            data[i][1] = v.getPositionChange().getY();
            data[i][2] = v.getRotationChange().getX();
            data[i][3] = v.getRotationChange().getY();
            data[i][4] = v.getAction().ordinal();
            data[i][5] = v.getDurationTicks();
        }

        return new TimeSeries(data);
    }

    /**
     * API için senkron veya asenkron anlık skor hesaplar.
     * Not: Bu işlem ağır olabilir, dikkatli kullanılmalı.
     */
    public double calculateScoreForApi(Player player, String patternName) {
        List<MovementVector> history = observationWindows.get(player.getUniqueId());
        Pattern pattern = patternManager.getPattern(patternName);

        if (history == null || pattern == null || history.size() < MIN_OBSERVATION_SIZE) {
            return -1.0;
        }

        TimeSeries tsPlayer = convertToTimeSeries(history);
        TimeSeries tsPattern = convertToTimeSeries(pattern.getVectors());

        TimeWarpInfo info = FastDTW.getWarpInfoBetween(tsPlayer, tsPattern, searchRadius, distanceFunction);

        double distance = info.getDistance();
        return 1.0 / (1.0 + distance);
    }
}