package com.bentahsin.antiafk.api.managers;

import com.bentahsin.antiafk.api.learning.MovementVector;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public interface PatternAPI {
    /**
     * Kayıtlı tüm desen isimlerini getirir.
     */
    List<String> getPatternNames();

    /**
     * Belirli bir deseni yükler. (Pattern sınıfı API'ye taşınmalı)
     */
    Object getPattern(String name);

    /**
     * Yeni bir deseni programatik olarak oluşturur ve kaydeder.
     * Bu, BenthAC'nin kendi "Killaura" desenlerini enjekte etmesini sağlar.
     */
    CompletableFuture<Void> createAndSavePattern(String name, List<MovementVector> vectors);

    /**
     * Bir deseni siler.
     */
    void deletePattern(String name);
}