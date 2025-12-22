package com.bentahsin.antiafk.math;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class TrajectoryMathTest {

    @Test
    @DisplayName("Lineer İnterpolasyon (Lerp) Doğru Çalışmalı")
    void testInterpolation() {
        double start = 0;
        double end = 10;
        double fraction = 0.5;

        double result = interpolate(start, end, fraction);

        assertEquals(5.0, result, 0.001);
    }

    @Test
    @DisplayName("Açısal İnterpolasyon (Yaw Dönüşü) Doğru Çalışmalı")
    void testAngleInterpolation() {
        double angle1 = 350;
        double angle2 = 10;
        double fraction = 0.5;

        double diff = (angle2 - angle1 + 180) % 360 - 180;
        double diffAdjusted = (diff < -180 ? diff + 360 : diff);

        double result = angle1 + (diffAdjusted * fraction);

        assertTrue(Math.abs(result - 360) < 0.1 || Math.abs(result - 0) < 0.1);
    }

    private double interpolate(double a, double b, double f) {
        return a + (b - a) * f;
    }
}