package org.example;

import org.junit.jupiter.api.Test;

import java.util.*;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

public class MyConcurrentHashMapTest {

    @Test
    void should_putValue_when_added() throws InterruptedException {
        MyConcurrentHashMap<Integer, String> map = new MyConcurrentHashMap<>();

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
        MyConcurrentHashMap<Integer, String> map = new MyConcurrentHashMap<>();

        int totalItems = 1000;
        int threadCount = 5;

        for (int i = 0; i < totalItems; i++) {
            map.put(i, "V" + i);
        }

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
        MyConcurrentHashMap<Integer, String> map = new MyConcurrentHashMap<>();

        int totalItems = 500;
        int threadCount = 5;
        int itemsPerThread = totalItems / threadCount;

        for (int i = 0; i < totalItems; i++) {
            map.put(i, "V" + i);
        }

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

        for (int i = 0; i < totalItems; i++) {
            assertNull(map.get(i));
        }
    }


    @Test
    void should_clearAllValues_when_clearCalled() throws InterruptedException {
        MyConcurrentHashMap<Integer, String> map = new MyConcurrentHashMap<>();

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

        for (int i = 0; i < threadCount * itemsPerThread; i++) {
            assertNull(map.get(i));
        }

        map.put(9999, "NewValue");
        assertEquals(1, map.size());
        assertEquals("NewValue", map.get(9999));
    }

    @Test
    void should_returnTrue_when_containsKeyCalled() throws InterruptedException {
        MyConcurrentHashMap<Integer, String> map = new MyConcurrentHashMap<>();

        int threadCount = 5;
        int itemsPerThread = 100;
        CountDownLatch latch = new CountDownLatch(threadCount);

        for (int t = 0; t < threadCount; t++) {
            for (int i = 0; i < itemsPerThread; i++) {
                int key = t * itemsPerThread + i;
                map.put(key, "T" + t + "V" + i);
            }
        }

        for (int t = 0; t < threadCount; t++) {
            new Thread(() -> {
                for (int key = 0; key < threadCount * itemsPerThread; key++) {
                    assertTrue(map.containsKey(key), "Key " + key + " should exist");
                }
                latch.countDown();
            }).start();
        }

        latch.await();

        assertEquals(threadCount * itemsPerThread, map.size());
    }

    @Test
    void should_resize_when_multithreaded() throws InterruptedException {
        int initialCapacity = 4;
        MyConcurrentHashMap<Integer, String> map = new MyConcurrentHashMap<>(initialCapacity, 0.75f);

        int threadCount = 5;
        int itemsPerThread = 50;
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

        for (int t = 0; t < threadCount; t++) {
            for (int i = 0; i < itemsPerThread; i++) {
                int key = t * itemsPerThread + i;
                assertEquals("T" + t + "V" + i, map.get(key));
            }
        }

        assertEquals(threadCount * itemsPerThread, map.size());
    }

}
