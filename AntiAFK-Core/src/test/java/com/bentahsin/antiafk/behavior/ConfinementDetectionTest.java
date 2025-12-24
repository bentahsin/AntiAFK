package com.bentahsin.antiafk.behavior;

import org.bukkit.Location;
import org.bukkit.World;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ConfinementDetectionTest {

    private PlayerBehaviorData behaviorData;
    @Mock private World mockWorld;
    @Mock private Location loc1;
    @Mock private Location loc2;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        behaviorData = new PlayerBehaviorData();

        when(loc1.getWorld()).thenReturn(mockWorld);
        when(loc2.getWorld()).thenReturn(mockWorld);
    }

    @Test
    @DisplayName("Pozitif Tespit: Oyuncu dar alanda çok hareket ederse yakalanmalı")
    void testConfinementPositive() {
        behaviorData.processMovement(loc1, 5.0);

        when(loc1.distance(loc2)).thenReturn(2.0);
        when(loc1.distanceSquared(loc2)).thenReturn(4.0);

        for(int i = 0; i < 50; i++) {
            behaviorData.processMovement(loc2, 5.0);
            behaviorData.processMovement(loc1, 5.0);
        }

        assertTrue(behaviorData.getTotalDistanceTraveled() >= 100.0, "Toplam mesafe doğru hesaplanmadı.");
        assertTrue(behaviorData.getConfinementDuration() >= 0);
    }

    @Test
    @DisplayName("Negatif Tespit: Oyuncu alanın dışına çıkarsa sayaç sıfırlanmalı")
    void testConfinementResetOnExit() {
        behaviorData.processMovement(loc1, 3.0);

        when(loc1.distanceSquared(loc2)).thenReturn(100.0);

        behaviorData.processMovement(loc2, 3.0);
        assertEquals(0.0, behaviorData.getTotalDistanceTraveled(), 0.001);
    }

    @Test
    @DisplayName("Dünya Değişimi: Farklı dünyaya geçerse sistem sıfırlanmalı")
    void testWorldChangeReset() {
        World otherWorld = mock(World.class);
        Location otherWorldLoc = mock(Location.class);
        when(otherWorldLoc.getWorld()).thenReturn(otherWorld);

        behaviorData.processMovement(loc1, 5.0);
        behaviorData.processMovement(otherWorldLoc, 5.0);

        assertEquals(0.0, behaviorData.getTotalDistanceTraveled(), "Dünya değişince veriler sıfırlanmadı.");
    }
}