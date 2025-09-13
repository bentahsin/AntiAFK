package com.bentahsin.antiafk.language;

import java.util.Arrays;

public enum SupportedLanguage {
    TURKISH("Turkish", "_TR"),
    ENGLISH("English", "_EN"),
    SPANISH("Spanish", "_ES"),
    GERMAN("German", "_DE"),
    FRENCH("French", "_FR"),
    RUSSIAN("Russian", "_RU"),
    POLISH("Polish", "_PL");


    private final String configName;
    private final String keySuffix;

    SupportedLanguage(String configName, String keySuffix) {
        this.configName = configName;
        this.keySuffix = keySuffix;
    }

    public String getConfigName() {
        return configName;
    }

    public String getKeySuffix() {
        return keySuffix;
    }

    /**
     * Config dosyasında belirtilen metne göre uygun enum sabitini bulur.
     * @param configValue config.yml'den okunan dil adı.
     * @return Eşleşen SupportedLanguage sabiti veya bulunamazsa varsayılan (TURKISH).
     */
    public static SupportedLanguage fromConfigName(String configValue) {
        if (configValue == null) {
            return TURKISH;
        }
        return Arrays.stream(values())
                .filter(lang -> lang.getConfigName().equalsIgnoreCase(configValue))
                .findFirst()
                .orElse(TURKISH);
    }
}