package org.example.hw05.bank;

import java.math.BigInteger;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class BankWork {
    private static final int THREADS = 1000;

    public static void main(String[] args) throws InterruptedException {
        testBank(new BankSynchronized(200, 0L, 1_000L), "BankSynchronized");
        testBank(new BankLock(200, 0L, 1_000L), "BankLock");
        testBank(new BankAtomic(200, 0L, 1_000L), "BankAtomic");
    }

    private static void testBank(Bank bank, String bankName) throws InterruptedException {
        System.out.println(bankName + " initial total: " + bank.getSumOfAllAccounts());

        CountDownLatch latch = new CountDownLatch(THREADS);
        ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor();

        for (int i = 0; i < THREADS; i++) {
            executor.submit(() -> {
                try {
                    int from = bank.pickRandomAccountId();
                    int to   = bank.pickRandomAccountId();

                    long x = Math.min(bank.getAccountBalance(from), 100L);

                    if (bank instanceof BankSynchronized) {
                        ((BankSynchronized) bank).transfer(from, to, x);
                    } else if (bank instanceof BankLock) {
                        ((BankLock) bank).transfer(from, to, x);
                    } else if (bank instanceof BankAtomic) {
                        ((BankAtomic) bank).transfer(from, to, x);
                    }

                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await();
        executor.shutdown();

        BigInteger finalTotal = bank.getSumOfAllAccounts();
        System.out.println(bankName + " final total: " + finalTotal);
    }
}
