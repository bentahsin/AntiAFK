package com.bentahsin.antiafk.api.models;

/**
 * Yapay zeka motorunun analiz sonucunu taşıyan veri sınıfı.
 * En iyi eşleşen deseni ve benzerlik skorunu tutar.
 */
public class PatternMatchResult {
    private final String patternName;
    private final double similarityScore;
    private final boolean isMatch;

    public PatternMatchResult(String patternName, double similarityScore, boolean isMatch) {
        this.patternName = patternName;
        this.similarityScore = similarityScore;
        this.isMatch = isMatch;
    }

    /**
     * Eşleşen desenin adı (Dosya adı).
     * Eşleşme yoksa null dönebilir.
     */
    public String getPatternName() {
        return patternName;
    }

    /**
     * Benzerlik skoru (0.0 - 1.0 arası).
     * 1.0 = Birebir aynı.
     */
    public double getSimilarityScore() {
        return similarityScore;
    }

    /**
     * Bu skorun, config'deki eşik değerini geçip geçmediği.
     * (Yani sistem bunu bot olarak işaretledi mi?)
     */
    public boolean isMatch() {
        return isMatch;
    }
}