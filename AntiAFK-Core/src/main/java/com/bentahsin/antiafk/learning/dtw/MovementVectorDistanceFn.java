package com.bentahsin.antiafk.learning.dtw;

import com.bentahsin.fastdtw.util.DistanceFunction;

public class MovementVectorDistanceFn implements DistanceFunction {

    private static final double POSITION_WEIGHT = 1.0;
    private static final double ROTATION_WEIGHT = 0.5;
    private static final double ACTION_MISMATCH_PENALTY = 5.0;
    private static final double DURATION_WEIGHT = 0.8;

    @Override
    public double calcDistance(double[] vector1, double[] vector2) {
        // [0]=PosX, [1]=PosY, [2]=RotX, [3]=RotY, [4]=Action, [5]=Duration

        double dx = vector1[0] - vector2[0];
        double dy = vector1[1] - vector2[1];
        double positionDistance = Math.sqrt(dx * dx + dy * dy);

        double drx = vector1[2] - vector2[2];
        double dry = vector1[3] - vector2[3];
        double rotationDistance = Math.sqrt(drx * drx + dry * dry);

        int action1Ordinal = (int) vector1[4];
        int action2Ordinal = (int) vector2[4];
        double actionDistance = (action1Ordinal != action2Ordinal) ? ACTION_MISMATCH_PENALTY : 0.0;

        double durationDistance = Math.abs(vector1[5] - vector2[5]);

        return (positionDistance * POSITION_WEIGHT) +
                (rotationDistance * ROTATION_WEIGHT) +
                actionDistance +
                (durationDistance * DURATION_WEIGHT);
    }
}