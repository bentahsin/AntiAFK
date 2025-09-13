package com.bentahsin.antiafk.learning;

import com.bentahsin.antiafk.AntiAFKPlugin;
import com.bentahsin.antiafk.learning.serialization.KryoPatternSerializer;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;

/**
 * Bilinen hareket desenlerini diskten yükler ve bellekte yönetir.
 */
public class PatternManager {

    private final AntiAFKPlugin plugin;
    private final File knownRoutesDirectory;
    private final Map<String, Pattern> knownPatterns = new ConcurrentHashMap<>();

    public PatternManager(AntiAFKPlugin plugin) {
        this.plugin = plugin;
        this.knownRoutesDirectory = new File(plugin.getDataFolder(), "known_routes");
        if (!knownRoutesDirectory.exists()) {
            knownRoutesDirectory.mkdirs();
        }
    }

    /**
     * 'known_routes' klasöründeki tüm .kryo.pattern dosyalarını belleğe yükler.
     */
    public void loadPatterns() {
        knownPatterns.clear();
        KryoPatternSerializer serializer = new KryoPatternSerializer();
        File[] files = knownRoutesDirectory.listFiles((dir, name) -> name.endsWith("." + serializer.getFileExtension() + ".pattern"));

        if (files == null) return;

        long maxSize = plugin.getConfigManager().getMaxPatternFileSizeBytes();
        int maxVectors = plugin.getConfigManager().getMaxVectorsPerPattern();

        for (File file : files) {
            if (file.length() > maxSize) {
                plugin.getLogger().warning("Skipping pattern file '" + file.getName() + "' because it exceeds the max file size limit (" + (maxSize / 1024) + " KB).");
                continue;
            }
            try (FileInputStream fis = new FileInputStream(file)) {
                Pattern pattern = serializer.deserialize(fis);
                if (pattern.getVectors().size() > maxVectors) {
                    plugin.getLogger().warning("Skipping pattern '" + pattern.getName() + "' because it exceeds the max vector count limit (" + maxVectors + ").");
                    continue;
                }
                knownPatterns.put(pattern.getName(), pattern);
                plugin.getLogger().info("Loaded known pattern: " + pattern.getName());
            } catch (IOException e) {
                plugin.getLogger().log(Level.WARNING, "Could not load pattern file: " + file.getName(), e);
            }
        }
    }

    /**
     * Bellekteki tüm bilinen desenleri bir koleksiyon olarak döndürür.
     * @return Bilinen desenlerin bir koleksiyonu.
     */
    public Collection<Pattern> getKnownPatterns() {
        return knownPatterns.values();
    }
}