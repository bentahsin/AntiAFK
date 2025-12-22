package com.bentahsin.antiafk.api.managers;

import com.bentahsin.antiafk.api.models.PatternMatchResult;
import com.bentahsin.antiafk.api.learning.MovementVector;
import java.util.List;

public interface AIEngineAPI {

    /**
     * İki farklı vektör listesini (yörüngeyi) karşılaştırır.
     *
     * @param trajectoryA Birinci yörünge (Örn: Oyuncu)
     * @param trajectoryB İkinci yörünge (Örn: Bot Deseni)
     * @return 0.0 (Benzemiyor) - 1.0 (Birebir Aynı) arası skor.
     */
    double compareTrajectories(List<MovementVector> trajectoryA, List<MovementVector> trajectoryB);

    /**
     * Verilen yörüngenin, sistemdeki HERHANGİ bir desenle eşleşip eşleşmediğini kontrol eder.
     *
     * @param trajectory Analiz edilecek hareket geçmişi.
     * @return En iyi eşleşme sonucu.
     */
    PatternMatchResult findBestMatch(List<MovementVector> trajectory);
}