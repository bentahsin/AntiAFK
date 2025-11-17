package com.bentahsin.antiafk.language;

import com.bentahsin.antiafk.AntiAFKPlugin;
import com.bentahsin.antiafk.language.provider.*;
import com.google.inject.Singleton;

import java.util.EnumMap;
import java.util.IllegalFormatException;
import java.util.Map;
import java.util.logging.Logger;

@Singleton
public class SystemLanguageManager {

    private final Logger logger;

    private final Map<SupportedLanguage, TranslationProvider> providerMap = new EnumMap<>(SupportedLanguage.class);
    private TranslationProvider activeHardcodedProvider;

    public SystemLanguageManager(AntiAFKPlugin plugin) {
        this.logger = plugin.getLogger();
        providerMap.put(SupportedLanguage.TURKISH, new Turkish());
        providerMap.put(SupportedLanguage.ENGLISH, new English());
        providerMap.put(SupportedLanguage.SPANISH, new Spanish());
        providerMap.put(SupportedLanguage.GERMAN, new German());
        providerMap.put(SupportedLanguage.FRENCH, new French());
        providerMap.put(SupportedLanguage.RUSSIAN, new Russian());
        providerMap.put(SupportedLanguage.POLISH, new Polish());
        setLanguage(SupportedLanguage.TURKISH);
    }

    public void setLanguage(SupportedLanguage language) {
        SupportedLanguage selectedLanguage = (language != null) ? language : SupportedLanguage.TURKISH;
        this.activeHardcodedProvider = providerMap.getOrDefault(selectedLanguage, providerMap.get(SupportedLanguage.TURKISH));
    }

    /**
     * Hard-coded (kod içi) bir sistem mesajı alır.
     * @param key Çeviri anahtarı.
     * @param placeholders Yer tutucular.
     * @return Formatlanmış çeviri.
     */
    public String getSystemMessage(Lang key, Object... placeholders) {
        String message = activeHardcodedProvider.get(key);
        try {
            if (placeholders == null || placeholders.length == 0 || !message.contains("%")) {
                return message;
            }
            return String.format(message, placeholders);
        } catch (IllegalFormatException e) {
            logger.warning("Lang format error for key " + key.name() + ": " + e.getMessage());
            return message;
        }
    }

    /**
     * String tabanlı sistem mesajı metodu.
     * Bu metot, String anahtarı Lang enum sabitine dönüştürerek diğer metodu çağırır.
     * Bu, projenin geri kalanında büyük değişiklikler yapmayı önler.
     * @param baseKeyName Mesajın temel adı (örn: "CFG_MGR_LOADED_LANG").
     * @param placeholders Yer tutucular.
     * @return Formatlanmış çeviri.
     */
    public String getSystemMessage(String baseKeyName, Object... placeholders) {
        try {
            Lang key = Lang.valueOf(baseKeyName.toUpperCase());
            return getSystemMessage(key, placeholders);
        } catch (IllegalArgumentException e) {
            logger.severe("CRITICAL: Hard-coded Lang enum constant not found for key: " + baseKeyName);
            return "[Lang Error: " + baseKeyName + "]";
        }
    }
}