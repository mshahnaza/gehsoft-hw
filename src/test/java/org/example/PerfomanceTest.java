package org.example;

import org.example.hw01.CustomList;
import org.example.hw03.MyLinkedList;

import java.util.*;

public class PerfomanceTest {

    public static void main(String[] args) {
        System.out.println("    Bulk Addition Test    ");
        bulkAdditionTest();

        System.out.println("\n    Add/Remove Test    ");
        addRemoveTest();
    }

    private static void bulkAdditionTest() {
        int count = 1000000;

        runTest("ArrayList", () -> {
            List<Integer> list = new ArrayList<>();
            for (int i = 0; i < count; i++) list.add(i);
        });

        runTest("LinkedList", () -> {
            List<Integer> list = new LinkedList<>();
            for (int i = 0; i < count; i++) list.add(i);
        });

        runTest("CustomList", () -> {
            CustomList<Integer> list = new CustomList<>();
            for (int i = 0; i < count; i++) list.add(i);
        });

        runTest("MyLinkedList", () -> {
            MyLinkedList<Integer> list = new MyLinkedList<>();
            for (int i = 0; i < count; i++) list.add(i);
        });
    }

    private static void addRemoveTest() {
        int count = 10000;

        runTest("ArrayList", () -> {
            List<Integer> list = new ArrayList<>();
            for (int i = 0; i < count; i++) list.add(i);
            for (int i = 0; i < count; i++) list.remove(0);
        });

        runTest("LinkedList", () -> {
            List<Integer> list = new LinkedList<>();
            for (int i = 0; i < count; i++) list.add(i);
            for (int i = 0; i < count; i++) list.remove(0);
        });

        runTest("CustomList", () -> {
            CustomList<Integer> list = new CustomList<>();
            for (int i = 0; i < count; i++) list.add(i);
            for (int i = 0; i < count; i++) list.remove(0);
        });

        runTest("MyLinkedList", () -> {
            MyLinkedList<Integer> list = new MyLinkedList<>();
            for (int i = 0; i < count; i++) list.add(i);
            for (int i = 0; i < count; i++) list.remove(0);
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
