package org.example.test_runner.test;

import org.example.FibonacciAlgorithms;
import org.example.test_runner.annotations.Test;
import static org.example.test_runner.Assertions.*;

public class FibonacciTest {

    @Test
    public void fibonacciRecursive() {
        long zero = FibonacciAlgorithms.fibonacciRecursive(0);
        long one = FibonacciAlgorithms.fibonacciRecursive(1);

        assertEquals(0, zero);
        assertEquals(1, one);
    }
}
