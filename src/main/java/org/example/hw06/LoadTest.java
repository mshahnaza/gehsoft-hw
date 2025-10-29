package org.example.hw06;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicLong;

public class LoadTest {

    private static final int CONCURRENT_USERS = 50;
    private static final int REQUESTS_PER_USER = 100;

    public static void main(String[] args) {
        System.out.println("Concurrent users: " + CONCURRENT_USERS);
        System.out.println("Requests per user: " + REQUESTS_PER_USER);
        System.out.println("Total requests: " + (CONCURRENT_USERS * REQUESTS_PER_USER) + "\n");

        System.out.println("Testing Virtual Thread Server (port 8080)...");
        long virtualTime = run("http://localhost:8080/api/time");

        try { Thread.sleep(2000); } catch (InterruptedException e) {}

        System.out.println("Testing Platform Thread Server (port 8081)...");
        long platformTime = run("http://localhost:8081/api/time");

        System.out.println("\n=== Results ===");
        System.out.println("Virtual Thread Server:  " + virtualTime + " ms");
        System.out.println("Platform Thread Server: " + platformTime + " ms");

        if (virtualTime < platformTime) {
            long diff = platformTime - virtualTime;
            double improvement = (diff * 100.0) / platformTime;
            System.out.printf("\nVirtual threads are faster by %d ms (%.2f%% improvement)\n",
                    diff, improvement);
        } else {
            long diff = virtualTime - platformTime;
            double improvement = (diff * 100.0) / virtualTime;
            System.out.printf("\nPlatform threads are faster by %d ms (%.2f%% improvement)\n",
                    diff, improvement);
        }
    }

    public static long run(String url) {
        HttpClient client = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(10))
                .build();

        ExecutorService executor = Executors.newFixedThreadPool(CONCURRENT_USERS);
        CountDownLatch latch = new CountDownLatch(CONCURRENT_USERS);

        AtomicLong totalResponseTime = new AtomicLong(0);
        AtomicLong successCount = new AtomicLong(0);
        AtomicLong errorCount = new AtomicLong(0);

        long startTime = System.currentTimeMillis();

        for (int i = 0; i < CONCURRENT_USERS; i++) {
            executor.submit(() -> {
                try {
                    for (int j = 0; j < REQUESTS_PER_USER; j++) {
                        HttpRequest request = HttpRequest.newBuilder()
                                .uri(URI.create(url))
                                .GET()
                                .timeout(Duration.ofSeconds(30))
                                .build();

                        long reqStart = System.nanoTime();
                        HttpResponse<String> response = client.send(request,
                                HttpResponse.BodyHandlers.ofString());
                        long reqEnd = System.nanoTime();

                        if (response.statusCode() == 200) {
                            successCount.incrementAndGet();
                            totalResponseTime.addAndGet((reqEnd - reqStart) / 1_000_000);
                        } else {
                            errorCount.incrementAndGet();
                        }
                    }
                } catch (Exception e) {
                    errorCount.incrementAndGet();
                } finally {
                    latch.countDown();
                }
            });
        }

        try {
            latch.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        executor.shutdown();

        long endTime = System.currentTimeMillis();
        long totalTime = endTime - startTime;

        int totalRequests = CONCURRENT_USERS * REQUESTS_PER_USER;
        double avgResponseTime = totalResponseTime.get() / (double) successCount.get();
        double throughput = successCount.get() / (totalTime / 1000.0);

        System.out.println("  Total requests: " + totalRequests);
        System.out.println("  Successful: " + successCount.get());
        System.out.println("  Failed: " + errorCount.get());
        System.out.println("  Total time: " + totalTime + " ms");
        System.out.printf("  Avg response time: %.2f ms\n", avgResponseTime);
        System.out.printf("  Throughput: %.2f req/sec\n", throughput);

        return totalTime;
    }
}