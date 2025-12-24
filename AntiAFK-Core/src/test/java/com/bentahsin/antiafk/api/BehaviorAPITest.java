package com.bentahsin.antiafk.api;

import com.bentahsin.antiafk.api.implementation.BehaviorAPIImpl;
import com.bentahsin.antiafk.api.learning.MovementVector;
import com.bentahsin.antiafk.behavior.BehaviorAnalysisManager;
import com.bentahsin.antiafk.behavior.PlayerBehaviorData;
import com.bentahsin.antiafk.managers.AFKManager;
import com.bentahsin.antiafk.managers.BotDetectionManager;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.LinkedList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class BehaviorAPITest {

    @Mock private BehaviorAnalysisManager behaviorManager;
    @Mock private AFKManager afkManager;
    @Mock private Player player;
    @Mock private PlayerBehaviorData behaviorData;
    @Mock private BotDetectionManager botDetectionManager;

    private BehaviorAPIImpl behaviorAPI;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        when(behaviorManager.isEnabled()).thenReturn(true);
        when(behaviorManager.getPlayerData(player)).thenReturn(behaviorData);
        when(afkManager.getBotDetectionManager()).thenReturn(botDetectionManager);

        behaviorAPI = new BehaviorAPIImpl(behaviorManager, afkManager);
    }

    @Test
    @DisplayName("Location Geçmişi Doğru Vektöre Dönüşmeli")
    void testTrajectoryConversion() {
        LinkedList<Location> history = new LinkedList<>();

        Location loc1 = mock(Location.class); when(loc1.getX()).thenReturn(0.0); when(loc1.getZ()).thenReturn(0.0); when(loc1.getY()).thenReturn(60.0);
        Location loc2 = mock(Location.class); when(loc2.getX()).thenReturn(10.0); when(loc2.getZ()).thenReturn(0.0); when(loc2.getY()).thenReturn(60.0);
        Location loc3 = mock(Location.class); when(loc3.getX()).thenReturn(10.0); when(loc3.getZ()).thenReturn(10.0); when(loc3.getY()).thenReturn(60.0);

        history.add(loc1);
        history.add(loc2);
        history.add(loc3);

        when(behaviorData.getMovementHistory()).thenReturn(history);

        List<MovementVector> result = behaviorAPI.getTrajectory(player, 5);

        assertEquals(2, result.size(), "3 noktadan 2 vektör (değişim) oluşmalı.");

        assertEquals(10.0, result.get(0).getPositionChange().getX(), 0.001);
        assertEquals(0.0, result.get(0).getPositionChange().getY(), 0.001);

        assertEquals(0.0, result.get(1).getPositionChange().getX(), 0.001);
        assertEquals(10.0, result.get(1).getPositionChange().getY(), 0.001);
    }
}