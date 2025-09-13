package com.bentahsin.antiafk.learning.pool;

import com.bentahsin.antiafk.AntiAFKPlugin;
import com.bentahsin.antiafk.language.Lang;
import com.bentahsin.antiafk.language.SystemLanguageManager;
import com.bentahsin.antiafk.learning.MovementVector;
import org.apache.commons.math3.geometry.euclidean.twod.Vector2D;
import org.apache.commons.pool2.impl.GenericObjectPool;
import org.apache.commons.pool2.impl.GenericObjectPoolConfig;

import java.time.Duration;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * MovementVector nesne havuzunu yönetir.
 */
public class VectorPoolManager {

    private final GenericObjectPool<MovementVector> pool;
    private final AntiAFKPlugin plugin;
    private final Logger logger;
    private final SystemLanguageManager sysLang;

    public VectorPoolManager(AntiAFKPlugin plugin) {
        this.plugin = plugin;
        this.logger = plugin.getLogger();
        this.sysLang = plugin.getSystemLanguageManager();

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
            logger.log(Level.WARNING, sysLang.getSystemMessage(Lang.VECTOR_POOL_BORROW_ERROR), e);
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