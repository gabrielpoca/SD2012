/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package tx;

import java.rmi.Naming;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import org.apache.derby.jdbc.EmbeddedXADataSource;

import static bookstore.Bookstore.DATABASE_NAME;
import javax.transaction.xa.XAResource;
/**
 *
 * @author gabriel
 */
public class TxObject extends UnicastRemoteObject implements Resource {

    private MonitorInterface monitor;
    
    public TxObject() throws RemoteException {
        super();
        try {
            monitor = (MonitorInterface) Naming.lookup("//localhost/monitor");
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
    
    protected synchronized void enter(int xid) throws RemoteException {
	log("enter...");
	monitor.addResource(xid, this);
	log("finishing enter...");
    }
    
    public boolean phase1(int xid) throws RemoteException {
	try {
	    EmbeddedXADataSource data_source = new EmbeddedXADataSource();
	    data_source.setDatabaseName(DATABASE_NAME);
	    XAResource resource = data_source.getXAConnection().getXAResource();
	    resource.prepare(new XidImpl(xid));
	} catch (Exception ex) {
	    ex.printStackTrace();
	}
	log("Phase1");
	return true;
    }

    public synchronized void phase2(int xid, boolean commit) throws RemoteException {
	try {
	    EmbeddedXADataSource data_source = new EmbeddedXADataSource();
	    data_source.setDatabaseName(DATABASE_NAME);
	    XAResource resource = data_source.getXAConnection().getXAResource();
	    if (commit)
		resource.commit(new XidImpl(xid), false);
	    else resource.rollback(new XidImpl(xid));
	} catch (Exception ex) {
	    ex.printStackTrace();
	}
	log("Phase2");
    }

    protected void log(String s) {
	System.out.println("[Object] "+s);
    }
}
