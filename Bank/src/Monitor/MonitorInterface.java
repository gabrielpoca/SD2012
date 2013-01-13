package Monitor;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface MonitorInterface extends Remote {
    public int getID() throws Exception;
    public int getMax() throws RemoteException;

    public void begin(int source, int destiny, int amount, int[] clock) throws RemoteException;
    public void end(int source, int destiny, int amount, int[] clock) throws RemoteException;
}
