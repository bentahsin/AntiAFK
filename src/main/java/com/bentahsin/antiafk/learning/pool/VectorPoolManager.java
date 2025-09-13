package com.bentahsin.antiafk.learning.pool;

import com.bentahsin.antiafk.AntiAFKPlugin;
import com.bentahsin.antiafk.learning.MovementVector;
import org.apache.commons.math3.geometry.euclidean.twod.Vector2D;
import org.apache.commons.pool2.impl.GenericObjectPool;
import org.apache.commons.pool2.impl.GenericObjectPoolConfig;

import java.time.Duration;
import java.util.logging.Level;

/**
 * MovementVector nesne havuzunu yönetir.
 */
public class VectorPoolManager {

    private final GenericObjectPool<MovementVector> pool;
    private final AntiAFKPlugin plugin;

    public VectorPoolManager(AntiAFKPlugin plugin) {
        this.plugin = plugin;

        GenericObjectPoolConfig<MovementVector> config = new GenericObjectPoolConfig<>();
        config.setMaxTotal(500);
        config.setMaxWait(Duration.ofMillis(10));

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
            return vector;
        } catch (Exception e) {
            plugin.getLogger().log(Level.WARNING, "Could not borrow MovementVector from pool. Creating a new instance as a fallback.", e);
            return new MovementVector(posChange, rotChange, action, durationTicks);
        }
    }

    /**
     * Kullanımı biten bir MovementVector nesnesini havuza geri bırakır.
     */
    public void returnVector(MovementVector vector) {
        if (vector != null) {
            pool.returnObject(vector);
        }
    }

    /**
     * Eklenti devre dışı bırakıldığında havuzu kapatır.
     */
    public void close() {
        pool.close();
    }
}