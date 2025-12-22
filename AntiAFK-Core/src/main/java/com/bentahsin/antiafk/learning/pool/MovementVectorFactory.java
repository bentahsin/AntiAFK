package com.bentahsin.antiafk.learning.pool;

import com.bentahsin.antiafk.api.learning.MovementVector;
import org.apache.commons.pool2.BasePooledObjectFactory;
import org.apache.commons.pool2.PooledObject;
import org.apache.commons.pool2.impl.DefaultPooledObject;

/**
 * Apache Commons Pool2 için MovementVector nesnelerini nasıl oluşturacağını
 * ve yöneteceğini tanımlayan fabrika sınıfı.
 */
public class MovementVectorFactory extends BasePooledObjectFactory<MovementVector> {

    /**
     * Havuzda ödünç alınacak nesne kalmadığında, yeni bir tane oluşturur.
     */
    @Override
    public MovementVector create() {
        return new MovementVector(0, 0, 0, 0, MovementVector.PlayerAction.NONE, 0);
    }

    /**
     * Oluşturulan nesneyi, havuzun yönetebileceği bir PooledObject'e sarar.
     */
    @Override
    public PooledObject<MovementVector> wrap(MovementVector obj) {
        return new DefaultPooledObject<>(obj);
    }
}