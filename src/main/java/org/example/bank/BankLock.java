package org.example.bank;

import java.util.concurrent.locks.ReentrantLock;

public class BankLock extends Bank {
    private ReentrantLock[] locks;
    public BankLock(int numberOfAccounts, long minBalance, long maxBalance) {
        super(numberOfAccounts, minBalance, maxBalance);
        locks = new ReentrantLock[numberOfAccounts];
        for (int i = 0; i < numberOfAccounts; i++) locks[i] = new ReentrantLock();
    }

    public void transfer(int from, int to, long amount) {
        if (from == to || balances[from] < amount) return;

        int min = Math.min(from, to);
        int max = Math.max(from, to);

        locks[min].lock();
        locks[max].lock();

        try {
            setAccountBalance(from, getAccountBalance(from) - amount);
            setAccountBalance(to, getAccountBalance(to) + amount);
        } finally {
            locks[max].unlock();
            locks[min].unlock();
        }
    }
}
