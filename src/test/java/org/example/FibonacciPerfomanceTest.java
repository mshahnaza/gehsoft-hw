package org.example;

import org.example.hw02.FibonacciAlgorithms;

public class FibonacciPerfomanceTest {
    public static void main(String[] args) {
        System.out.println("======Fibonacci perfomance test======");
        System.out.println("Input: 10");
        fibonacciTest(10);
        System.out.println("====================================");
        System.out.println("Input: 20");
        fibonacciTest(20);
        System.out.println("====================================");
        System.out.println("Input: 30");
        fibonacciTest(30);
        System.out.println("====================================");
        System.out.println("Input: 35");
        fibonacciTest(35);
    }

    private static void fibonacciTest(int n) {
        runTest("Recursive Test", () -> {
            FibonacciAlgorithms.fibonacciRecursive(n);
        });

        runTest("Memoized Test", () -> {
            FibonacciAlgorithms.fibonacciMemoized(n);
        });

        runTest("Iterative Test", () -> {
            FibonacciAlgorithms.fibonacciIterative(n);
        });
    }

    private static void runTest(String name, Runnable task) {
        Runtime runtime = Runtime.getRuntime();
        System.gc();

        long beforeUsedMem = (runtime.totalMemory() - runtime.freeMemory()) / (1024 * 1024);
        long start = System.currentTimeMillis();

        task.run();

        long end = System.currentTimeMillis();
        long afterUsedMem = (runtime.totalMemory() - runtime.freeMemory()) / (1024 * 1024);

        System.out.printf("%-12s : Time: %4d ms,  Memory: %4d MB\n",
                name, (end - start), (afterUsedMem - beforeUsedMem));
    }
}
