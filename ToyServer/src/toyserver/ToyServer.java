/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package toyserver;

import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.HashMap;

/**
 *
 * @author gabrielpoca
 */
public class ToyServer implements ToyInterface {

    HashMap<String, Soft> soft_database;
    HashMap<String, Hard> hard_database;
    
    public ToyServer() {
	super();
	soft_database = new HashMap<String, Soft>();
	hard_database = new HashMap<String, Hard>();
    }
    
    public HardInterface getHard(String s) throws RemoteException {
	Hard hard = null;
	if(hard_database.containsKey(s)) {
	    hard = hard_database.get(s);
	}
	else {
	    hard = new Hard(s);
	    hard_database.put(s, hard);
	}
	return hard;
    }
    
    public SoftInterface getSoft(String s) throws RemoteException{
	Soft soft = null;
	if(soft_database.containsKey(s)) {
	    soft = soft_database.get(s);
	} else {
	    soft = new Soft(s);
	    soft_database.put(s, soft);
	}
	return soft;
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
	try {
	    ToyServer server = new ToyServer();
	    ToyInterface stub = (ToyInterface) UnicastRemoteObject.exportObject(server, 0);
	    Registry registry = LocateRegistry.getRegistry();
	    registry.bind("ToyServer", stub);
	    System.out.println("Server ready...");
	} catch (Exception e) {
	    e.printStackTrace();
	}
    }
}
