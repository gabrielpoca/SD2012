package Bank;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface BankInterface extends Remote {
    public int[] receive(int amount, int[] clock) throws RemoteException;
    public void transfer(int amount, int destiny) throws RemoteException;
}
