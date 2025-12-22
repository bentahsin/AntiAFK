package com.bentahsin.antiafk.api;

import com.bentahsin.antiafk.api.implementation.*;
import com.bentahsin.antiafk.learning.PatternAnalysisTask;
import org.bukkit.entity.Player;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

class AntiAFKAPIIntegrationTest {

    // Mock Alt Implementasyonlar
    @Mock private TuringAPIImpl turingAPI;
    @Mock private PatternAPIImpl patternAPI;
    @Mock private BehaviorAPIImpl behaviorAPI;
    @Mock private AIEngineAPIImpl aiEngineAPI;
    @Mock private PatternAnalysisTask patternAnalysisTask;
    @Mock private Player player;

    private AntiAFKAPIImpl mainApi;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        // Constructor injection (Diğer managerlar null olabilir çünkü test ettiğimiz metotlarda kullanılmıyor)
        mainApi = new AntiAFKAPIImpl(
                null, null, null, null, null,
                patternAnalysisTask, behaviorAPI, patternAPI, turingAPI, aiEngineAPI
        );
    }

    @Test
    @DisplayName("submitCaptchaResult: Başarılı İse forcePass Çağrılmalı")
    void testSubmitPass() {
        mainApi.submitCaptchaResult(player, true, null);

        verify(turingAPI, times(1)).forcePass(player);
        verify(turingAPI, never()).forceFail(any(), anyString());
    }

    @Test
    @DisplayName("submitCaptchaResult: Başarısız İse forceFail Çağrılmalı")
    void testSubmitFail() {
        mainApi.submitCaptchaResult(player, false, "Timeout");

        verify(turingAPI, never()).forcePass(any());
        verify(turingAPI, times(1)).forceFail(player, "Timeout");
    }

    @Test
    @DisplayName("calculateSimilarityScore: İşlem PatternAnalysisTask'a Delege Edilmeli")
    void testSimilarityDelegation() {
        String patternName = "killaura_test";
        when(patternAnalysisTask.calculateScoreForApi(player, patternName)).thenReturn(0.85);
        double result = mainApi.calculateSimilarityScore(player, patternName);
        assertEquals(0.85, result, "API, Task'tan dönen skoru aynen iletmeli.");
        verify(patternAnalysisTask, times(1)).calculateScoreForApi(player, patternName);
    }
}