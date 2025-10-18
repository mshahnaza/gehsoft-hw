package org.example.hw05;

import java.util.concurrent.locks.ReentrantLock;

public class Deadlock {

    public static void main(String[] args) throws Exception {
        System.out.println("First deadlock");
        deadlockIntrinsicLocks();
        System.out.println();
        System.out.println("Second deadlock");
        deadlockReentrantLocks();
        System.out.println();
        System.out.println("Third deadlock");
        deadlockSingleThreadJoin();
    }

    public static void deadlockIntrinsicLocks() throws InterruptedException {
        final Object lock1 = new Object();
        final Object lock2 = new Object();

        Thread t1 = new Thread(() -> {
            synchronized (lock1) {
                System.out.println("Thread 1: Acquired lock1");

                try { Thread.sleep(100); } catch (InterruptedException e) {}

                System.out.println("Thread 1: Waiting for lock2");
                synchronized (lock2) {
                    System.out.println("Thread 1: Acquired lock2");
                }
            }
        });

        Thread t2 = new Thread(() -> {
            synchronized (lock2) {
                System.out.println("Thread 2: Acquired lock2");

                try { Thread.sleep(100); } catch (InterruptedException e) {}

                System.out.println("Thread 2: Waiting for lock1...");
                synchronized (lock1) {
                    System.out.println("Thread 2: Acquired lock1");
                }
            }
        });

        t1.start();
        t2.start();

        t1.join(500);
        t2.join(500);

        if (t1.isAlive() || t2.isAlive()) {
            System.out.println("Deadlock detected!");
        }

    }


    public static void deadlockReentrantLocks() throws InterruptedException {
        final ReentrantLock lock1 = new ReentrantLock();
        final ReentrantLock lock2 = new ReentrantLock();
        Thread t1 = new Thread(() -> {
            lock1.lock();
            System.out.println("Thread 1: Acquired lock1");
            
            try { Thread.sleep(100); } catch (InterruptedException e) {}
            
            System.out.println("Thread 1: Waiting for lock2...");
            lock2.lock();
            try {
                System.out.println("Thread 1: Acquired lock2");
            } finally {
                lock2.unlock();
                lock1.unlock();
            }
        });
        Thread t2 = new Thread(() -> {
            lock2.lock();
            System.out.println("Thread 2: Acquired lock2");
            
            try { Thread.sleep(100); } catch (InterruptedException e) {}
            
            System.out.println("Thread 2: Waiting for lock1...");
                lock1.lock();
                try {
                    System.out.println("Thread 2: Acquired lock1");
                } finally {
                    lock1.unlock();
                    lock2.unlock();
                }
        });

        t1.start();
        t2.start();

        t1.join(500);
        t2.join(500);

        if (t1.isAlive() || t2.isAlive()) {
            System.out.println("Deadlock detected!");
        }

    }

    public static void deadlockSingleThreadJoin() throws InterruptedException {
        Thread[] threads = new Thread[2];

        threads[0] = new Thread(() -> {
            System.out.println("Thread 1 running, waiting for Thread 2");
            try {
                threads[1].join();
            } catch (InterruptedException e) {}
        });

        threads[1] = new Thread(() -> {
            System.out.println("Thread 2 running, waiting for Thread 1");
            try {
                threads[0].join();
            } catch (InterruptedException e) {}
        });

        threads[0].start();
        threads[1].start();



        Thread.sleep(500);
        if (threads[0].isAlive() && threads[1].isAlive()) {
            System.out.println("Deadlock detected in join demo!");
        }
    }
}