package com.bentahsin.antiafk.language.provider;

import com.bentahsin.antiafk.language.Lang;
import com.bentahsin.antiafk.language.TranslationProvider;

import java.util.EnumMap;
import java.util.Map;

public class Turkish implements TranslationProvider {
    private final Map<Lang, String> translations = new EnumMap<>(Lang.class);
    public Turkish() {
        add(Lang.BEHAVIOR_ANALYSIS_ENABLED_AND_INIT, "Davranışsal AFK Analizi etkinleştirildi. Başlatılıyor...");
        add(Lang.BEHAVIOR_ANALYSIS_TASK_STOPPED, "Davranışsal AFK Analizi görevi durduruldu.");
        add(Lang.BEHAVIOR_ANALYSIS_DATA_CLEARED, "Davranışsal analiz oyuncu verileri temizlendi.");

        add(Lang.CAPTCHA_QUESTIONS_FILE_EMPTY_OR_INVALID, "questions.yml dosyası boş veya hatalı! Turing Testi çalışmayacak.");

        add(Lang.ANTIAFK_COMMAND_NOT_IN_YML, "AntiAFK komutu (antiafk) plugin.yml dosyasında bulunamadı veya hatalı yapılandırıldı!");
        add(Lang.PLUGIN_COMMANDS_WILL_NOT_WORK, "Plugin komutları çalışmayacak. Lütfen plugin.yml dosyanızı kontrol edin.");
        add(Lang.AFK_COMMAND_NOT_IN_YML, "/afk komutu plugin.yml'de bulunamadı!");
        add(Lang.AFKCEVAP_COMMAND_NOT_IN_YML, "/afkcevap komutu plugin.yml'de bulunamadı!");
        add(Lang.PROTOCOLLIB_FOUND, "ProtocolLib bulundu, kitap düzenleme özelliği aktif.");
        add(Lang.PROTOCOLLIB_NOT_FOUND, "ProtocolLib bulunamadı! Bölge aksiyonları için komut düzenleme özelliği devre dışı bırakıldı.");
        add(Lang.PLACEHOLDERAPI_FOUND, "PlaceholderAPI bulundu ve entegrasyon sağlandı.");
        add(Lang.PLACEHOLDERAPI_NOT_FOUND, "PlaceholderAPI bulunamadı, placeholder özellikleri kısıtlı çalışacak.");
        add(Lang.WORLDGUARD_FOUND, "WorldGuard bulundu ve entegrasyon sağlandı.");
        add(Lang.WORLDGUARD_ENABLED_BUT_NOT_FOUND, "Config'de WorldGuard entegrasyonu aktif fakat sunucuda WorldGuard bulunamadı.");
        add(Lang.PLUGIN_ENABLED_SUCCESSFULLY, "AntiAFK plugini başarıyla başlatıldı!");
        add(Lang.PLUGIN_DISABLED, "AntiAFK plugini devre dışı bırakıldı.");

        add(Lang.MESSAGES_YML_LOADED_SUCCESSFULLY, "messages.yml başarıyla yüklendi.");
        add(Lang.MESSAGES_YML_LOAD_ERROR, "messages.yml dosyası yüklenemedi! Bu kritik bir hatadır.");
        add(Lang.PLUGIN_DISABLE_CRITICAL_ERROR, "Daha fazla hatayı önlemek için eklenti devre dışı bırakılacak.");

        add(Lang.SKIPPING_PATTERN_FILE_TOO_LARGE, "'%s' desen dosyası, maksimum dosya boyutu limitini (%d KB) aştığı için atlanıyor.");
        add(Lang.SKIPPING_PATTERN_TOO_MANY_VECTORS, "'%s' deseni, maksimum vektör sayısı limitini (%d) aştığı için atlanıyor.");
        add(Lang.LOADED_KNOWN_PATTERN, "Bilinen desen yüklendi: %s");
        add(Lang.COULD_NOT_LOAD_PATTERN_FILE, "Desen dosyası yüklenemedi: %s");

        add(Lang.DB_CONNECTION_SUCCESS, "Veritabanı bağlantısı kuruldu ve tablo doğrulandı.");
        add(Lang.DB_CONNECTION_ERROR, "Veritabanına bağlanılamadı!");
        add(Lang.DB_DISCONNECT_SUCCESS, "Veritabanı bağlantısı kapatıldı.");
        add(Lang.DB_DISCONNECT_ERROR, "Veritabanı bağlantısı kapatılamadı!");
        add(Lang.DB_RESETTING_PUNISHMENT_COUNT_INACTIVITY, "Oyuncu %s için ceza sayacı inaktivite nedeniyle sıfırlanıyor.");
        add(Lang.DB_GET_STATS_ERROR, "%s için istatistikler alınamadı.");
        add(Lang.DB_RESET_PUNISHMENT_TO_ZERO_ERROR, "%s için ceza sayacı sıfıra indirilemedi.");
        add(Lang.DB_CREATE_ENTRY_ERROR, "%s için asenkron giriş oluşturulamadı.");
        add(Lang.DB_GET_PUNISHMENT_COUNT_ERROR, "%s için ceza sayısı alınamadı.");
        add(Lang.DB_RESET_PUNISHMENT_COUNT_ERROR, "%s için asenkron ceza sayısı sıfırlanamadı.");
        add(Lang.DB_INCREMENT_PUNISHMENT_COUNT_ERROR, "%s için asenkron ceza sayısı artırılamadı.");
        add(Lang.DB_UPDATE_AFK_TIME_ERROR, "%s için asenkron toplam AFK süresi güncellenemedi.");
        add(Lang.DB_INCREMENT_TESTS_PASSED_ERROR, "%s için asenkron geçilen test sayısı artırılamadı.");
        add(Lang.DB_INCREMENT_TESTS_FAILED_ERROR, "%s için asenkron kalınan test sayısı artırılamadı.");
        add(Lang.DB_GET_TOP_PLAYERS_INVALID_COLUMN, "getTopPlayers metoduna geçersiz sütun adı iletildi: %s");
        add(Lang.DB_GET_TOP_PLAYERS_ERROR, "%s için asenkron en iyi oyuncular alınamadı.");

        add(Lang.INVALID_SOUND_IN_CONFIG, "config.yml 'warnings' bölümünde geçersiz ses adı: %s");

        add(Lang.PATTERN_TRANSFORM_FILE_ERROR, "Desen dosyası dönüştürülemedi: %s");

        add(Lang.PATTERN_SAVED_SUCCESSFULLY, "'%s' deseni başarıyla '%s' dosyasına kaydedildi.");
        add(Lang.PATTERN_SAVE_ERROR, "'%s' deseni kaydedilemedi.");

        add(Lang.VECTOR_POOL_BORROW_ERROR, "MovementVector havuzdan ödünç alınamadı. Geri dönüş olarak yeni bir tane oluşturuluyor.");

        add(Lang.PATTERN_MATCH_FOUND, "Oyuncu %s için '%s' deseniyle eşleşme bulundu. Mesafe: %.2f");
    }
    private void add(Lang key, String message) {
        translations.put(key, message);
    }
    private void addLines(Lang key, String... lines) {
        translations.put(key, String.join("\n", lines));
    }

    @Override
    public String get(Lang key) {
        return translations.getOrDefault(key, "MISSING_TRANSLATION: " + key.name());
    }
}
