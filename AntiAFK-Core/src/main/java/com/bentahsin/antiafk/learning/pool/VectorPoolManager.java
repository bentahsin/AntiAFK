package com.bentahsin.antiafk.learning.pool;

import com.bentahsin.antiafk.AntiAFKPlugin;
import com.bentahsin.antiafk.api.learning.MovementVector;
import com.bentahsin.antiafk.managers.DebugManager;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import org.apache.commons.pool2.impl.GenericObjectPool;
import org.apache.commons.pool2.impl.GenericObjectPoolConfig;

/**
 * MovementVector nesne havuzunu yönetir.
 */
@Singleton
public class VectorPoolManager {

    private final GenericObjectPool<MovementVector> pool;
    private final AntiAFKPlugin plugin;
    private final DebugManager debugMgr;

    @Inject
    public VectorPoolManager(AntiAFKPlugin plugin, DebugManager debugMgr) {
        this.plugin = plugin;
        this.debugMgr = debugMgr;

        GenericObjectPoolConfig<MovementVector> config = new GenericObjectPoolConfig<>();
        config.setMaxTotal(10000);
        config.setMaxIdle(2000);
        config.setMinIdle(100);

        config.setBlockWhenExhausted(false);

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
            if (debugMgr.isEnabled(DebugManager.DebugModule.LEARNING_MODE)) {
                plugin.getLogger().warning("Pool exhausted, creating fresh vector. (High Load)");
            }
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