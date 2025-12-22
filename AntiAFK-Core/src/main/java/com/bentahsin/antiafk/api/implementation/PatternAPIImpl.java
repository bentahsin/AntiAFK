package com.bentahsin.antiafk.api.implementation;

import com.bentahsin.antiafk.api.learning.MovementVector;
import com.bentahsin.antiafk.api.learning.Pattern;
import com.bentahsin.antiafk.api.managers.PatternAPI;
import com.bentahsin.antiafk.learning.PatternManager;
import com.bentahsin.antiafk.learning.RecordingManager;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

@Singleton
public class PatternAPIImpl implements PatternAPI {

    private final PatternManager patternManager;
    private final RecordingManager recordingManager;

    @Inject
    public PatternAPIImpl(PatternManager patternManager, RecordingManager recordingManager) {
        this.patternManager = patternManager;
        this.recordingManager = recordingManager;
    }

    @Override
    public List<String> getPatternNames() {
        return patternManager.getKnownPatterns().stream()
                .map(Pattern::getName)
                .collect(Collectors.toList());
    }

    @Override
    public Pattern getPattern(String name) {
        return patternManager.getPattern(name);
    }

    @Override
    public CompletableFuture<Void> createAndSavePattern(String name, List<MovementVector> vectors) {
        return CompletableFuture.runAsync(() -> {
            Pattern pattern = new Pattern(name, new ArrayList<>(vectors));
            recordingManager.savePatternToDisk(pattern, "kryo");
            patternManager.addPattern(pattern);
        });
    }

    @Override
    public void deletePattern(String name) {
        patternManager.removePattern(name);

        File recordsDir = new File("plugins/AntiAFK/records");
        File knownRoutesDir = new File("plugins/AntiAFK/known_routes");

        deleteFileIfExists(recordsDir, name);
        deleteFileIfExists(knownRoutesDir, name);
    }

    private void deleteFileIfExists(File dir, String name) {
        File json = new File(dir, name + ".json.pattern");
        if (json.exists()) {
            boolean ignored = json.delete();
        }

        File kryo = new File(dir, name + ".kryo.pattern");
        if (kryo.exists()) {
            boolean ignored = json.delete();
        }
    }
}