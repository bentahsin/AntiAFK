package com.bentahsin.antiafk.api.implementation;

import com.bentahsin.antiafk.behavior.BehaviorAnalysisManager;
import com.bentahsin.antiafk.behavior.PlayerBehaviorData;
import com.bentahsin.antiafk.data.PointlessActivityData;
import com.bentahsin.antiafk.managers.AFKManager;
import com.bentahsin.antiafk.managers.BotDetectionManager;
import org.bukkit.entity.Player;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

class BehaviorAPIImplTest {

    @Mock private BehaviorAnalysisManager behaviorManager;
    @Mock private AFKManager afkManager;
    @Mock private BotDetectionManager botDetectionManager;
    @Mock private Player player;
    @Mock private PlayerBehaviorData behaviorData;
    @Mock private PointlessActivityData pointlessData;

    private BehaviorAPIImpl behaviorAPI;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        when(afkManager.getBotDetectionManager()).thenReturn(botDetectionManager);
        when(player.getUniqueId()).thenReturn(UUID.randomUUID());

        behaviorAPI = new BehaviorAPIImpl(behaviorManager, afkManager);
    }

    @Test
    @DisplayName("getPointlessActivityCount: Doğru Veriyi Çekmeli")
    void testGetPointlessActivityCount() {
        when(botDetectionManager.getBotDetectionData(any())).thenReturn(pointlessData);
        when(pointlessData.getPointlessActivityCounter()).thenReturn(5);

        int count = behaviorAPI.getPointlessActivityCount(player);

        assertEquals(5, count);
    }

    @Test
    @DisplayName("resetBehaviorData: Hem Behavior Hem BotManager Sıfırlanmalı")
    void testResetData() {
        when(behaviorManager.isEnabled()).thenReturn(true);
        when(behaviorManager.getPlayerData(player)).thenReturn(behaviorData);

        behaviorAPI.resetBehaviorData(player);

        verify(behaviorData, times(1)).reset();
        verify(botDetectionManager, times(1)).resetBotDetectionData(any());
    }
}