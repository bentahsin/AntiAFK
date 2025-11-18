package com.bentahsin.antiafk.learning.pool;

import com.bentahsin.antiafk.AntiAFKPlugin;
import com.bentahsin.antiafk.language.Lang;
import com.bentahsin.antiafk.language.SystemLanguageManager;
import com.bentahsin.antiafk.learning.MovementVector;
import com.bentahsin.antiafk.managers.DebugManager;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import org.apache.commons.math3.geometry.euclidean.twod.Vector2D;
import org.apache.commons.pool2.impl.GenericObjectPool;
import org.apache.commons.pool2.impl.GenericObjectPoolConfig;

import java.time.Duration;
import java.util.logging.Level;

/**
 * MovementVector nesne havuzunu yönetir.
 */
@Singleton
public class VectorPoolManager {

    private final GenericObjectPool<MovementVector> pool;
    private final AntiAFKPlugin plugin;
    private final SystemLanguageManager sysLang;
    private final DebugManager debugMgr;

    @Inject
    public VectorPoolManager(AntiAFKPlugin plugin, DebugManager debugMgr, SystemLanguageManager sysLang) {
        this.plugin = plugin;
        this.sysLang = sysLang;
        this.debugMgr = debugMgr;

        GenericObjectPoolConfig<MovementVector> config = new GenericObjectPoolConfig<>();
        config.setMaxTotal(2000);

        config.setMaxWait(Duration.ofMillis(100));

        this.pool = new GenericObjectPool<>(new MovementVectorFactory(), config);
    }

    /**
     * Havuzdan bir MovementVector nesnesi ödünç alır ve yeniden başlatır.
     * @param posChange Pozisyonel değişiklik.
     * @param rotChange Rotasyonel değişiklik.
     * @param action Yapılan eylem.
     * @param durationTicks Eylemin/hareketin süresi (tick).
     * @return Kullanıma hazır bir MovementVector nesnesi.
     */
    public MovementVector borrowVector(Vector2D posChange, Vector2D rotChange, MovementVector.PlayerAction action, int durationTicks) {
        try {
            MovementVector vector = pool.borrowObject();
            vector.reinitialize(posChange, rotChange, action, durationTicks);
            debugMgr.log(DebugManager.DebugModule.LEARNING_MODE,
                    "Borrowed vector from pool. Active: %d, Idle: %d",
                    pool.getNumActive(), pool.getNumIdle()
            );
            return vector;
        } catch (Exception e) {
            plugin.getLogger().log(Level.WARNING, sysLang.getSystemMessage(Lang.VECTOR_POOL_BORROW_ERROR), e);
            return new MovementVector(posChange, rotChange, action, durationTicks);
        }
    }

    /**
     * Kullanımı biten bir MovementVector nesnesini havuza geri bırakır.
     * @param vector Havuza iade edilecek nesne.
     */
    public void returnVector(MovementVector vector) {
        if (vector != null) {
            try {
                pool.returnObject(vector);
                debugMgr.log(DebugManager.DebugModule.LEARNING_MODE,
                        "Returned vector to pool. Active: %d, Idle: %d",
                        pool.getNumActive(), pool.getNumIdle()
                );
            } catch (IllegalStateException ignored) { }
        }
    }

    /**
     * Eklenti devre dışı bırakıldığında havuzu kapatır.
     */
    public void close() {
        pool.close();
    }
}