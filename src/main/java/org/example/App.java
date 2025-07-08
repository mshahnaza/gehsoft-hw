package org.example;

public class App
{
    public static void main( String[] args )
    {
        System.out.println(FibonacciAlgorithms.fibonacciRecursive(35));
        System.out.println(FibonacciAlgorithms.fibonacciIterative(35));
        System.out.println(FibonacciAlgorithms.fibonacciMemoized(35));
    }
}
