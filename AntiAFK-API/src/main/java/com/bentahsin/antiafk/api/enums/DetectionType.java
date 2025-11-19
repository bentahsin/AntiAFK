package com.bentahsin.antiafk.api.enums;

/**
 * AntiAFK'nın bir oyuncudan şüphelenme sebepleri.
 */
public enum DetectionType {
    /**
     * Sürekli aynı yörüngede hareket etme (BehaviorAnalysisTask).
     */
    BEHAVIORAL_REPETITION,

    /**
     * Öğrenilmiş bir bot deseniyle eşleşme (PatternAnalysisTask).
     */
    LEARNED_PATTERN,

    /**
     * Tutarlı tıklama aralıkları (Auto-Clicker).
     */
    AUTO_CLICKER,

    /**
     * Aynı noktada sürekli anlamsız etkileşimler (Eşya atma, eğilme vb.).
     */
    POINTLESS_ACTIVITY,

    /**
     * İnsanüstü hızda sürekli dünya değiştirme.
     */
    RAPID_WORLD_CHANGE,

    /**
     * Diğer veya bilinmeyen sebepler.
     */
    OTHER
}