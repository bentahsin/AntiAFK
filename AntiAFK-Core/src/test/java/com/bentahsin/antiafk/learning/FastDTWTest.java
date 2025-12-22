package com.bentahsin.antiafk.learning;

import com.bentahsin.antiafk.learning.dtw.MovementVectorDistanceFn;
import com.bentahsin.fastdtw.dtw.FastDTW;
import com.bentahsin.fastdtw.dtw.TimeWarpInfo;
import com.bentahsin.fastdtw.timeseries.TimeSeries;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class FastDTWTest {

    @Test
    @DisplayName("FastDTW: Aynı İki Zaman Serisinin Mesafesi 0 Olmalı")
    void testIdenticalSeries() {
        double[][] data = {
                {1.0, 0.0, 0.0, 0.0, 0.0, 1.0},
                {2.0, 0.0, 0.0, 0.0, 0.0, 1.0},
                {3.0, 0.0, 0.0, 0.0, 0.0, 1.0}
        };

        TimeSeries ts1 = new TimeSeries(data);
        TimeSeries ts2 = new TimeSeries(data);

        TimeWarpInfo info = FastDTW.getWarpInfoBetween(ts1, ts2, 1, new MovementVectorDistanceFn());

        assertEquals(0.0, info.getDistance(), 0.001, "Aynı serilerin DTW mesafesi 0 olmalıdır.");
    }

    @Test
    @DisplayName("FastDTW: Zaman Kayması (Time Warp) Testi")
    void testTimeWarping() {
        double[][] dataA = {
                {1.0, 0, 0, 0, 0, 0},
                {2.0, 0, 0, 0, 0, 0},
                {3.0, 0, 0, 0, 0, 0}
        };

        double[][] dataB = {
                {1.0, 0, 0, 0, 0, 0},
                {1.0, 0, 0, 0, 0, 0},
                {2.0, 0, 0, 0, 0, 0},
                {2.0, 0, 0, 0, 0, 0},
                {3.0, 0, 0, 0, 0, 0},
                {3.0, 0, 0, 0, 0, 0}
        };

        TimeSeries tsA = new TimeSeries(dataA);
        TimeSeries tsB = new TimeSeries(dataB);

        TimeWarpInfo info = FastDTW.getWarpInfoBetween(tsA, tsB, 5, new MovementVectorDistanceFn());

        System.out.println("Time Warp Distance: " + info.getDistance());

        assertTrue(info.getDistance() < 0.1, "DTW zaman kaymasını tolere etmeliydi.");
    }

    @Test
    @DisplayName("FastDTW: Farklı Uzunluktaki (Duraksamalı) Rota Testi")
    void testDifferentLengths() {
        double[][] cleanData = {
                {1, 1, 0, 0, 0, 0},
                {2, 2, 0, 0, 0, 0},
                {3, 3, 0, 0, 0, 0}
        };

        double[][] laggyData = {
                {1, 1, 0, 0, 0, 0},
                {1, 1, 0, 0, 0, 0},
                {1, 1, 0, 0, 0, 0},
                {2, 2, 0, 0, 0, 0},
                {3, 3, 0, 0, 0, 0},
                {3, 3, 0, 0, 0, 0}
        };

        TimeSeries tsClean = new TimeSeries(cleanData);
        TimeSeries tsLaggy = new TimeSeries(laggyData);

        TimeWarpInfo info = FastDTW.getWarpInfoBetween(tsClean, tsLaggy, 5, new MovementVectorDistanceFn());

        System.out.println("Time Warp Distance: " + info.getDistance());

        assertEquals(0.0, info.getDistance(), 0.001, "DTW zaman kaymasını (lag) tolere edemedi!");
    }

    @Test
    @DisplayName("FastDTW: Tamamen Farklı İki Rota")
    void testCompletelyDifferent() {
        double[][] right = { {1,0,0,0,0,0}, {2,0,0,0,0,0}, {3,0,0,0,0,0} };

        double[][] up = { {0,1,0,0,0,0}, {0,2,0,0,0,0}, {0,3,0,0,0,0} };

        TimeSeries ts1 = new TimeSeries(right);
        TimeSeries ts2 = new TimeSeries(up);

        TimeWarpInfo info = FastDTW.getWarpInfoBetween(ts1, ts2, 1, new MovementVectorDistanceFn());

        System.out.println("Different Shape Distance: " + info.getDistance());

        assertTrue(info.getDistance() > 5.0, "Farklı rotalar benzer çıktı!");
    }
}