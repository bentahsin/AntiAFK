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
        levels.add(new PunishmentLevel(1, null));
        levels.add(new PunishmentLevel(3, null));
        levels.add(new PunishmentLevel(5, null));

        Collections.sort(levels);

        assertEquals(1, findLevel(levels, 1).getCount());
        assertEquals(1, findLevel(levels, 2).getCount());
        assertEquals(3, findLevel(levels, 3).getCount());
        assertEquals(3, findLevel(levels, 4).getCount());
        assertEquals(5, findLevel(levels, 5).getCount());
        assertEquals(5, findLevel(levels, 10).getCount());
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