package org.example.hw05.bank;

public class BankSynchronized extends Bank{
    public BankSynchronized(int numberOfAccounts, long minBalance, long maxBalance) {
        super(numberOfAccounts, minBalance, maxBalance);
    }

    public synchronized void transfer(int from, int to, long amount) {
        if (from == to || balances[from] < amount) return;
        setAccountBalance(from, getAccountBalance(from) - amount);
        setAccountBalance(to, getAccountBalance(to) + amount);
    }
}
