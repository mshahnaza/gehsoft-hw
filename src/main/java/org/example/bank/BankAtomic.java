package org.example.bank;

import java.math.BigInteger;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicLong;

public class BankAtomic {
    private final AtomicLong[] balances;

    public BankAtomic(int numberOfAccounts, long minBalance, long maxBalance) {
        balances = new AtomicLong[numberOfAccounts];
        for (int i = 0; i < numberOfAccounts; i++) {
            balances[i] = new AtomicLong(ThreadLocalRandom.current().nextLong(minBalance, maxBalance + 1));
        }
    }

    public int pickRandomAccountId() {
        return ThreadLocalRandom.current().nextInt(balances.length);
    }
    public long getAccountBalance(int accountId) {
        return balances[accountId].get();
    }
    public void setAccountBalance(int accountId, long newBalance) {
        balances[accountId].set(newBalance);
    }
    public BigInteger getSumOfAllAccounts() {
        long sum = 0;
        for (AtomicLong balance : balances) {
            sum += balance.get();
        }
        return BigInteger.valueOf(sum);
    }

    public void transfer(int from, int to, long amount) {
        if (from == to) return;
        long oldVal;
        do {
            oldVal = balances[from].get();
            if (oldVal < amount) return;
        } while (!balances[from].compareAndSet(oldVal, oldVal - amount));
        balances[to].getAndAdd(amount);

    }

}
