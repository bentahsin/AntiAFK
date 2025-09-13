package com.bentahsin.antiafk.learning;

import com.bentahsin.antiafk.AntiAFKPlugin;
import com.bentahsin.antiafk.learning.serialization.JsonPatternSerializer;
import com.bentahsin.antiafk.learning.serialization.KryoPatternSerializer;
import com.bentahsin.antiafk.learning.serialization.ISerializer;

import java.io.File;
import java.nio.file.Files;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Level;

public class PatternTransformer {

    private final AntiAFKPlugin plugin;
    private final File recordsDirectory;

    public PatternTransformer(AntiAFKPlugin plugin) {
        this.plugin = plugin;
        this.recordsDirectory = new File(plugin.getDataFolder(), "records");
    }

    public CompletableFuture<Integer> transformAll(String targetFormat) {
        return CompletableFuture.supplyAsync(() -> {
            ISerializer targetSerializer = targetFormat.equals("kryo") ? new KryoPatternSerializer() : new JsonPatternSerializer();
            ISerializer jsonSerializer = new JsonPatternSerializer();
            ISerializer kryoSerializer = new KryoPatternSerializer();

            int convertedCount = 0;
            File[] files = recordsDirectory.listFiles();
            if (files == null) return 0;

            for (File file : files) {
                try {
                    String fileName = file.getName();
                    Pattern pattern;

                    if (fileName.endsWith(".json.pattern")) {
                        pattern = jsonSerializer.deserialize(Files.newInputStream(file.toPath()));
                    } else if (fileName.endsWith(".kryo.pattern")) {
                        pattern = kryoSerializer.deserialize(Files.newInputStream(file.toPath()));
                    } else {
                        continue;
                    }

                    File newFile = new File(recordsDirectory, pattern.getName() + "." + targetSerializer.getFileExtension() + ".pattern");
                    targetSerializer.serialize(pattern, Files.newOutputStream(newFile.toPath()));

                    if (!file.equals(newFile)) {
                        file.delete();
                    }
                    convertedCount++;
                } catch (Exception e) {
                    plugin.getLogger().log(Level.WARNING, "Could not transform pattern file: " + file.getName(), e);
                }
            }
            return convertedCount;
        });
    }
}