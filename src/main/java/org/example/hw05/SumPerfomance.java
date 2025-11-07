package org.example.hw05;

import java.io.FileWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;
import java.util.stream.IntStream;

public class SumPerfomance {
    private static final short[] array = new short[100_000_000];

    static {
        for (int i = 0; i < array.length; i++) {
            array[i] = 1;
        }
    }

    public static void main(String[] args) {
        int[] threadCounts = {1, 10, 100, 1000};

        try(FileWriter writer = new FileWriter("src/main/java/org/example/results.txt")) {
            for (int threadCount : threadCounts) {
                long start = System.nanoTime();
                long sum = sumWithParallelStream(threadCount);
                long end = System.nanoTime();
                writer.write("Parallel Stream with " + threadCount + "threads | Sum: " + sum + "| Time: " + (end - start) / 1000000 + "ms\n");
            }
            writer.write(System.lineSeparator());
            for (int threadCount : threadCounts) {
                long start = System.nanoTime();
                long sum = sumWithParallelThread(threadCount);
                long end = System.nanoTime();
                writer.write("Parallel Threads with " + threadCount + "threads | Sum: " + sum + "| Time: " + (end - start) / 1000000 + "ms\n");
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static long sumWithParallelStream(int threadsCount) throws InterruptedException, ExecutionException {
        ForkJoinPool pool = new ForkJoinPool(threadsCount);
        return pool.submit(() ->
                IntStream.range(0, array.length)
                        .parallel()
                        .mapToLong(i -> array[i])
                        .sum()
        ).get();
    }

    public static long sumWithParallelThread(int threadsCount) throws InterruptedException, ExecutionException {
        int chunk = (array.length + threadsCount - 1) / threadsCount;
        ExecutorService pool = Executors.newFixedThreadPool(threadsCount);
        List<Future<Long>> partialSums = new ArrayList<>();

        for (int i = 0; i < threadsCount; i++) {
            int from = i * chunk;
            int to = Math.min(from + chunk, array.length);
            partialSums.add(pool.submit(() -> {
                long sum = 0;
                for(int j = from; j < to; j++) sum += array[j];
                return sum;
            }));
        }

        long total = 0;
        for (Future<Long> future : partialSums) total += future.get();

        pool.shutdown();
        return total;
    }
}
