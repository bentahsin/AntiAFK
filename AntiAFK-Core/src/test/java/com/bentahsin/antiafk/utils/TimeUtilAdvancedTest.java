package com.bentahsin.antiafk.utils;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class TimeUtilAdvancedTest {

    @Test
    @DisplayName("Boşluklu ve Büyük Harfli Girdiler")
    void testMessyInput() {
        long result = TimeUtil.parseTime("  10M   30S ");
        assertEquals(630, result);
    }

    @Test
    @DisplayName("Bilinmeyen Birimler Yoksayılmalı")
    void testUnknownUnits() {
        long result = TimeUtil.parseTime("10x 5m");
        assertEquals(300, result);
    }
}