package com.bentahsin.antiafk.language;

import java.util.Arrays;

public enum SupportedLanguage {
    TURKISH("Turkish"),
    ENGLISH("English"),
    SPANISH("Spanish"),
    GERMAN("German"),
    FRENCH("French"),
    RUSSIAN("Russian"),
    POLISH("Polish");


    private final String configName;

    SupportedLanguage(String configName) {
        this.configName = configName;
    }

    public String getConfigName() {
        return configName;
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