
package bookstore;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface CartInterface extends Remote {
    Cart make() throws RemoteException;
    
}
