package com.bentahsin.antiafk.turing;

import org.bukkit.scheduler.BukkitTask;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Bir oyuncu için aktif bir Turing Testi'ni temsil eden veri sınıfı.
 */
public class CaptchaChallenge {

    private final List<String> correctAnswers;
    private final BukkitTask timeoutTask;

    public CaptchaChallenge(List<String> correctAnswers, BukkitTask timeoutTask) {
        this.correctAnswers = correctAnswers.stream()
                .map(String::toLowerCase)
                .collect(Collectors.toList());
        this.timeoutTask = timeoutTask;
    }

    public BukkitTask getTimeoutTask() {
        return timeoutTask;
    }

    /**
     * Verilen bir cevabın, kabul edilebilir cevaplar listesinde olup olmadığını kontrol eder.
     * @param providedAnswer Oyuncunun verdiği cevap.
     * @return Cevap doğruysa true.
     */
    public boolean isCorrect(String providedAnswer) {
        if (providedAnswer == null) {
            return false;
        }

        return correctAnswers.contains(providedAnswer.toLowerCase().trim());
    }
}