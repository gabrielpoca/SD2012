/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package toyserver;

import java.io.Serializable;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;

/**
 *
 * @author gabrielpoca
 */
public class Hard extends UnicastRemoteObject implements HardInterface {

    private String name;
    
    public Hard(String name) throws RemoteException {
	this.name = name;
    }
    
    public String getHardName() {
	return name;
    }
    
}
