package com.bentahsin.antiafk.learning;

import com.bentahsin.antiafk.AntiAFKPlugin;
import com.bentahsin.antiafk.language.Lang;
import com.bentahsin.antiafk.language.SystemLanguageManager;
import com.bentahsin.antiafk.learning.serialization.KryoPatternSerializer;
import com.bentahsin.antiafk.managers.ConfigManager;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Bilinen hareket desenlerini diskten yükler ve bellekte yönetir.
 */
public class PatternManager {

    private final Logger logger;
    private final ConfigManager cfgMgr;
    private final SystemLanguageManager sysLang;
    private final File knownRoutesDirectory;
    private final Map<String, Pattern> knownPatterns = new ConcurrentHashMap<>();

    public PatternManager(AntiAFKPlugin plugin) {
        this.logger = plugin.getLogger();
        this.cfgMgr = plugin.getConfigManager();
        this.sysLang = plugin.getSystemLanguageManager();
        this.knownRoutesDirectory = new File(plugin.getDataFolder(), "known_routes");
        if (!knownRoutesDirectory.exists()) {
            boolean ignored = knownRoutesDirectory.mkdirs();
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

        long maxSize = cfgMgr.getMaxPatternFileSizeBytes();
        int maxVectors = cfgMgr.getMaxVectorsPerPattern();

        for (File file : files) {
            if (file.length() > maxSize) {
                logger.warning(sysLang.getSystemMessage(
                        Lang.SKIPPING_PATTERN_FILE_TOO_LARGE,
                        file.getName(),
                        (maxSize / 1024)
                ));
                continue;
            }
            try (FileInputStream fis = new FileInputStream(file)) {
                Pattern pattern = serializer.deserialize(fis);
                if (pattern.getVectors().size() > maxVectors) {
                    logger.warning(sysLang.getSystemMessage(
                            Lang.SKIPPING_PATTERN_TOO_MANY_VECTORS,
                            pattern.getName(),
                            maxVectors
                    ));
                    continue;
                }
                knownPatterns.put(pattern.getName(), pattern);
                logger.info(sysLang.getSystemMessage(
                        Lang.LOADED_KNOWN_PATTERN,
                        pattern.getName()
                ));
            } catch (IOException e) {
                logger.log(Level.WARNING, sysLang.getSystemMessage(
                        Lang.COULD_NOT_LOAD_PATTERN_FILE,
                        file.getName()
                ), e);
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