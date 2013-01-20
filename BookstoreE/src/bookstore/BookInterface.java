/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package bookstore;

import java.rmi.Remote;
import java.rmi.RemoteException;

/**
 *
 * @author gabriel
 */
public interface BookInterface extends Remote {
    public int getStock(int xid) throws RemoteException;
    public void buy(int xid) throws RemoteException;
}
