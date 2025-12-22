package com.bentahsin.antiafk.api.implementation;

import com.bentahsin.antiafk.turing.CaptchaManager;
import com.google.inject.Provider;
import org.bukkit.entity.Player;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

class TuringAPIImplTest {

    @Mock private Provider<CaptchaManager> captchaManagerProvider;
    @Mock private CaptchaManager captchaManager;
    @Mock private Player player;

    private TuringAPIImpl turingAPI;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        when(captchaManagerProvider.get()).thenReturn(captchaManager);

        turingAPI = new TuringAPIImpl(captchaManagerProvider);
    }

    @Test
    @DisplayName("openCaptcha: CaptchaManager'a Start Komutu Gitmeli")
    void testOpenCaptcha() {
        turingAPI.openCaptcha(player, "COLOR_PALETTE");
        verify(captchaManager, times(1)).startChallenge(player);
    }

    @Test
    @DisplayName("forcePass: Sadece Testteki Oyuncu Geçirilmeli")
    void testForcePass() {
        when(captchaManager.isBeingTested(player)).thenReturn(true);
        turingAPI.forcePass(player);
        verify(captchaManager, times(1)).passChallenge(player);

        reset(captchaManager);
        when(captchaManager.isBeingTested(player)).thenReturn(false);
        turingAPI.forcePass(player);

        verify(captchaManager, never()).passChallenge(player);
    }

    @Test
    @DisplayName("isBeingTested: Doğru Sonuç Dönmeli")
    void testIsBeingTested() {
        when(captchaManager.isBeingTested(player)).thenReturn(true);
        assertTrue(turingAPI.isBeingTested(player));

        when(captchaManager.isBeingTested(player)).thenReturn(false);
        assertFalse(turingAPI.isBeingTested(player));
    }
}