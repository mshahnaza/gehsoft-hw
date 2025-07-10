package org.example.test_runner.test;

import org.example.FibonacciAlgorithms;
import org.example.test_runner.annotations.*;
import static org.example.test_runner.Assertions.*;

public class FibonacciTest {
    @BeforeEach
    public void before() {
        System.out.println("[Started -> BeforeEach]");
    }

    @AfterEach
    public void after() {
        System.out.println("[Finished -> AfterEach]");
        System.out.println();
    }

    @Test
    public void fibonacciRecursive() {
        long result = FibonacciAlgorithms.fibonacciRecursive(10);

        assertEquals(55, result);
    }

    @Test(description = "Check iterative Fibonacci for 10", timeout = 1000)
    public void fibonacciIterativeTest() {
        long result = FibonacciAlgorithms.fibonacciIterative(10);
        assertEquals(55, result);
    }

    @Test(description = "Check memoized Fibonacci correctness")
    public void fibonacciMemoizedTest() {
        long result = FibonacciAlgorithms.fibonacciMemoized(15);
        assertEquals(610, result);
    }

    @Test(description = "This test should fail")
    public void failingTest() {
        long result = FibonacciAlgorithms.fibonacciRecursive(1);
        assertEquals(10, result);
    }

    @Test(timeout = 10)
    public void timeoutExceededTest() throws InterruptedException {
        FibonacciAlgorithms.fibonacciRecursive(50);
    }

    @ParametrizedTest(intValues = {0, 1, 2, 3, 5, 10, 15})
    public void parametrizedFibonacciRecursive(int n) {
        long expected = FibonacciAlgorithms.fibonacciIterative(n);
        long actual = FibonacciAlgorithms.fibonacciRecursive(n);
        assertEquals(expected, actual);
    }
}
