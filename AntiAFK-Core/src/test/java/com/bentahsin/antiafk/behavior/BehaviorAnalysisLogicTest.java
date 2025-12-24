package com.bentahsin.antiafk.behavior;

import com.bentahsin.antiafk.AntiAFKPlugin;
import com.bentahsin.antiafk.api.enums.DetectionType;
import com.bentahsin.antiafk.managers.AFKManager;
import com.bentahsin.antiafk.managers.BotDetectionManager;
import com.bentahsin.antiafk.managers.ConfigManager;
import com.bentahsin.antiafk.managers.DebugManager;
import com.bentahsin.antiafk.managers.PlayerStateManager;
import com.google.inject.Provider;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.MockitoAnnotations;

import java.util.Collections;

import static org.mockito.Mockito.*;

class BehaviorAnalysisLogicTest {

    private BehaviorAnalysisTask task;
    private MockedStatic<Bukkit> bukkitMock;

    @Mock private AntiAFKPlugin plugin;
    @Mock private Provider<BehaviorAnalysisManager> behaviorManagerProvider;
    @Mock private BehaviorAnalysisManager behaviorManager;
    @Mock private ConfigManager configManager;
    @Mock private DebugManager debugManager;
    @Mock private Provider<AFKManager> afkManagerProvider;
    @Mock private AFKManager afkManager;
    @Mock private BotDetectionManager botDetectionManager;
    @Mock private PlayerStateManager stateManager;
    @Mock private Player player;
    @Mock private PlayerBehaviorData playerData;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        bukkitMock = mockStatic(Bukkit.class);
        bukkitMock.when(Bukkit::getOnlinePlayers).thenReturn(Collections.singletonList(player));

        when(behaviorManagerProvider.get()).thenReturn(behaviorManager);
        when(afkManagerProvider.get()).thenReturn(afkManager);
        when(afkManager.getBotDetectionManager()).thenReturn(botDetectionManager);
        when(afkManager.getStateManager()).thenReturn(stateManager);

        when(behaviorManager.getPlayerData(player)).thenReturn(playerData);
        when(player.isOnline()).thenReturn(true);
        when(stateManager.isEffectivelyAfk(player)).thenReturn(false);

        task = new BehaviorAnalysisTask(
                plugin,
                behaviorManagerProvider,
                configManager,
                debugManager,
                afkManagerProvider
        );
    }

    @AfterEach
    void tearDown() {
        bukkitMock.close();
    }

    @Test
    @DisplayName("Hapsedilme TESPİTİ: Süre + Mesafe aşılırsa challenge tetiklenmeli ve veri sıfırlanmalı")
    void testConfinementViolation() {
        when(configManager.isConfinementCheckEnabled()).thenReturn(true);
        when(configManager.getConfinementCheckDurationMillis()).thenReturn(1200000L); // 20m
        when(configManager.getConfinementMinDistance()).thenReturn(100.0);

        when(playerData.getConfinementDuration()).thenReturn(1300000L);
        when(playerData.getTotalDistanceTraveled()).thenReturn(500.0);

        task.run();
        verify(botDetectionManager, times(1)).triggerSuspicionAndChallenge(
                eq(player),
                eq("behavior.afk_pool_detected"),
                eq(DetectionType.POINTLESS_ACTIVITY)
        );
        verify(playerData, times(1)).reset();
    }

    @Test
    @DisplayName("Fresh Tracking: Süre dolsa bile mesafe düşükse ceza verme ama veriyi sıfırla")
    void testConfinementFreshTracking() {
        when(configManager.isConfinementCheckEnabled()).thenReturn(true);
        when(configManager.getConfinementCheckDurationMillis()).thenReturn(1200000L);
        when(configManager.getConfinementMinDistance()).thenReturn(100.0);

        when(playerData.getConfinementDuration()).thenReturn(1300000L);
        when(playerData.getTotalDistanceTraveled()).thenReturn(5.0);

        task.run();
        verify(botDetectionManager, never()).triggerSuspicionAndChallenge(any(), any(), any());
        verify(playerData, times(1)).reset();
    }

    @Test
    @DisplayName("Bypass Kontrolü: Oyuncu zaten AFK ise analiz atlanmalı")
    void testSkipIfAlreadyAfk() {
        when(stateManager.isEffectivelyAfk(player)).thenReturn(true);

        task.run();

        verify(playerData, never()).getConfinementDuration();
    }
}