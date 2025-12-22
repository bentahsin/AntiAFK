package com.bentahsin.antiafk.learning;

import com.bentahsin.antiafk.api.learning.MovementVector;
import com.bentahsin.antiafk.learning.pool.MovementVectorFactory;
import org.apache.commons.pool2.impl.GenericObjectPool;
import org.apache.commons.pool2.impl.GenericObjectPoolConfig;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

import static org.junit.jupiter.api.Assertions.*;

class VectorPoolConcurrencyTest {

    @Test
    @DisplayName("Havuz Stres Testi: Çoklu Thread Erişimi")
    void testConcurrentBorrowAndReturn() throws InterruptedException {
        GenericObjectPoolConfig<MovementVector> config = new GenericObjectPoolConfig<>();
        config.setMaxTotal(50);
        config.setMaxWait(Duration.ofMillis(100));

        GenericObjectPool<MovementVector> pool = new GenericObjectPool<>(new MovementVectorFactory(), config);

        int threadCount = 10;
        int operationsPerThread = 1000;
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        CountDownLatch latch = new CountDownLatch(threadCount);
        List<Future<Boolean>> results = new ArrayList<>();

        for (int i = 0; i < threadCount; i++) {
            results.add(executor.submit(() -> {
                try {
                    for (int j = 0; j < operationsPerThread; j++) {
                        MovementVector v = pool.borrowObject();

                        double randomVal = Math.random();
                        v.reinitialize(randomVal, 0, 0, 0, MovementVector.PlayerAction.IDLE, 1);

                        Thread.sleep(0, 100);

                        if (v.getPositionChange().getX() != randomVal) {
                            return false;
                        }

                        pool.returnObject(v);
                    }
                    return true;
                } catch (Exception e) {
                    e.printStackTrace();
                    return false;
                } finally {
                    latch.countDown();
                }
            }));
        }

        boolean ignored = latch.await(5, TimeUnit.SECONDS);
        executor.shutdown();

        for (Future<Boolean> result : results) {
            try {
                assertTrue(result.get(), "Threadlerden biri hata ile karşılaştı veya veri kirlenmesi yaşadı.");
            } catch (ExecutionException e) {
                fail("Thread execution failed: " + e.getMessage());
            }
        }

        assertEquals(0, pool.getNumActive(), "Havuzda asılı kalan (iade edilmemiş) nesne var!");
    }
}