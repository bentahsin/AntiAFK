package com.bentahsin.antiafk.turing;

import org.bukkit.scheduler.BukkitTask;

/**
 * Bir oyuncu için aktif bir Turing Testi'ni temsil eden veri sınıfı.
 * Bu sınıfın sorumluluğu, bir testin verilerini tutmak ve cevap kontrolünü yapmaktır.
 * Soru ve cevap gibi dahili veriler dışarıya açılmaz.
 */
public class CaptchaChallenge {

    private final String answer;
    private final BukkitTask timeoutTask;

    public CaptchaChallenge(String answer, BukkitTask timeoutTask) {
        this.answer = answer.toLowerCase();
        this.timeoutTask = timeoutTask;
    }

    /**
     * Zaman aşımı görevini döndürür. Bu, test başarılı olduğunda
     * veya oyuncu çıktığında görevi iptal etmek için gereklidir.
     * @return BukkitTask nesnesi.
     */
    public BukkitTask getTimeoutTask() {
        return timeoutTask;
    }

    /**
     * Verilen bir cevabın doğru olup olmadığını kontrol eder.
     * Bu metot, null değerlere ve baş/sondaki boşluklara karşı güvenlidir.
     * @param providedAnswer Oyuncunun verdiği cevap.
     * @return Cevap doğruysa true.
     */
    public boolean isCorrect(String providedAnswer) {
        if (providedAnswer == null) {
            return false;
        }
        return providedAnswer.toLowerCase().trim().equals(this.answer);
    }
}