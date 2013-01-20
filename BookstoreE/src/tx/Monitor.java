/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package tx;

import java.rmi.Naming;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.HashMap;

/**
 *
 * @author gabriel
 */
public class Monitor extends UnicastRemoteObject implements MonitorInterface {
    
    private int sequence;
    HashMap<Integer, ArrayList<Resource>> resources;
    
    public Monitor() throws RemoteException {
        sequence = 0;
        resources = new HashMap<Integer, ArrayList<Resource>>();
    }
    
    public int begin() throws RemoteException {
	log("Begin...");
        resources.put(sequence, new ArrayList<Resource>());
        return sequence++;
    }
    
    public void addResource(int xid, Resource resource) throws RemoteException {
        resources.get(xid).add(resource);
    }
    
    public boolean commit(int xid) throws RemoteException {
        boolean res = true;
        log("Phase 1");
        for(Resource r : resources.get(xid))
            res = res && r.phase1(xid);
        
        if(!res)
            return false;
        
        log("Phase 2");
        
        for(Resource r : resources.get(xid))
            r.phase2(xid, true);
        
        return true;
    }
    
    public static void main(String[] args) {
        try {
            LocateRegistry.createRegistry(1099);
            Naming.rebind("//localhost/monitor", new Monitor());
	    System.out.println("Monitor ready...");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    private void log(String s) {
        System.out.println("[Monitor] "+s);
    }
}
