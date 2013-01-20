package bookstore;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface CartInterface extends Remote {

    void add(BookInterface book, int xid) throws RemoteException;

    boolean buy(int _xid) throws RemoteException;
}
