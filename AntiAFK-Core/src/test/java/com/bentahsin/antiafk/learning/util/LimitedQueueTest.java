package com.bentahsin.antiafk.learning.util;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class LimitedQueueTest {

    @Test
    @DisplayName("Liste Limiti Aşınca En Eski Eleman Silinmeli")
    void testLimitEnforcement() {
        LimitedQueue<Integer> queue = new LimitedQueue<>(3, null);

        queue.add(1);
        queue.add(2);
        queue.add(3);

        assertEquals(3, queue.size());

        queue.add(4);

        assertEquals(3, queue.size());
        assertEquals(2, queue.get(0));
        assertEquals(4, queue.get(2));
    }
}