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
public interface BookstoreInterface extends Remote {
    public BookInterface find(int isbn) throws RemoteException;
}
