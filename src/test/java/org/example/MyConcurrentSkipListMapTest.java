package org.example;

import org.example.hw04.MyConcurrentSkipListMap;
import org.junit.jupiter.api.Test;

import java.util.concurrent.CountDownLatch;

import static org.junit.jupiter.api.Assertions.*;

public class MyConcurrentSkipListMapTest {

    @Test
    void should_putValue_when_added() throws InterruptedException {
        MyConcurrentSkipListMap<Integer, String> map = new MyConcurrentSkipListMap<>();

        int threadCount = 5;
        int itemsPerThread = 100;
        CountDownLatch latch = new CountDownLatch(threadCount);

        for (int t = 0; t < threadCount; t++) {
            final int threadId = t;
            new Thread(() -> {
                for (int i = 0; i < itemsPerThread; i++) {
                    int key = threadId * itemsPerThread + i;
                    map.put(key, "T" + threadId + "V" + i);
                }
                latch.countDown();
            }).start();
        }

        latch.await();

        assertEquals(threadCount * itemsPerThread, map.size());
        assertEquals("T0V0", map.get(0));
        assertEquals("T4V99", map.get(499));
    }

    @Test
    void should_returnValue_when_keyExists() throws InterruptedException {
        MyConcurrentSkipListMap<Integer, String> map = new MyConcurrentSkipListMap<>();

        int totalItems = 1000;
        for (int i = 0; i < totalItems; i++) map.put(i, "V" + i);

        int threadCount = 5;
        CountDownLatch latch = new CountDownLatch(threadCount);

        for (int t = 0; t < threadCount; t++) {
            new Thread(() -> {
                for (int i = 0; i < totalItems; i++) {
                    assertEquals("V" + i, map.get(i));
                }
                latch.countDown();
            }).start();
        }

        latch.await();
        assertEquals(totalItems, map.size());
    }

    @Test
    void should_removeValue_when_keyExists() throws InterruptedException {
        MyConcurrentSkipListMap<Integer, String> map = new MyConcurrentSkipListMap<>();

        int totalItems = 500;
        int threadCount = 5;
        int itemsPerThread = totalItems / threadCount;

        for (int i = 0; i < totalItems; i++) map.put(i, "V" + i);

        CountDownLatch latch = new CountDownLatch(threadCount);
        for (int t = 0; t < threadCount; t++) {
            final int threadId = t;
            new Thread(() -> {
                int start = threadId * itemsPerThread;
                int end = start + itemsPerThread;
                for (int i = start; i < end; i++) {
                    String removed = map.remove(i);
                    assertEquals("V" + i, removed);
                }
                latch.countDown();
            }).start();
        }

        latch.await();
        assertEquals(0, map.size());
        for (int i = 0; i < totalItems; i++) assertNull(map.get(i));
    }

    @Test
    void should_clearAllValues_when_clearCalled() throws InterruptedException {
        MyConcurrentSkipListMap<Integer, String> map = new MyConcurrentSkipListMap<>();

        int threadCount = 5;
        int itemsPerThread = 100;
        CountDownLatch latch = new CountDownLatch(threadCount);
        CountDownLatch startLatch = new CountDownLatch(1);

        for (int t = 0; t < threadCount; t++) {
            final int threadId = t;
            new Thread(() -> {
                try {
                    startLatch.await();
                    for (int i = 0; i < itemsPerThread; i++) {
                        int key = threadId * itemsPerThread + i;
                        map.put(key, "T" + threadId + "V" + i);
                        Thread.yield();
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                } finally {
                    latch.countDown();
                }
            }).start();
        }

        Thread clearThread = new Thread(() -> {
            try {
                startLatch.await();
                Thread.sleep(10);
                map.clear();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        });
        clearThread.start();

        startLatch.countDown();
        latch.await();
        clearThread.join();

        assertEquals(0, map.size());
        for (int i = 0; i < threadCount * itemsPerThread; i++) assertNull(map.get(i));

        map.put(9999, "NewValue");
        assertEquals(1, map.size());
        assertEquals("NewValue", map.get(9999));
    }
}
