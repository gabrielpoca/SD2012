package bank;

public interface BankInterface {
    public int balance();
    public boolean deposit(int amount);
    public boolean withdraw(int amount);
}
