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
public interface ToyInterface extends Remote {
    public SoftInterface getSoft(String s) throws RemoteException;
    public HardInterface getHard(String s) throws RemoteException;
}
