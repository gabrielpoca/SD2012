package tx;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface Resource extends Remote {

    public boolean phase1(int xid) throws RemoteException;

    public void phase2(int xid, boolean commit) throws RemoteException;
}
