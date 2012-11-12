/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package toyserver;

import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

/**
 *
 * @author gabrielpoca
 */
public class ToyClient {
    public static void main(String args[]) {
	try {
	    Registry registry = LocateRegistry.getRegistry();
	    ToyInterface server = (ToyInterface) registry.lookup("ToyServer");
	    HardInterface hard = server.getHard("My Hard Name");
	    SoftInterface soft = server.getSoft("My Soft Name");
	    
	    System.out.println("HARD "+hard.getHardName());
	    System.out.println("SOFT "+soft.getSoftName());
	} catch (Exception e) {
	    e.printStackTrace();
	}
    }
}
