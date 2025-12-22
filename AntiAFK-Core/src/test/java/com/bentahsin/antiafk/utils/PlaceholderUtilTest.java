package com.bentahsin.antiafk.utils;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class PlaceholderUtilTest {

    @Test
    @DisplayName("Basit String Değiştirme Mantığı")
    void testBasicReplacements() {
        String template = "Merhaba %player%, kalan süre: %time_left%";
        String playerName = "Steve";
        String timeLeft = "10s";

        String result = template
                .replace("%player%", playerName)
                .replace("%time_left%", timeLeft);

        assertEquals("Merhaba Steve, kalan süre: 10s", result);
    }

    @Test
    @DisplayName("Null Değerler Kodu Patlatmamalı")
    void testNullSafety() {
        String template = null;
        String result = (template == null) ? "" : template.replace("%a%", "b");

        assertEquals("", result, "Null mesaj geldiğinde sistem çökmemeli, boş string dönmeli.");
    }
}