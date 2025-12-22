package com.bentahsin.antiafk.api.managers;

import org.bukkit.entity.Player;

public interface TuringAPI {
    /**
     * Oyuncuya zorla belirli bir Captcha türünü açar.
     */
    void openCaptcha(Player player, String captchaType);

    /**
     * Oyuncunun aktif testini (varsa) kapatır ve başarılı sayar.
     */
    void forcePass(Player player);

    /**
     * Oyuncunun aktif testini kapatır ve başarısız sayar (Cezalandırır).
     */
    void forceFail(Player player, String reason);

    /**
     * Oyuncunun şu an test çözüp çözmediğini kontrol eder.
     */
    boolean isBeingTested(Player player);
}