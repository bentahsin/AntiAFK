package com.bentahsin.antiafk.utils;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class TimeUtilTest {

    @Test
    @DisplayName("Basit Zaman Birimleri Doğru Çevrilmeli")
    void testSimpleParsing() {
        assertEquals(10, TimeUtil.parseTime("10s"));
        assertEquals(60, TimeUtil.parseTime("1m"));
        assertEquals(3600, TimeUtil.parseTime("1h"));
        assertEquals(86400, TimeUtil.parseTime("1d"));
    }

    @Test
    @DisplayName("Karmaşık Zaman İfadeleri Doğru Çevrilmeli")
    void testComplexParsing() {
        assertEquals(90, TimeUtil.parseTime("1m 30s"));
        assertEquals(3661, TimeUtil.parseTime("1h 1m 1s"));
    }

    @Test
    @DisplayName("Hatalı veya Boş Girdiler 0 Dönmeli")
    void testInvalidInput() {
        assertEquals(0, TimeUtil.parseTime(null));
        assertEquals(0, TimeUtil.parseTime(""));
        assertEquals(0, TimeUtil.parseTime("elma"));
        assertEquals(0, TimeUtil.parseTime("disabled"));
    }

    @Test
    @DisplayName("Saniyeden String'e Formatlama")
    void testFormatting() {
        assertEquals("1 dakika 30 saniye", TimeUtil.formatTime(90));
        assertEquals("10 saniye", TimeUtil.formatTime(10));
    }
}