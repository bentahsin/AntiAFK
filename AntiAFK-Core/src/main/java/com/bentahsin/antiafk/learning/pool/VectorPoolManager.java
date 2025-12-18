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
     * Havuzdan bir MovementVector nesnesi ödünç alır.
     * @param dx X eksenindeki değişim
     * @param dz Z eksenindeki değişim
     * @param dYaw Yaw açısındaki değişim
     * @param dPitch Pitch açısındaki değişim
     * @param action Oyuncu eylemi
     * @param durationTicks Süre (tick cinsinden)
     * @return Ödünç alınan veya yeni oluşturulan MovementVector nesnesi.
     */
    public MovementVector borrowVector(double dx, double dz, double dYaw, double dPitch, MovementVector.PlayerAction action, int durationTicks) {
        try {
            MovementVector vector = pool.borrowObject();
            vector.reinitialize(dx, dz, dYaw, dPitch, action, durationTicks);
            debugMgr.log(DebugManager.DebugModule.LEARNING_MODE,
                    "Borrowed vector from pool. Active: %d, Idle: %d",
                    pool.getNumActive(), pool.getNumIdle()
            );
            return vector;
        } catch (Exception e) {
            plugin.getLogger().log(Level.WARNING, sysLang.getSystemMessage(Lang.VECTOR_POOL_BORROW_ERROR), e);
            return new MovementVector(dx, dz, dYaw, dPitch, action, durationTicks);
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