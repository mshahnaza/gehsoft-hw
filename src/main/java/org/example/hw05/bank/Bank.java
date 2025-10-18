package org.example.hw05.bank;

import java.math.BigInteger;
import java.util.concurrent.ThreadLocalRandom;

public class Bank {
    protected long[] balances;

    public Bank(int numberOfAccounts, long minBalance, long maxBalance) {
        balances = new long[numberOfAccounts];
        for (int i = 0; i < numberOfAccounts; i++) {
            balances[i] = ThreadLocalRandom.current().nextLong(minBalance, maxBalance + 1);
        }
    }

    public Bank() {
    }

    public int pickRandomAccountId() {
        return ThreadLocalRandom.current().nextInt(balances.length);
    }
    public long getAccountBalance(int accountId) {
        return balances[accountId];
    }
    public void setAccountBalance(int accountId, long newBalance) {
        balances[accountId] = newBalance;
    }
    public BigInteger getSumOfAllAccounts() {
        long sum = 0;
        for (long balance : balances) {
            sum += balance;
        }
        return BigInteger.valueOf(sum);
    }

}
