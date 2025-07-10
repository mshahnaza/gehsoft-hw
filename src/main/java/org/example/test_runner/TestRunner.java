package org.example.test_runner;

import org.example.test_runner.annotations.Test;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class TestRunner {
    private static int failedTests = 0, passedTests = 0;
    private static long totalTime = 0;

    public static void main(String[] args) throws ClassNotFoundException {
        String packageName = "org.example.test_runner.test";
        List<Class<?>> classes = getPackageClasses(packageName);
        int totalTests = countTests(classes);

        System.out.println("=== Custom Test Runner Results ===");
        System.out.println("Package: " + packageName);
        System.out.println("Classes scanned: " + classes.size());
        System.out.println("Tests discovered: " + totalTests);
        System.out.println("Test Results:");

        for (Class<?> clazz : classes) {
            runTests(clazz);
        }

        System.out.println("Summary:");
        System.out.println("Total tests: " + totalTests);
        System.out.println("Passed: " + passedTests);
        System.out.println("Failed: " + failedTests);
        System.out.println("Total execution time: " + totalTime + "ms");
        System.out.println("Success rate: " + (passedTests * 100.0 / totalTests) + "%");
    }

    public static void runTests(Class<?> clazz) {
        Method[] methods = clazz.getDeclaredMethods();

        for (Method method : methods) {
            if (method.isAnnotationPresent(Test.class)) {
                long startTime = System.currentTimeMillis();

                try {
                    Object instance = clazz.getDeclaredConstructor().newInstance();
                    method.invoke(instance);
                    passedTests++;

                    long duration = System.currentTimeMillis() - startTime;
                    totalTime += duration;
                    System.out.println("✓ " + clazz.getSimpleName() + "." + method.getName() + " (" + duration + "ms)");
                } catch (InvocationTargetException e) {
                    failedTests++;
                    long duration = System.currentTimeMillis() - startTime;
                    totalTime += duration;
                    System.out.println("✗ " + clazz.getSimpleName() + "." + method.getName() + " (" + duration + "ms)"
                    + " - " + e.getCause().getClass().getSimpleName() + ": " + e.getCause().getMessage());
                } catch (Exception e) {
                    failedTests++;
                    long duration = System.currentTimeMillis() - startTime;
                    totalTime += duration;

                    Throwable cause = e.getCause();
                    String errorType = (cause != null) ? cause.getClass().getSimpleName() : e.getClass().getSimpleName();
                    String message = (cause != null) ? cause.getMessage() : e.getMessage();

                    System.out.println("✗ " + clazz.getSimpleName() + "." + method.getName() + " (" + duration + "ms)"
                            + " - " + errorType + ": " + message);
                }

            }
        }
    }

    public static List<Class<?>> getPackageClasses(String packageName) throws ClassNotFoundException {
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        String path = packageName.replace('.', '/');
        URL resource = classLoader.getResource(path);
        if (resource == null) {
            throw new IllegalArgumentException("Package not found: " + packageName);
        }

        File directory = new File(resource.getFile());
        List<Class<?>> classes = new ArrayList<>();

        File[] files = directory.listFiles();
        if (files == null) return classes;

        for (File file : files) {
            if (file.getName().endsWith(".class")) {
                String className = packageName + "." + file.getName().replace(".class", "");
                classes.add(Class.forName(className));
            }
        }

        return classes;
    }

    public static int countTests(List<Class<?>> classes) {
        int count = 0;
        for (Class<?> clazz : classes) {
            for (Method method : clazz.getDeclaredMethods()) {
                if (method.isAnnotationPresent(Test.class)) {
                    count++;
                }
            }
        }
        return count;
    }

}
