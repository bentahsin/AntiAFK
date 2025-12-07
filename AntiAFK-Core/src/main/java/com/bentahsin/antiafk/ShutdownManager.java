package com.bentahsin.antiafk;

import com.bentahsin.antiafk.behavior.BehaviorAnalysisManager;
import com.bentahsin.antiafk.learning.PatternAnalysisTask;
import com.bentahsin.antiafk.learning.RecordingManager;
import com.bentahsin.antiafk.learning.pool.VectorPoolManager;
import com.bentahsin.antiafk.storage.DatabaseManager;
import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.Singleton;

/**
 * Eklenti devre dışı bırakıldığında tüm servisleri, görevleri ve bağlantıları
 * güvenli bir şekilde sonlandıran yönetici sınıf.
 */
@Singleton
public class ShutdownManager {

    private final RecordingManager recordingManager;
    private final VectorPoolManager vectorPoolManager;
    private final Provider<BehaviorAnalysisManager> behaviorAnalysisManagerProvider;
    private final PatternAnalysisTask patternAnalysisTask;
    private final Provider<DatabaseManager> databaseManagerProvider;

    @Inject
    public ShutdownManager(
            RecordingManager recordingManager,
            VectorPoolManager vectorPoolManager,
            Provider<BehaviorAnalysisManager> behaviorAnalysisManagerProvider,
            PatternAnalysisTask patternAnalysisTask,
            Provider<DatabaseManager> databaseManagerProvider
    ) {
        this.recordingManager = recordingManager;
        this.vectorPoolManager = vectorPoolManager;
        this.behaviorAnalysisManagerProvider = behaviorAnalysisManagerProvider;
        this.patternAnalysisTask = patternAnalysisTask;
        this.databaseManagerProvider = databaseManagerProvider;
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
        BehaviorAnalysisManager bm = behaviorAnalysisManagerProvider.get();
        if (bm != null && bm.isEnabled()) {
            bm.shutdown();
        }
        DatabaseManager dbm = databaseManagerProvider.get();
        if (dbm != null) {
            dbm.disconnect();
        }
    }
}