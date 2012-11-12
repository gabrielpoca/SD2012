/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package toyserver;

import java.rmi.Remote;
import java.rmi.RemoteException;

/**
 *
 * @author gabrielpoca
 */
public interface HardInterface extends Remote {
    public String getHardName() throws RemoteException;
}
