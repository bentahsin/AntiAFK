package com.bentahsin.antiafk.math;

import com.bentahsin.antiafk.api.learning.CustomVector;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class CustomVectorTest {

    @Test
    @DisplayName("Vektör Mesafesi (Euclidean) Doğru Hesaplanmalı")
    void testDistanceCalculation() {
        CustomVector v1 = new CustomVector(0, 0);
        CustomVector v2 = new CustomVector(3, 4);

        double distance = v1.distance(v2);

        assertEquals(5.0, distance, 0.0001, "Mesafe hesaplaması hatalı!");
    }

    @Test
    @DisplayName("Vektör Set Metodu Değerleri Doğru Güncellemeli")
    void testVectorMutability() {
        CustomVector vector = new CustomVector(10, 20);

        vector.set(50, 60);

        assertAll("Vektör güncellemeleri",
                () -> assertEquals(50, vector.getX()),
                () -> assertEquals(60, vector.getY())
        );
    }
}