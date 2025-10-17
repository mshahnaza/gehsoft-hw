package org.example.bank;

import java.math.BigInteger;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicLong;

public class BankAtomic extends Bank {
    private final AtomicLong[] atomicBalances;

    public BankAtomic(int numberOfAccounts, long minBalance, long maxBalance) {
        atomicBalances = new AtomicLong[numberOfAccounts];
        for (int i = 0; i < numberOfAccounts; i++) {
            atomicBalances[i] = new AtomicLong(ThreadLocalRandom.current().nextLong(minBalance, maxBalance + 1));
        }
    }

    @Override
    public int pickRandomAccountId() {
        return ThreadLocalRandom.current().nextInt(atomicBalances.length);
    }

    @Override
    public long getAccountBalance(int accountId) {
        return atomicBalances[accountId].get();
    }

    @Override
    public void setAccountBalance(int accountId, long newBalance) {
        atomicBalances[accountId].set(newBalance);
    }

    @Override
    public BigInteger getSumOfAllAccounts() {
        long sum = 0;
        for (AtomicLong balance : atomicBalances) {
            sum += balance.get();
        }
        return BigInteger.valueOf(sum);
    }

    public void transfer(int from, int to, long amount) {
        if (from == to) return;
        long oldVal;
        do {
            oldVal = atomicBalances[from].get();
            if (oldVal < amount) return;
        } while (!atomicBalances[from].compareAndSet(oldVal, oldVal - amount));
        atomicBalances[to].getAndAdd(amount);

    }

}
