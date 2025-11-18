package com.bentahsin.antiafk.api;

import org.bukkit.entity.Player;

/**
 * AntiAFK eklentisi ile etkileşime geçmek için ana API arayüzü.
 */
public interface AntiAFKAPI {

    /**
     * Bir oyuncunun şu anda AFK olup olmadığını kontrol eder.
     * @param player Kontrol edilecek oyuncu.
     * @return Oyuncu AFK ise true.
     */
    boolean isAfk(Player player);

    /**
     * Bir oyuncunun kaç saniyedir inaktif olduğunu döndürür.
     * @param player Kontrol edilecek oyuncu.
     * @return Saniye cinsinden süre.
     */
    long getAfkTime(Player player);

    /**
     * Bir oyuncuyu manuel olarak AFK moduna sokar.
     * @param player Oyuncu.
     * @param reason AFK sebebi.
     */
    void setAfk(Player player, String reason);

    /**
     * Bir oyuncunun AFK durumunu kaldırır.
     * @param player Oyuncu.
     */
    void setActive(Player player);
}