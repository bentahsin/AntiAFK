package com.bentahsin.antiafk.api;

import com.bentahsin.antiafk.api.implementation.AIEngineAPIImpl;
import com.bentahsin.antiafk.api.learning.MovementVector;
import com.bentahsin.antiafk.api.learning.Pattern;
import com.bentahsin.antiafk.api.models.PatternMatchResult;
import com.bentahsin.antiafk.learning.PatternManager;
import com.bentahsin.antiafk.managers.ConfigManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class AIEngineAPITest {

    @Mock private PatternManager patternManager;
    @Mock private ConfigManager configManager;

    private AIEngineAPIImpl aiEngine;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        when(configManager.getLearningSearchRadius()).thenReturn(1);
        when(configManager.getLearningSimilarityThreshold()).thenReturn(0.85);

        aiEngine = new AIEngineAPIImpl(patternManager, configManager);
    }

    @Test
    @DisplayName("Aynı Yörüngeler İçin Skor 1.0 (veya çok yakın) Olmalı")
    void testCompareIdenticalTrajectories() {
        List<MovementVector> trajA = new ArrayList<>();
        trajA.add(new MovementVector(1, 0, 0, 0, MovementVector.PlayerAction.IDLE, 1));
        trajA.add(new MovementVector(2, 0, 0, 0, MovementVector.PlayerAction.IDLE, 1));

        List<MovementVector> trajB = new ArrayList<>();
        trajB.add(new MovementVector(1, 0, 0, 0, MovementVector.PlayerAction.IDLE, 1));
        trajB.add(new MovementVector(2, 0, 0, 0, MovementVector.PlayerAction.IDLE, 1));

        double score = aiEngine.compareTrajectories(trajA, trajB);

        assertEquals(1.0, score, 0.001, "Aynı yörüngelerin skoru 1.0 olmalı.");
    }

    @Test
    @DisplayName("Pattern Eşleştirme (FindBestMatch) Doğru Çalışmalı")
    void testFindBestMatch() {
        List<MovementVector> patternVectors = new ArrayList<>();
        patternVectors.add(new MovementVector(5, 5, 0, 0, MovementVector.PlayerAction.JUMP, 10));
        Pattern mockPattern = new Pattern("test_killaura", patternVectors);

        when(patternManager.getKnownPatterns()).thenReturn(Collections.singletonList(mockPattern));

        List<MovementVector> playerVectors = new ArrayList<>();
        for(int i=0; i<25; i++) {
            playerVectors.add(new MovementVector(5, 5, 0, 0, MovementVector.PlayerAction.JUMP, 10));
        }

        PatternMatchResult result = aiEngine.findBestMatch(playerVectors);

        assertNotNull(result);
        verify(patternManager, times(1)).getKnownPatterns();
    }
}