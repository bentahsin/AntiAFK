package com.bentahsin.antiafk.learning;

import com.bentahsin.antiafk.learning.dtw.MovementVectorDistanceFn;
import com.bentahsin.fastdtw.dtw.FastDTW;
import com.bentahsin.fastdtw.dtw.TimeWarpInfo;
import com.bentahsin.fastdtw.timeseries.TimeSeries;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class FastDTWNoiseTest {

    @Test
    @DisplayName("FastDTW: Gürültülü (Jittery) Hareket Testi")
    void testNoiseTolerance() {
        double[][] cleanPath = {
                {10, 10, 0, 0, 0, 0}, {20, 20, 0, 0, 0, 0}, {30, 30, 0, 0, 0, 0}
        };

        double[][] noisyPath = {
                {10.2, 9.8, 0, 0, 0, 0}, {19.9, 20.1, 0, 0, 0, 0}, {30.1, 29.9, 0, 0, 0, 0}
        };

        TimeSeries tsClean = new TimeSeries(cleanPath);
        TimeSeries tsNoisy = new TimeSeries(noisyPath);

        TimeWarpInfo info = FastDTW.getWarpInfoBetween(tsClean, tsNoisy, 1, new MovementVectorDistanceFn());
        System.out.println("Noisy Distance: " + info.getDistance());

        assertTrue(info.getDistance() < 2.0, "DTW küçük titreşimleri (noise) tolere edemedi!");
    }
}