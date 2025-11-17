package com.bentahsin.antiafk.managers;

import com.bentahsin.antiafk.AntiAFKPlugin;
import org.bukkit.configuration.ConfigurationSection;

import java.util.EnumMap;
import java.util.Map;
import java.util.logging.Logger;

/**
 * Eklentinin modüler hata ayıklama (debug) sistemini yönetir.
 * Konsola sadece yapılandırmada etkinleştirilmiş modüller için log basar.
 */
public class DebugManager {

    /**
     * Hata ayıklama mesajlarının ilişkilendirilebileceği farklı eklenti modüllerini temsil eder.
     * Bu enum'daki her bir değer, config.yml'deki bir anahtara karşılık gelir.
     */
    public enum DebugModule {
        ACTIVITY_LISTENER,
        BEHAVIORAL_ANALYSIS,
        LEARNING_MODE,
        COMMAND_REGISTRATION,
        DATABASE_QUERIES
    }

    private final Logger logger;
    private boolean isMasterEnabled;

    private final Map<DebugModule, Boolean> moduleStates = new EnumMap<>(DebugModule.class);

    public DebugManager(AntiAFKPlugin plugin) {
        this.logger = plugin.getLogger();
        loadConfigSettings(plugin);
    }

    /**
     * config.yml dosyasından tüm debug ayarlarını okur ve dahili durumu günceller.
     * Bu metot, eklenti başlangıcında ve yeniden yüklendiğinde çağrılmalıdır.
     * @param plugin Config dosyasına erişmek için ana eklenti örneği.
     */
    public void loadConfigSettings(AntiAFKPlugin plugin) {
        moduleStates.clear();

        ConfigurationSection debugSection = plugin.getConfig().getConfigurationSection("debug");

        if (debugSection == null || !debugSection.getBoolean("enabled", false)) {
            isMasterEnabled = false;
            return;
        }

        isMasterEnabled = true;
        ConfigurationSection modulesSection = debugSection.getConfigurationSection("modules");

        for (DebugModule module : DebugModule.values()) {
            boolean isEnabled = false;
            if (modulesSection != null) {
                String configKey = module.name().toLowerCase();
                isEnabled = modulesSection.getBoolean(configKey, false);
            }
            moduleStates.put(module, isEnabled);
        }
    }

    /**
     * Belirtilen modül için bir debug mesajı loglar.
     * Mesaj, sadece ana şalter ve ilgili modül aktifse konsola yazdırılır.
     *
     * @param module Hangi modülle ilgili olduğu.
     * @param message Loglanacak mesaj (String.format stili yer tutucular içerebilir).
     * @param args Mesajdaki yer tutucular için argümanlar.
     */
    public void log(DebugModule module, String message, Object... args) {
        if (!isMasterEnabled || !moduleStates.getOrDefault(module, false)) {
            return;
        }

        String formattedMessage = String.format(message, args);
        logger.info(String.format("[Debug][%s] %s", module.name(), formattedMessage));
    }

    /**
     * Bir modülün debug modunun aktif olup olmadığını kontrol eder.
     * Bu, sadece log mesajı oluşturmak bile maliyetliyse (örn: bir döngü içinde),
     * gereksiz işlem yapılmasını önlemek için kullanılır.
     *
     * @param module Kontrol edilecek modül.
     * @return Modülün hata ayıklaması aktifse true.
     */
    public boolean isEnabled(DebugModule module) {
        return isMasterEnabled && moduleStates.getOrDefault(module, false);
    }
}