package com.bentahsin.antiafk.api.implementation;

import com.bentahsin.antiafk.api.learning.MovementVector;
import com.bentahsin.antiafk.api.learning.Pattern;
import com.bentahsin.antiafk.learning.PatternManager;
import com.bentahsin.antiafk.learning.RecordingManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutionException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

class PatternAPIImplTest {

    @Mock private PatternManager patternManager;
    @Mock private RecordingManager recordingManager;

    private PatternAPIImpl patternAPI;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        patternAPI = new PatternAPIImpl(patternManager, recordingManager);
    }

    @Test
    @DisplayName("getPatternNames: Pattern İsimlerini Liste Olarak Döndürmeli")
    void testGetPatternNames() {
        Pattern p1 = new Pattern("killaura", new ArrayList<>());
        Pattern p2 = new Pattern("spinbot", new ArrayList<>());

        when(patternManager.getKnownPatterns()).thenReturn(Arrays.asList(p1, p2));

        List<String> names = patternAPI.getPatternNames();

        assertEquals(2, names.size());
        assertEquals("killaura", names.get(0));
        assertEquals("spinbot", names.get(1));
    }

    @Test
    @DisplayName("createAndSavePattern: Asenkron Kayıt Yapılmalı")
    void testCreateAndSave() throws ExecutionException, InterruptedException {
        String name = "new_bot";
        List<MovementVector> vectors = new ArrayList<>();

        patternAPI.createAndSavePattern(name, vectors).get();

        verify(recordingManager, times(1)).savePatternToDisk(any(Pattern.class), eq("kryo"));
        verify(patternManager, times(1)).addPattern(any(Pattern.class));
    }

    @Test
    @DisplayName("deletePattern: Bellekten Silme Çağrılmalı")
    void testDeletePattern() {
        patternAPI.deletePattern("old_bot");
        verify(patternManager, times(1)).removePattern("old_bot");
    }
}