
package bookstore;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface CartFactoryInterface extends Remote {
    CartInterface make() throws RemoteException;
}
