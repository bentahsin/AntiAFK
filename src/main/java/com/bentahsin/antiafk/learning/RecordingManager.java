package com.bentahsin.antiafk.learning;

import com.bentahsin.antiafk.AntiAFKPlugin;
import com.bentahsin.antiafk.language.Lang;
import com.bentahsin.antiafk.language.SystemLanguageManager;
import com.bentahsin.antiafk.learning.pool.VectorPoolManager;
import com.bentahsin.antiafk.learning.serialization.JsonPatternSerializer;
import com.bentahsin.antiafk.learning.serialization.KryoPatternSerializer;
import com.bentahsin.antiafk.learning.serialization.ISerializer;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;

/**
 * Oyuncu hareket desenlerini kaydetme sürecini yönetir.
 */
public class RecordingManager {

    private final AntiAFKPlugin plugin;
    private final SystemLanguageManager sysLang;
    private final VectorPoolManager vectorPoolManager;
    private final Map<UUID, List<MovementVector>> activeRecordings = new ConcurrentHashMap<>();
    private final File recordsDirectory;

    public RecordingManager(AntiAFKPlugin plugin) {
        this.plugin = plugin;
        this.sysLang = plugin.getSystemLanguageManager();
        this.vectorPoolManager = plugin.getVectorPoolManager();
        this.recordsDirectory = new File(plugin.getDataFolder(), "records");
        if (!recordsDirectory.exists()) {
            boolean ignored = recordsDirectory.mkdirs();
        }
    }

    public boolean startRecording(Player player) {
        if (isRecording(player)) {
            return false;
        }
        activeRecordings.put(player.getUniqueId(), new ArrayList<>());
        return true;
    }

    public boolean stopRecording(Player player, String patternName, String format) {
        final List<MovementVector> vectors = activeRecordings.remove(player.getUniqueId());

        if (vectors == null || vectors.isEmpty()) {
            return false;
        }

        Pattern pattern = new Pattern(patternName, new ArrayList<>(vectors));

        CompletableFuture.runAsync(() -> {
            ISerializer serializer = format.equalsIgnoreCase("kryo")
                    ? new KryoPatternSerializer()
                    : new JsonPatternSerializer();

            File file = new File(recordsDirectory, patternName + "." + serializer.getFileExtension() + ".pattern");
            try (FileOutputStream fos = new FileOutputStream(file)) {
                serializer.serialize(pattern, fos);
                plugin.getLogger().info(sysLang.getSystemMessage(
                        Lang.PATTERN_SAVED_SUCCESSFULLY,
                        patternName,
                        file.getName()
                ));
            } catch (IOException e) {
                plugin.getLogger().log(Level.SEVERE, sysLang.getSystemMessage(
                        Lang.PATTERN_SAVE_ERROR,
                        patternName
                ), e);
            } finally {
                vectors.forEach(vectorPoolManager::returnVector);
            }
        });

        return true;
    }

    public boolean isRecording(Player player) {
        return activeRecordings.containsKey(player.getUniqueId());
    }

    public void addVectorIfRecording(UUID uuid, MovementVector vector) {
        List<MovementVector> vectors = activeRecordings.get(uuid);
        if (vectors != null) {
            vectors.add(vector);
        }
    }

    public boolean cancelRecording(Player player) {
        List<MovementVector> vectors = activeRecordings.remove(player.getUniqueId());
        if (vectors != null) {
            vectors.forEach(vectorPoolManager::returnVector);
            return true;
        }
        return false;
    }

    public void clearAllRecordings() {
        activeRecordings.values().forEach(list -> list.forEach(vectorPoolManager::returnVector));
        activeRecordings.clear();
    }

    public void onPlayerQuit(Player player) {
        cancelRecording(player);
    }
}