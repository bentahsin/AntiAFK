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

class PlayerBehaviorDataTest {

    private PlayerBehaviorData behaviorData;
    @Mock private World world;
    @Mock private Location baseLoc;
    @Mock private Location moveLoc;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        behaviorData = new PlayerBehaviorData();
        when(baseLoc.getWorld()).thenReturn(world);
        when(moveLoc.getWorld()).thenReturn(world);
    }

    @Test
    @DisplayName("Mesafe Birikimi: Dar alanda hareket mesafe toplamalı")
    void testDistanceAccumulation() {
        behaviorData.processMovement(baseLoc, 5.0);

        when(moveLoc.distance(any())).thenReturn(2.0);
        when(moveLoc.distanceSquared(any())).thenReturn(4.0);

        behaviorData.processMovement(moveLoc, 5.0);
        assertEquals(2.0, behaviorData.getTotalDistanceTraveled(), 0.001);
    }

    @Test
    @DisplayName("Radius İhlali: Alan dışına çıkınca her şey sıfırlanmalı")
    void testResetOnRadiusExit() {
        behaviorData.processMovement(baseLoc, 2.0);

        when(moveLoc.distanceSquared(baseLoc)).thenReturn(100.0);

        behaviorData.processMovement(moveLoc, 2.0);

        assertEquals(0.0, behaviorData.getTotalDistanceTraveled(), "Mesafe sıfırlanmalı çünkü yarıçap dışına çıkıldı.");
        assertNotEquals(0, behaviorData.getConfinementDuration(), "Yeni bir takip penceresi başlamalı.");
    }

    @Test
    @DisplayName("Tam Sıfırlama: Reset metodu tüm alanları temizlemeli")
    void testFullReset() {
        behaviorData.processMovement(baseLoc, 5.0);
        behaviorData.setConsecutiveRepeatCount(5);

        behaviorData.reset();

        assertEquals(0.0, behaviorData.getTotalDistanceTraveled());
        assertEquals(0, behaviorData.getConsecutiveRepeatCount());
        assertEquals(0, behaviorData.getConfinementDuration());
    }
}