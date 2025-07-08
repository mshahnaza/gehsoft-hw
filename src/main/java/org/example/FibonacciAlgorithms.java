package org.example;

import java.util.HashMap;
import java.util.Map;

public class FibonacciAlgorithms {
    private static Map<Integer, Long> memoTable = new HashMap<>();

    /**
     * Recursive implementation of Fibonacci sequence
     * Time Complexity: O(2^n)
     * Explanation: Each call makes two more calls,
     * so the total number of calls doubles many times
     *
     * Space Complexity: O(n)
     * Explanation: The call stack grows up to n levels deep
     */

    public static long fibonacciRecursive(int n) {
        if(n < 2) {
            return n;
        }
        else {
            return fibonacciRecursive(n-1) + fibonacciRecursive(n-2);
        }
    }

    /**
     * Memoized implementation of Fibonacci sequence
     * Time Complexity: O(n)
     * Explanation: Each number is calculated once and stored,
     * so we avoid repeating work
     *
     * Space Complexity: O(n)
     * Explanation: We store n results in the map
     */
    public static long fibonacciMemoized(int n) {
        if(n < 2) {
            return n;
        }
        if (memoTable.containsKey(n)) {
            return memoTable.get(n);
        }
        long result = fibonacciRecursive(n-1) + fibonacciRecursive(n-2);
        memoTable.put(n, result);
        return result;
    }

    /**
     * Iterative implementation of Fibonacci sequence
     * Time Complexity: O(n)
     * Explanation: We loop once from 2 to n
     *
     * Space Complexity: O(1)
     * Explanation: We use a constant amount of memory, no matter how large n is
     */
    public static long fibonacciIterative(int n) {
        if(n < 2) return n;
        int previous = 0, current = 1;

        for (int i = 2; i <= n; i++) {
            int temp = current;
            current = previous + current;
            previous = temp;
        }
        return current;
    }
}
