package com.bentahsin.antiafk.learning.util;

import com.bentahsin.antiafk.api.learning.MovementVector;
import com.bentahsin.antiafk.learning.pool.VectorPoolManager;

import java.util.ArrayList;

/**
 * Belirlenen bir maksimum boyutu aşmayan bir ArrayList.
 * Boyut aşıldığında, en eski eleman (listenin başındaki) otomatik olarak silinir.
 * @param <E> Listede tutulacak elemanın türü.
 */
public class LimitedQueue<E> extends ArrayList<E> {

    private final int limit;
    private final VectorPoolManager poolManager;

    public LimitedQueue(int limit, VectorPoolManager poolManager) {
        this.limit = limit;
        this.poolManager = poolManager;
    }

    @Override
    public boolean add(E e) {
        boolean added = super.add(e);
        while (size() > limit) {
            E removed = super.remove(0);
            if (removed instanceof MovementVector && poolManager != null) {
                poolManager.returnVector((MovementVector) removed);
            }
        }
        return added;
    }
}