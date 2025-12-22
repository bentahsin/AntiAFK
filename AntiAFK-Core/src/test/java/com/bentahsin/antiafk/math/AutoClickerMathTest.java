package com.bentahsin.antiafk.math;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class AutoClickerMathTest {

    @Test
    @DisplayName("Makro (Düzenli) Tıklamaların Sapması Düşük Olmalı")
    void testMacroConsistency() {
        List<Long> macroIntervals = Arrays.asList(50L, 50L, 51L, 50L, 50L, 49L, 50L, 50L);

        double deviation = calculateStandardDeviation(macroIntervals);

        System.out.println("Macro Deviation: " + deviation);

        assertTrue(deviation < 2.0, "Makro tespiti başarısız: Sapma çok yüksek çıktı.");
    }

    @Test
    @DisplayName("İnsan (Düzensiz) Tıklamaların Sapması Yüksek Olmalı")
    void testHumanConsistency() {
        List<Long> humanIntervals = Arrays.asList(100L, 250L, 120L, 90L, 300L, 110L, 500L);

        double deviation = calculateStandardDeviation(humanIntervals);

        System.out.println("Human Deviation: " + deviation);

        assertTrue(deviation > 20.0, "İnsan tespiti başarısız: Sapma çok düşük (bot gibi) çıktı.");
    }

    private double calculateStandardDeviation(List<Long> data) {
        double sum = 0.0;
        for (long i : data) sum += i;
        double mean = sum / data.size();

        double standardDeviation = 0.0;
        for (long num : data) {
            standardDeviation += Math.pow(num - mean, 2);
        }
        return Math.sqrt(standardDeviation / data.size());
    }
}