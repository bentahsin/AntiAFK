package com.bentahsin.antiafk;

import com.bentahsin.antiafk.behavior.BehaviorAnalysisManager;
import com.bentahsin.antiafk.learning.PatternAnalysisTask;
import com.bentahsin.antiafk.learning.RecordingManager;
import com.bentahsin.antiafk.learning.pool.VectorPoolManager;
import com.bentahsin.antiafk.storage.DatabaseManager;
import com.google.inject.Inject;
import com.google.inject.Singleton;

/**
 * Eklenti devre dışı bırakıldığında tüm servisleri, görevleri ve bağlantıları
 * güvenli bir şekilde sonlandıran yönetici sınıf.
 */
@Singleton
public class ShutdownManager {

    private final RecordingManager recordingManager;
    private final VectorPoolManager vectorPoolManager;
    private final BehaviorAnalysisManager behaviorAnalysisManager;
    private final PatternAnalysisTask patternAnalysisTask;
    private final DatabaseManager databaseManager;

    @Inject
    public ShutdownManager(
            RecordingManager recordingManager,
            VectorPoolManager vectorPoolManager,
            BehaviorAnalysisManager behaviorAnalysisManager,
            PatternAnalysisTask patternAnalysisTask,
            DatabaseManager databaseManager
    ) {
        this.recordingManager = recordingManager;
        this.vectorPoolManager = vectorPoolManager;
        this.behaviorAnalysisManager = behaviorAnalysisManager;
        this.patternAnalysisTask = patternAnalysisTask;
        this.databaseManager = databaseManager;
    }

    /**
     * Eklentinin tüm aktif bileşenlerini güvenli bir şekilde kapatır.
     */
    public void shutdown() {
        if (recordingManager != null) {
            recordingManager.clearAllRecordings();
        }
        if (vectorPoolManager != null) {
            vectorPoolManager.close();
        }
        if (patternAnalysisTask != null) {
            patternAnalysisTask.shutdown();
        }
        if (behaviorAnalysisManager != null && behaviorAnalysisManager.isEnabled()) {
            behaviorAnalysisManager.shutdown();
        }
        if (databaseManager != null) {
            databaseManager.disconnect();
        }
    }
}