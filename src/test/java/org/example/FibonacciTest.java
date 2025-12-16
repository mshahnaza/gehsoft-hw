package org.example;

import org.example.hw02.FibonacciAlgorithms;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class FibonacciTest {

    @Test
    void shouldReturnSameFibonacci() {
        for (int i = 0; i < 35; i++) {
            long recursive = FibonacciAlgorithms.fibonacciRecursive(35);
            long memoized = FibonacciAlgorithms.fibonacciMemoized(35);
            long iterative = FibonacciAlgorithms.fibonacciIterative(35);

            assertEquals(recursive, memoized);
            assertEquals(recursive, iterative);
            assertEquals(memoized, iterative);
        }
    }
}
