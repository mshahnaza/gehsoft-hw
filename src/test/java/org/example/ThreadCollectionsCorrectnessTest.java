package org.example;

import org.example.hw05.decorators.CopyOnWriteList;
import org.example.hw05.decorators.LockDecorator;
import org.example.hw05.decorators.SynchronizedDecorator;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;

public class ThreadCollectionsCorrectnessTest {
    static List<List<Integer>> listProvider() {
        List<List<Integer>> result = new ArrayList<>();
        result.add(new SynchronizedDecorator<>(new CustomList<>()));
        result.add(new LockDecorator<>(new CustomList<>()));
        result.add(new CustomList());
        return result;
    }

    @ParameterizedTest
    @MethodSource("listProvider")
    void testThreadCorrectness(List<Integer> list) throws InterruptedException {
        int elements = 1_000_000;
        int threads = 2;
        int expectedElements = elements * threads;

        boolean correct = true;

        String listType = list.getClass().getSimpleName();
        System.out.println("Testing " + listType);

        for (int i = 0; i < 100; i++) {
            list.clear();
            CountDownLatch latch = new CountDownLatch(threads);
            for (int j = 0; j < threads; j++) {
                new Thread(() -> {
                    for (int k = 0; k < elements; k++) {
                        list.add(k);
                    }
                    latch.countDown();
                }).start();
            }

            latch.await();
            if (list.size() != expectedElements) {
                correct = false;
            }
        }

        assertTrue(correct);
        assertEquals(list.size(), expectedElements);
    }

    @Test
    void testCopyOnWriteCorrectness() throws InterruptedException {
        CopyOnWriteList<Integer> list = new CopyOnWriteList<>(new CustomList<Integer>());
        int elements = 10_000;
        int threads = 2;
        int expectedElements = elements * threads;

        boolean correct = true;

        String listType = list.getClass().getSimpleName();
        System.out.println("Testing " + listType);

        for (int i = 0; i < 10; i++) {
            list.clear();
            CountDownLatch latch = new CountDownLatch(threads);
            for (int j = 0; j < threads; j++) {
                new Thread(() -> {
                    for (int k = 0; k < elements; k++) {
                        list.add(k);
                    }
                    latch.countDown();
                }).start();
            }

            latch.await();
            if (list.size() != expectedElements) {
                correct = false;
            }
        }

        assertTrue(correct);
        assertEquals(list.size(), expectedElements);
    }

    @ParameterizedTest
    @MethodSource("listProvider")
    void testPerfomance(List<Integer> list) throws InterruptedException {
        long start = System.nanoTime();
        long startMemory = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();

        CountDownLatch latch = new CountDownLatch(3);

        for (int i = 0; i < 3; i++) {
            new Thread(() -> {
                for (int k = 0; k < 1_000; k++) {
                    list.add(k);
                }
                latch.countDown();
            }).start();
        }

        latch.await();

        long endMemory = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();
        long end = System.nanoTime();
        System.out.printf("%s: Memory=%d, Time=%d ms%n", list.getClass().getSimpleName(), (endMemory - startMemory), (end - start));
    }

    @Test
    void copyOnWritePerfomanceTest() throws InterruptedException {
        CopyOnWriteList<Integer> list = new CopyOnWriteList<>(new CustomList<>());
        long start = System.nanoTime();
        long startMemory = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();

        CountDownLatch latch = new CountDownLatch(3);

        for (int i = 0; i < 3; i++) {
            new Thread(() -> {
                for (int k = 0; k < 1_000; k++) {
                    list.add(k);
                }
                latch.countDown();
            }).start();
        }

        latch.await();

        long endMemory = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();
        long end = System.nanoTime();
        System.out.printf("%s: Memory=%d, Time=%d ms%n", list.getClass().getSimpleName(), (endMemory - startMemory), (end - start));
    }
}
