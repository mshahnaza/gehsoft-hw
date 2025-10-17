package org.example;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class PlatformTest {

    private static final int threads = 8000;
    private static final int sleepTime = 200;

    public static void main(String[] args) throws InterruptedException {
        virtualThreads();
        System.out.println();
        platformThreads();
    }

    public static void virtualThreads() throws InterruptedException{
        System.gc();
        Thread.sleep(50);

        long startMemory = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();
        long start = System.nanoTime();
        ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor();

        CountDownLatch countDownLatch = new CountDownLatch(threads);
        for (int i = 0; i < threads; i++) {
            executor.execute(() -> {
                try {
                    Thread.sleep(sleepTime);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                } finally {
                    countDownLatch.countDown();
                }
            });
        }

        countDownLatch.await();
        executor.shutdown();
        long endMemory = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();
        long end = System.nanoTime();

        System.out.println("Virtual thread time: " + (end - start) + " | Memory used: " + (endMemory - startMemory));
    }

    public static void platformThreads() throws InterruptedException {
        System.gc();
        Thread.sleep(50);

        long startMemory = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();
        long start = System.nanoTime();

        CountDownLatch countDownLatch = new CountDownLatch(threads);

        for (int i = 0; i < threads; i++) {
            new Thread(() -> {
                try {
                    Thread.sleep(sleepTime);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                } finally {
                    countDownLatch.countDown();
                }
            }).start();
        }

        countDownLatch.await();

        long end = System.nanoTime();
        long endMemory = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();

        System.out.printf("Platform thread time: " + (end - start) + " | Memory used: " + (endMemory - startMemory));
    }
}
