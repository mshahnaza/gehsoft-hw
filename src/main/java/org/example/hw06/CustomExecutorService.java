package org.example.hw06;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

public class CustomExecutorService implements ExecutorService {
    private final int corePoolSize;
    private final boolean useVirtualThreads;
    private volatile boolean shutdown = false;
    private final BlockingQueue<Runnable> taskQueue;
    private final List<Thread> workers;

    public CustomExecutorService(int corePoolSize, boolean useVirtualThreads) {
        if (corePoolSize <= 0) throw new IllegalArgumentException();
        this.corePoolSize = corePoolSize;
        this.useVirtualThreads = useVirtualThreads;
        this.taskQueue = new LinkedBlockingQueue<>();
        this.workers = new ArrayList<>();

        for (int i = 0; i < corePoolSize; i++) {
            Worker worker = new Worker();
            Thread t;
            if (useVirtualThreads) {
                t = Thread.ofVirtual().name("VirtualWorker-" + i).unstarted(worker);
            } else {
                t = new Thread(worker, "PlatformWorker-" + i);
            }
            workers.add(t);
            t.start();
        }
    }

    @Override
    public void shutdown() {
        shutdown = true;
    }

    @Override
    public List<Runnable> shutdownNow() {
        shutdown = true;
        List<Runnable> remaining = new ArrayList<>();
        taskQueue.drainTo(remaining);
        for (Thread worker : workers) {
            worker.interrupt();
        }
        return remaining;
    }

    @Override
    public boolean isShutdown() {
        return shutdown;
    }

    @Override
    public boolean isTerminated() {
        if(!shutdown) return false;
        return taskQueue.isEmpty() && workers.stream().noneMatch(Thread::isAlive);
    }

    @Override
    public boolean awaitTermination(long timeout, TimeUnit unit) throws InterruptedException {
        long deadline = System.nanoTime() + unit.toNanos(timeout);
        while (!isTerminated()) {
            if (System.nanoTime() > deadline) {
                return false;
            }
            Thread.sleep(10);
        }
        return true;
    }

    @Override
    public <T> Future<T> submit(Callable<T> task) {
        if (shutdown) throw new RejectedExecutionException("ExecutorService has been shutdown");
        FutureTask<T> futureTask = new FutureTask<>(task);
        execute(futureTask);
        return futureTask;
    }

    @Override
    public <T> Future<T> submit(Runnable task, T result) {
        if (shutdown) throw new RejectedExecutionException("ExecutorService has been shutdown");
        FutureTask<T> futureTask = new FutureTask<>(task, result);
        execute(futureTask);
        return futureTask;
    }

    @Override
    public Future<?> submit(Runnable task) {
       if (shutdown) throw new RejectedExecutionException("ExecutorService has been shutdown");
       FutureTask<?> futureTask = new FutureTask<>(task, null);
       execute(futureTask);
       return futureTask;
    }

    @Override
    public <T> List<Future<T>> invokeAll(Collection<? extends Callable<T>> tasks) throws InterruptedException {
        List<Future<T>> futures = new ArrayList<>();
        for (Callable<T> task : tasks) {
            futures.add(submit(task));
        }
        for (Future<T> future : futures) {
            try {
                future.get();
            } catch (ExecutionException e) {}
        }
        return futures;
    }

    @Override
    public <T> List<Future<T>> invokeAll(Collection<? extends Callable<T>> tasks, long timeout, TimeUnit unit) throws InterruptedException {
        long deadline = System.nanoTime() + unit.toNanos(timeout);
        List<Future<T>> futures = new ArrayList<>();
        for (Callable<T> task : tasks) {
            futures.add(submit(task));
        }
        for (Future<T> future : futures) {
            long remaining = deadline - System.nanoTime();
            if (remaining <= 0) {
                future.cancel(true);
            } else {
                try {
                    future.get(remaining, TimeUnit.NANOSECONDS);
                } catch (TimeoutException | ExecutionException e) {}
            }
        }
        return futures;
    }

    @Override
    public <T> T invokeAny(Collection<? extends Callable<T>> tasks) throws InterruptedException, ExecutionException {
        ExecutorCompletionService<T> completionService = new ExecutorCompletionService<>(this);
        List<Future<T>> futures = new ArrayList<>();

        try {
            for (Callable<T> task : tasks) {
                futures.add(completionService.submit(task));
            }

            ExecutionException exception = null;
            for(int i = 0; i < tasks.size(); i++) {
                Future<T> completed;
                try {
                    completed = completionService.take();
                } catch (InterruptedException e) {
                    for(Future<T> future : futures) future.cancel(true);
                    throw e;
                }

                try {
                    T result = completed.get();
                    for(Future<T> future : futures) {
                        if(future != completed) future.cancel(true);
                    }
                    return result;
                } catch (ExecutionException e) {
                    exception = e;
                } catch (CancellationException e) {}
            }

            if(exception != null) throw exception;
            throw new ExecutionException(exception);
        } finally {
            for (Future<T> future : futures) {
                future.cancel(true);
            }
        }
    }

    @Override
    public <T> T invokeAny(Collection<? extends Callable<T>> tasks, long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
        long timeoutNanos = unit.toNanos(timeout);
        long startTime = System.nanoTime();
        ExecutorCompletionService<T> ecs = new ExecutorCompletionService<>(this);
        List<Future<T>> futures = new ArrayList<>(tasks.size());

        try {
            for (Callable<T> task : tasks) {
                futures.add(ecs.submit(task));
            }

            ExecutionException lastEE = null;
            long remaining = timeoutNanos;

            for (int i = 0; i < tasks.size(); i++) {
                Future<T> completed;
                if (remaining <= 0L) {
                    throw new TimeoutException("Timeout while waiting for tasks");
                }
                completed = ecs.poll(remaining, TimeUnit.NANOSECONDS);
                if (completed == null) {
                    throw new TimeoutException("Timeout while waiting for tasks");
                }

                try {
                    T result = completed.get();
                    for (Future<T> f : futures) {
                        if (f != completed) f.cancel(true);
                    }
                    return result;
                } catch (ExecutionException ee) {
                    lastEE = ee;
                } catch (CancellationException ce) {
                }

                long now = System.nanoTime();
                remaining = timeoutNanos - (now - startTime);
            }

            if (lastEE != null) throw lastEE;
            throw new ExecutionException(new Exception("No task completed successfully"));
        } finally {
            for (Future<T> f : futures) f.cancel(true);
        }
    }

    @Override
    public void execute(Runnable command) {
        if (shutdown) throw new RejectedExecutionException("ExecutorService has been shutdown");
        try {
            taskQueue.put(command);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RejectedExecutionException("Task submission interrupted",e);
        }
    }

    private class Worker implements Runnable {
        @Override
        public void run() {
            while (true) {
                try {
                    Runnable task = taskQueue.poll(100, TimeUnit.MILLISECONDS);
                    if (task != null) {
                        task.run();
                    } else if (shutdown && taskQueue.isEmpty()) {
                        break;
                    }
                } catch (InterruptedException e) {
                    if (shutdown) break;
                    Thread.currentThread().interrupt();
                }
            }
        }
    }

    public static void main(String[] args) throws Exception {
        System.out.println("=== Тестирование CustomExecutorService ===");

        testPerformanceComparison();
        System.out.println("\n" + "==================================================" + "\n");

        testConcurrentExecution();
        System.out.println("\n" + "==================================================" + "\n");
        testShutdownBehavior();
    }

    private static void testPerformanceComparison() throws Exception {
        System.out.println("Test 1: Perfomance Comparison");
        int[] poolSizes = {10, 50, 100, 500};
        int taskCount = 10000;
        long taskSleep = 10;

        for (int poolSize : poolSizes) {
            System.out.println("\nPool size: " + poolSize);

            long startTime = System.currentTimeMillis();
            CustomExecutorService platformExecutor =
                    new CustomExecutorService(poolSize, false);
            submitTasks(platformExecutor, taskCount, taskSleep);
            platformExecutor.shutdown();
            platformExecutor.awaitTermination(60, TimeUnit.SECONDS);
            long platformTime = System.currentTimeMillis() - startTime;
            System.out.println("  Platform threads: " + platformTime + " ms");

            startTime = System.currentTimeMillis();
            CustomExecutorService virtualExecutor =
                    new CustomExecutorService(poolSize, true);
            submitTasks(virtualExecutor, taskCount, taskSleep);
            virtualExecutor.shutdown();
            virtualExecutor.awaitTermination(60, TimeUnit.SECONDS);
            long virtualTime = System.currentTimeMillis() - startTime;
            System.out.println("  Virtual threads:  " + virtualTime + " ms");
            System.out.println("  Difference: " + (platformTime - virtualTime) + " ms");
        }
    }

    private static void testConcurrentExecution() throws Exception {
        System.out.println("Test 2: Concurrent execution (1000 tasks)");

        AtomicInteger counter = new AtomicInteger(0);

        System.out.println("\nPlatform threads:");
        CustomExecutorService platformExecutor =
                new CustomExecutorService(10, false);
        for (int i = 0; i < 1000; i++) {
            platformExecutor.submit(() -> counter.incrementAndGet());
        }
        platformExecutor.shutdown();
        platformExecutor.awaitTermination(30, TimeUnit.SECONDS);
        System.out.println("  Final counter value: " + counter.get());
        System.out.println("  Counter reaches 1000: " + (counter.get() == 1000));

        System.out.println("\nVirtual threads:");
        counter.set(0);
        CustomExecutorService virtualExecutor =
                new CustomExecutorService(10, true);
        for (int i = 0; i < 1000; i++) {
            virtualExecutor.submit(() -> counter.incrementAndGet());
        }
        virtualExecutor.shutdown();
        virtualExecutor.awaitTermination(30, TimeUnit.SECONDS);
        System.out.println("  Final counter value: " + counter.get());
        System.out.println("  Counter reaches 1000: " + (counter.get() == 1000));
    }

    private static void testShutdownBehavior() throws Exception {
        System.out.println("Test 3: Shutdown behavior");

        CustomExecutorService executor = new CustomExecutorService(5, false);

        List<Future<?>> futures = new ArrayList<>();
        for (int i = 0; i < 20; i++) {
            futures.add(executor.submit(() -> {
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }));
        }

        executor.shutdown();
        System.out.println("isShutdown(): " + executor.isShutdown());

        boolean completed = executor.awaitTermination(10, TimeUnit.SECONDS);
        System.out.println("awaitTermination ended: " + completed);
        System.out.println("isTerminated(): " + executor.isTerminated());
        System.out.println("Number of successfully executed tasks: " +
                futures.stream().filter(Future::isDone).count());
    }

    private static void submitTasks(CustomExecutorService executor,
                                    int count, long sleepMs) {
        for (int i = 0; i < count; i++) {
            executor.execute(() -> {
                try {
                    Thread.sleep(sleepMs);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            });
        }
    }
}
