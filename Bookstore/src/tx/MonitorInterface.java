/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package tx;

import java.rmi.Remote;
import java.rmi.RemoteException;

/**
 *
 * @author gabriel
 */
public interface MonitorInterface extends Remote {
    public void addResource(int xid, Resource resource) throws RemoteException;
    public int begin() throws RemoteException;
}
