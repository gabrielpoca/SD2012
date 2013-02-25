package bank;


import java.util.logging.Logger;

public class Bank {
    private static final Logger LOGGER = Logger.getLogger(Bank.class.getName());

    private int amount;

    public Bank(int amount) {
        this.amount = amount;
    }

    public synchronized int balance() {
        LOGGER.entering("Balance", "");
        return amount;
    }

    public synchronized boolean deposit(int amount) {
        LOGGER.entering("Deposit", ""+amount);
        this.amount += amount;
        return true;
    }

    public synchronized boolean withdraw(int amount) {
        LOGGER.entering("Withdraw", ""+amount);
        if (this.amount >= amount) {
            this.amount -= amount;
            return true;
        }
        return false;
    }


}
