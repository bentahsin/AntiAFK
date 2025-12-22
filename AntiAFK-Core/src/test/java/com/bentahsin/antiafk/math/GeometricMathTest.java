package com.bentahsin.antiafk.math;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class GeometricMathTest {

    @Test
    @DisplayName("Vektör Normalizasyonu Testi")
    void testNormalize() {
        double x = 3;
        double y = 4;
        double length = Math.sqrt(x*x + y*y);

        double normX = x / length;
        double normY = y / length;

        assertEquals(0.6, normX, 0.001);
        assertEquals(0.8, normY, 0.001);
        assertEquals(1.0, Math.sqrt(normX*normX + normY*normY), 0.001);
    }

    @Test
    @DisplayName("İki Vektör Arasındaki Açı Testi")
    void testAngleCalculation() {
        double x1 = 1, y1 = 0;
        double x2 = 0, y2 = 1;

        double dot = (x1 * x2) + (y1 * y2);
        double mag1 = Math.sqrt(x1*x1 + y1*y1);
        double mag2 = Math.sqrt(x2*x2 + y2*y2);

        double cosTheta = dot / (mag1 * mag2);
        double angleRad = Math.acos(cosTheta);
        double angleDeg = Math.toDegrees(angleRad);

        assertEquals(90.0, angleDeg, 0.001, "Açı hesaplaması 90 derece çıkmalı.");
    }
}