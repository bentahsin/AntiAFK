package com.bentahsin.antiafk.api.turing;

import org.bukkit.entity.Player;

/**
 * Tüm captcha türlerinin uygulaması gereken temel arayüz.
 */
public interface ICaptcha {

    /**
     * Captcha'nın config.yml'deki adını döndürür (örn: "QUESTION_ANSWER").
     * @return Captcha türünün adı.
     */
    String getTypeName();

    /**
     * Bu captcha testini belirtilen oyuncu için başlatır.
     * @param player Testin uygulanacağı oyuncu.
     */
    void start(Player player);

    /**
     * Oyuncu tarafından kapatılan bir testi (örn: GUI) yeniden açar veya
     * testle ilgili bir hatırlatma gönderir.
     * @param player Testi yeniden açacak oyuncu.
     */
    void reopen(Player player);

    /**
     * Oyuncu sunucudan çıktığında veya test başka bir nedenle iptal edildiğinde
     * çağrılır.
     * @param player Temizlik yapılacak oyuncu.
     */
    void cleanUp(Player player);
}