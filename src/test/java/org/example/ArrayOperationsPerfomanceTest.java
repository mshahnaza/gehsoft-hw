package org.example;

public class ArrayOperationsPerfomanceTest {
    public static void main(String[] args) {
        int[] sizes = new int[] { 1000, 10000, 100000, 1000000 };
        int[] positions = new int[] { 1, 10, 100, 1000 };

        System.out.println("=================Array shift Test==================");
        for (int size : sizes) {
            for (int position : positions) {
                shiftTest(size, position);
            }
        }

        System.out.println("Both methods have time complexity O(n), because each element must be read and written once.\n" +
                "Experimental results show that `System.arraycopy` is usually slightly faster than manual loop due to JVM optimizations.\n" +
                "For small arrays the time is negligible. For large arrays the time is visible and grows linearly.");
    }

    private static void shiftTest(int size, int positions) {
        int[] array = new int[size];
        for (int i = 0; i < size; i++) {
            array[i] = i;
        }
        System.out.println("Size: " + size + " | Positions: " + positions);
        runTest("Shift SystemCopy", () -> {
            int[] copy = array.clone();
            ArrayOperations.shiftLeftSystemCopy(copy, positions);
        });

        runTest("Shift manual", () -> {
            int[] copy = array.clone();
            ArrayOperations.shiftLeftManualLoop(copy, positions);
        });
        System.out.println("===================================================");
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


