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
        // 3 Adımlık basit bir hareket serisi
        double[][] data = {
                {1.0, 0.0, 0.0, 0.0, 0.0, 1.0},
                {2.0, 0.0, 0.0, 0.0, 0.0, 1.0},
                {3.0, 0.0, 0.0, 0.0, 0.0, 1.0}
        };

        TimeSeries ts1 = new TimeSeries(data);
        TimeSeries ts2 = new TimeSeries(data); // Aynı veri

        TimeWarpInfo info = FastDTW.getWarpInfoBetween(ts1, ts2, 1, new MovementVectorDistanceFn());

        assertEquals(0.0, info.getDistance(), 0.001, "Aynı serilerin DTW mesafesi 0 olmalıdır.");
    }

    @Test
    @DisplayName("FastDTW: Zaman Kayması (Time Warp) Testi")
    void testTimeWarping() {
        // Seri A: Normal Hız (1, 2, 3)
        double[][] dataA = {
                {1.0, 0, 0, 0, 0, 0},
                {2.0, 0, 0, 0, 0, 0},
                {3.0, 0, 0, 0, 0, 0}
        };

        // Seri B: Yavaş Hız (1, 1, 2, 2, 3, 3) - Aynı rota ama duraksayarak
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

        // Euclidean Distance olsa mesafe çok büyük çıkardı.
        // DTW olduğu için mesafe 0 (veya çok yakın) çıkmalı çünkü şekil aynı.
        System.out.println("Time Warp Distance: " + info.getDistance());

        assertTrue(info.getDistance() < 0.1, "DTW zaman kaymasını tolere etmeliydi.");
    }
}