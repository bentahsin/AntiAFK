package com.bentahsin.antiafk.learning.dtw;

import com.bentahsin.fastdtw.util.DistanceFunction;
import org.apache.commons.math3.geometry.euclidean.twod.Vector2D;

/**
 * FastDTW kütüphanesi için, iki MovementVector arasındaki mesafeyi
 * nasıl hesaplayacağını tanımlayan özel bir DistanceFunction.
 * Bu versiyon, konum, rotasyon VE eylem farklılıklarını hesaba katar.
 */
public class MovementVectorDistanceFn implements DistanceFunction {

    private static final double POSITION_WEIGHT = 1.0;
    private static final double ROTATION_WEIGHT = 0.5;
    private static final double ACTION_MISMATCH_PENALTY = 5.0;
    private static final double DURATION_WEIGHT = 0.8;

    @Override
    public double calcDistance(double[] vector1, double[] vector2) {
        Vector2D pos1 = new Vector2D(vector1[0], vector1[1]);
        Vector2D pos2 = new Vector2D(vector2[0], vector2[1]);

        Vector2D rot1 = new Vector2D(vector1[2], vector1[3]);
        Vector2D rot2 = new Vector2D(vector2[2], vector2[3]);

        int action1Ordinal = (int) vector1[4];
        int action2Ordinal = (int) vector2[4];

        int duration1 = (int) vector1[5];
        int duration2 = (int) vector2[5];

        double positionDistance = pos1.distance(pos2);
        double rotationDistance = rot1.distance(rot2);

        double actionDistance = 0.0;
        if (action1Ordinal != action2Ordinal) {
            actionDistance = ACTION_MISMATCH_PENALTY;
        }

        double durationDistance = Math.abs(duration1 - duration2);

        return (positionDistance * POSITION_WEIGHT) +
                (rotationDistance * ROTATION_WEIGHT) +
                actionDistance +
                (durationDistance * DURATION_WEIGHT);
    }
}