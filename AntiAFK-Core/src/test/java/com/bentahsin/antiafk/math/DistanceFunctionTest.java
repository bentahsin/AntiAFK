package com.bentahsin.antiafk.math;

import com.bentahsin.antiafk.learning.dtw.MovementVectorDistanceFn;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class DistanceFunctionTest {

    private final MovementVectorDistanceFn distFn = new MovementVectorDistanceFn();

    @Test
    @DisplayName("Birebir Aynı İki Hareketin Mesafesi 0 Olmalı")
    void testIdenticalVectors() {
        double[] vec1 = {1.0, 0.0, 90.0, 0.0, 0.0, 20.0};
        double[] vec2 = {1.0, 0.0, 90.0, 0.0, 0.0, 20.0};

        double distance = distFn.calcDistance(vec1, vec2);

        assertEquals(0.0, distance, "Aynı vektörlerin mesafesi 0 olmalıdır.");
    }

    @Test
    @DisplayName("Sadece Pozisyon Farkı Ağırlığı Testi")
    void testPositionDifference() {
        double[] vec1 = {0.0, 0.0, 0.0, 0.0, 0.0, 0.0};
        double[] vec2 = {1.0, 0.0, 0.0, 0.0, 0.0, 0.0};

        double distance = distFn.calcDistance(vec1, vec2);

        assertEquals(1.0, distance, 0.001);
    }

    @Test
    @DisplayName("Eylem (Action) Farkı Cezası Testi")
    void testActionMismatch() {
        double[] vec1 = {0.0, 0.0, 0.0, 0.0, 1.0, 0.0};
        double[] vec2 = {0.0, 0.0, 0.0, 0.0, 2.0, 0.0};

        double distance = distFn.calcDistance(vec1, vec2);

        assertEquals(5.0, distance, 0.001, "Eylem uyuşmazlığı cezası (5.0) uygulanmadı.");
    }
}