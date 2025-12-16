package org.example.hw02.test_runner;

import org.example.hw02.test_runner.annotations.AfterEach;
import org.example.hw02.test_runner.annotations.BeforeEach;
import org.example.hw02.test_runner.annotations.ParametrizedTest;
import org.example.hw02.test_runner.annotations.Test;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

public class TestRunner {
    private static int failedTests = 0, passedTests = 0;
    private static long totalTime = 0;

    private static List<Method> beforeEachMethods;
    private static List<Method> afterEachMethods;

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
        List<Method> methods = getAllMethods(clazz);

        beforeEachMethods = new ArrayList<>();
        afterEachMethods = new ArrayList<>();

        for (Method method : methods) {
            if (method.isAnnotationPresent(BeforeEach.class)) beforeEachMethods.add(method);
            if (method.isAnnotationPresent(AfterEach.class)) afterEachMethods.add(method);
        }

        for (Method method : methods) {
            if (method.isAnnotationPresent(Test.class)) {
                runSingleTest(clazz, method, null);
            }
            if (method.isAnnotationPresent(ParametrizedTest.class)) {
                ParametrizedTest params = method.getAnnotation(ParametrizedTest.class);

                for (int value : params.intValues()) {
                    runSingleTest(clazz, method, value);
                }

                for (String value : params.stringValues()) {
                    runSingleTest(clazz, method, value);
                }
            }
        }
    }

    public static void runSingleTest(Class<?> clazz, Method method, Object param) {
        long startTime = System.currentTimeMillis();
        Object instance = null;

        try {
            instance = clazz.getDeclaredConstructor().newInstance();

            for (Method before : beforeEachMethods) {
                before.setAccessible(true);
                before.invoke(instance);
            }

            long timeout = 0;
            String description = "";

            if (param == null) {
                timeout = method.getAnnotation(Test.class).timeout();
                description = method.getAnnotation(Test.class).description();
                if (timeout > 0) {
                    Object finalInstance = instance;
                    invokeWithTimeout(() -> {
                        method.invoke(finalInstance);
                        return null;
                    }, timeout);
                } else {
                    method.invoke(instance);
                }
            } else {
                method.invoke(instance, param);
            }

            passedTests++;

            long duration = System.currentTimeMillis() - startTime;
            totalTime += duration;
            String paramStr = (param != null) ? " [param=" + param + "]" : "";
            System.out.println("✓ " + clazz.getSimpleName() + "." + method.getName()
                    + (description.isEmpty() ? "" : " - " + description) + paramStr + " (" + duration + "ms)");
        } catch (InvocationTargetException e) {
            failedTests++;
            long duration = System.currentTimeMillis() - startTime;
            totalTime += duration;
            String paramStr = (param != null) ? " [param=" + param + "]" : "";
            System.out.println("✗ " + clazz.getSimpleName() + "." + method.getName() + " (" + duration + "ms)" + paramStr
                    + " - " + e.getCause().getClass().getSimpleName() + ": " + e.getCause().getMessage());
        } catch (Exception e) {
            failedTests++;
            long duration = System.currentTimeMillis() - startTime;
            totalTime += duration;

            Throwable cause = e.getCause();
            String errorType = (cause != null) ? cause.getClass().getSimpleName() : e.getClass().getSimpleName();
            String message = (cause != null) ? cause.getMessage() : e.getMessage();

            String paramStr = (param != null) ? " [param=" + param + "]" : "";
            System.out.println("✗ " + clazz.getSimpleName() + "." + method.getName() + " (" + duration + "ms)" + paramStr
                    + " - " + errorType + ": " + message);
        } finally {
            try {
                for (Method after : afterEachMethods) {
                    after.setAccessible(true);
                    after.invoke(instance);
                }
            } catch (Exception e) {}
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

    private static void invokeWithTimeout(Callable<Void> task, long timeout) throws Exception {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        Future<Void> future = executor.submit(task);
        try {
            future.get(timeout, TimeUnit.MILLISECONDS);
        } catch (TimeoutException e) {
            future.cancel(true);
            throw new Exception("Test timed out after " + timeout + "ms");
        } finally {
            executor.shutdownNow();
        }
    }

    public static List<Method> getAllMethods(Class<?> clazz) {
        List<Method> testMethods = new ArrayList<>();
        Class<?> current = clazz;

        while (current != null && current != Object.class) {
            for (Method method : current.getDeclaredMethods()) {
                testMethods.add(method);
            }
            current = current.getSuperclass();
        }

        return testMethods;
    }

    public static int countTests(List<Class<?>> classes) {
        int count = 0;
        for (Class<?> clazz : classes) {
            List<Method> methods = getAllMethods(clazz);
            for (Method method : methods) {
                if (method.isAnnotationPresent(Test.class)) {
                    count++;
                }
                if (method.isAnnotationPresent(ParametrizedTest.class)) {
                    ParametrizedTest params = method.getAnnotation(ParametrizedTest.class);
                    count += params.intValues().length + params.stringValues().length;
                }
            }
        }
        return count;
    }



}
