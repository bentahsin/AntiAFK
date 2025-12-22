package com.bentahsin.antiafk.managers;

import com.bentahsin.antiafk.models.PunishmentLevel;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class PunishmentLogicTest {

    @Test
    @DisplayName("Ceza Seviyesi Seçimi Doğru Çalışmalı")
    void testPunishmentLevelSelection() {
        List<PunishmentLevel> levels = new ArrayList<>();
        levels.add(new PunishmentLevel(1, null)); // Uyarı
        levels.add(new PunishmentLevel(3, null)); // Kick
        levels.add(new PunishmentLevel(5, null)); // Ban

        Collections.sort(levels);

        assertEquals(1, findLevel(levels, 1).getCount()); // 1. Ceza -> Seviye 1
        assertEquals(1, findLevel(levels, 2).getCount()); // 2. Ceza -> Seviye 1 (Arada kalıyor)
        assertEquals(3, findLevel(levels, 3).getCount()); // 3. Ceza -> Seviye 3
        assertEquals(3, findLevel(levels, 4).getCount()); // 4. Ceza -> Seviye 3
        assertEquals(5, findLevel(levels, 5).getCount()); // 5. Ceza -> Seviye 5
        assertEquals(5, findLevel(levels, 10).getCount()); // 10. Ceza -> Seviye 5 (Max)
    }

    private PunishmentLevel findLevel(List<PunishmentLevel> levels, int currentCount) {
        for (PunishmentLevel level : levels) {
            if (currentCount >= level.getCount()) {
                return level;
            }
        }
        return levels.get(levels.size() - 1);
    }
}