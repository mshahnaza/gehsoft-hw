package org.example;

import java.util.HashMap;
import java.util.Map;

public class HashMapPerfomanceTest {
    public static void main(String[] args) {
        System.out.println("\n    HashMap Performance Test    ");

        int count = 1_000_000;

        runTest("JDK HashMap put", () -> {
            Map<Integer, Integer> map = new HashMap<>();
            for (int i = 0; i < count; i++) map.put(i, i);
        });

        runTest("MyHashMap put", () -> {
            Map<Integer, Integer> map = new MyHashMap<>();
            for (int i = 0; i < count; i++) map.put(i, i);
        });

        runTest("JDK HashMap get", () -> {
            Map<Integer, Integer> map = new HashMap<>();
            for (int i = 0; i < count; i++) map.put(i, i);
            for (int i = 0; i < count; i++) map.get(i);
        });

        runTest("MyHashMap get", () -> {
            Map<Integer, Integer> map = new MyHashMap<>();
            for (int i = 0; i < count; i++) map.put(i, i);
            for (int i = 0; i < count; i++) map.get(i);
        });

        runTest("JDK HashMap remove", () -> {
            Map<Integer, Integer> map = new HashMap<>();
            for (int i = 0; i < count; i++) map.put(i, i);
            for (int i = 0; i < count; i++) map.remove(i);
        });

        runTest("MyHashMap remove", () -> {
            Map<Integer, Integer> map = new MyHashMap<>();
            for (int i = 0; i < count; i++) map.put(i, i);
            for (int i = 0; i < count; i++) map.remove(i);
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
