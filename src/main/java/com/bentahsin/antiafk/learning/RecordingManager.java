package com.bentahsin.antiafk.learning;

import com.bentahsin.antiafk.AntiAFKPlugin;
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
 * Bu sınıf, ana sunucu iş parçacığında (main thread) çalışır ve
 * asenkron analiz görevinden gelen verileri işler.
 */
public class RecordingManager {

    private final AntiAFKPlugin plugin;
    private final Map<UUID, List<MovementVector>> activeRecordings = new ConcurrentHashMap<>();
    private final File recordsDirectory;

    public RecordingManager(AntiAFKPlugin plugin) {
        this.plugin = plugin;
        this.recordsDirectory = new File(plugin.getDataFolder(), "records");
        if (!recordsDirectory.exists()) {
            recordsDirectory.mkdirs();
        }
    }

    /**
     * Bir oyuncu için hareket deseni kaydını başlatır.
     * @param player Kaydedilecek oyuncu.
     * @return Kayıt başarıyla başlarsa true.
     */
    public boolean startRecording(Player player) {
        if (isRecording(player)) {
            return false;
        }
        activeRecordings.put(player.getUniqueId(), new ArrayList<>());
        return true;
    }

    /**
     * Bir oyuncunun hareket deseni kaydını durdurur ve dosyaya asenkron olarak yazar.
     * @param player Kaydı durdurulacak oyuncu.
     * @param patternName Desene verilecek isim.
     * @param format Kaydedilecek format ("json" veya "kryo").
     * @return Kayıt başarıyla durdurulup kaydedilirse true.
     */
    public boolean stopRecording(Player player, String patternName, String format) {
        List<MovementVector> vectors = activeRecordings.remove(player.getUniqueId());

        if (vectors == null || vectors.isEmpty()) {
            return false;
        }

        Pattern pattern = new Pattern(patternName, vectors);

        CompletableFuture.runAsync(() -> {
            ISerializer serializer = format.equalsIgnoreCase("kryo")
                    ? new KryoPatternSerializer()
                    : new JsonPatternSerializer();

            File file = new File(recordsDirectory, patternName + "." + serializer.getFileExtension() + ".pattern");
            try (FileOutputStream fos = new FileOutputStream(file)) {
                serializer.serialize(pattern, fos);
                plugin.getLogger().info("Pattern '" + patternName + "' successfully saved to " + file.getName());
            } catch (IOException e) {
                plugin.getLogger().log(Level.SEVERE, "Could not save pattern '" + patternName + "'", e);
            }
        });

        return true;
    }

    /**
     * Bir oyuncunun anlık olarak kaydedilip kaydedilmediğini kontrol eder.
     * @param player Kontrol edilecek oyuncu.
     * @return Oyuncu kayıttaysa true.
     */
    public boolean isRecording(Player player) {
        return activeRecordings.containsKey(player.getUniqueId());
    }

    /**
     * Eğer oyuncu kayıt modundaysa, yeni bir hareket vektörü ekler.
     * Bu metot, PatternAnalysisTask tarafından, kuyruktan veri işlenirken çağrılır.
     * @param uuid Oyuncunun UUID'si.
     * @param vector Eklenecek hareket vektörü.
     */
    public void addVectorIfRecording(UUID uuid, MovementVector vector) {
        List<MovementVector> vectors = activeRecordings.get(uuid);
        if (vectors != null) {
            vectors.add(vector);
        }
    }

    /**
     * Bir oyuncu için devam eden bir kaydı, dosyaya yazmadan iptal eder.
     * @param player Kaydı iptal edilecek oyuncu.
     * @return Kayıt bulunup iptal edildiyse true.
     */
    public boolean cancelRecording(Player player) {
        return activeRecordings.remove(player.getUniqueId()) != null;
    }

    /**
     * Oyuncu sunucudan çıktığında, eğer kayıttaysa kaydı verileri kaydetmeden iptal eder.
     * @param player Çıkan oyuncu.
     */
    public void onPlayerQuit(Player player) {
        activeRecordings.remove(player.getUniqueId());
    }
}