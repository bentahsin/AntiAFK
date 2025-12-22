package com.bentahsin.antiafk.api.implementation;

import com.bentahsin.antiafk.api.learning.MovementVector;
import com.bentahsin.antiafk.api.learning.Pattern;
import com.bentahsin.antiafk.api.managers.AIEngineAPI;
import com.bentahsin.antiafk.api.models.PatternMatchResult;
import com.bentahsin.antiafk.learning.PatternManager;
import com.bentahsin.antiafk.learning.dtw.MovementVectorDistanceFn;
import com.bentahsin.antiafk.managers.ConfigManager;
import com.bentahsin.fastdtw.dtw.FastDTW;
import com.bentahsin.fastdtw.dtw.TimeWarpInfo;
import com.bentahsin.fastdtw.timeseries.TimeSeries;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import java.util.List;

@Singleton
public class AIEngineAPIImpl implements AIEngineAPI {

    private final PatternManager patternManager;
    private final ConfigManager configManager;
    private final MovementVectorDistanceFn distFn;

    @Inject
    public AIEngineAPIImpl(PatternManager patternManager, ConfigManager configManager) {
        this.patternManager = patternManager;
        this.configManager = configManager;
        this.distFn = new MovementVectorDistanceFn();
    }

    @Override
    public double compareTrajectories(List<MovementVector> trajectoryA, List<MovementVector> trajectoryB) {
        if (trajectoryA.isEmpty() || trajectoryB.isEmpty()) return 0.0;

        TimeSeries tsA = convertToTimeSeries(trajectoryA);
        TimeSeries tsB = convertToTimeSeries(trajectoryB);

        int radius = configManager.getLearningSearchRadius();
        TimeWarpInfo info = FastDTW.getWarpInfoBetween(tsA, tsB, radius, distFn);

        return 1.0 / (1.0 + info.getDistance());
    }

    @Override
    public PatternMatchResult findBestMatch(List<MovementVector> trajectory) {
        if (trajectory == null || trajectory.size() < 20) {
            return new PatternMatchResult(null, 0.0, false);
        }

        TimeSeries tsPlayer = convertToTimeSeries(trajectory);

        double minDistance = Double.MAX_VALUE;
        String bestPatternName = null;

        double thresholdDistance = configManager.getLearningSimilarityThreshold();
        int searchRadius = configManager.getLearningSearchRadius();

        for (Pattern pattern : patternManager.getKnownPatterns()) {
            TimeSeries tsPattern = convertToTimeSeries(pattern.getVectors());

            TimeWarpInfo info = FastDTW.getWarpInfoBetween(tsPlayer, tsPattern, searchRadius, distFn);
            double currentDistance = info.getDistance();

            if (currentDistance < minDistance) {
                minDistance = currentDistance;
                bestPatternName = pattern.getName();
            }
        }

        boolean isMatch = (bestPatternName != null && minDistance <= thresholdDistance);
        double finalScore = (minDistance == Double.MAX_VALUE) ? 0.0 : (1.0 / (1.0 + minDistance));

        return new PatternMatchResult(bestPatternName, finalScore, isMatch);
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
}