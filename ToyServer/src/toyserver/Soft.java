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
public class Soft extends UnicastRemoteObject implements SoftInterface {
 
    private String name;
    
    public Soft(String name) throws RemoteException{
	this.name = name;
    }
    
    public String getSoftName() {
	return name;
    }

}
