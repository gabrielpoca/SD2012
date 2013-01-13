package Monitor;

import java.io.IOException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;


public class Monitor extends UnicastRemoteObject implements MonitorInterface {

    private int id_index;
    private int max_ids;
    private int[] clock;

    public Monitor(int max_ids) throws RemoteException {
        id_index = 0;
        this.max_ids = max_ids;
        clock = new int[max_ids];
    }

    /*
     * If other_clock for id is more than one step ahead for monitor's clock then return false.
     * If other_clock for other ids is more than monitor's clock there are actions
     * that have yet to be submited, so return false.
     * Otherwise return true.
     */
    private boolean isNext(int id, int[] other_clock) {
        for(int i = 0; i < clock.length; i++) {
            if(i == id) {
                if(other_clock[i] != clock[i] + 1)
                    return false;
            } else {
                if(other_clock[i] > clock[i])
                    return false;
            }
        }
        return true;
    }

    private synchronized void sync(int id, int[] other_clock) throws InterruptedException {
        while(!isNext(id, other_clock)) {
            System.out.println("Bank "+id+" not yet!");
            wait();
        }

        for(int i = 0; i < clock.length; i++) {
            if(other_clock[i] > clock[i])
                clock[i] = other_clock[i];
            System.out.print(clock[i]+" ");
        }

        System.out.print("\n");

        notifyAll();
    }

    public void begin(int source, int destiny, int amount, int[] clock) throws RemoteException {
        try {
            sync(source, clock);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        System.out.println("Begin "+source+" to "+ destiny);
    }

    public void end(int source, int destiny, int amount, int[] clock) throws RemoteException {
        try {
            sync(source, clock);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        System.out.println("End "+source+" to "+ destiny);
    }

    public synchronized int getID() throws Exception {
        if(id_index == max_ids)
            throw new Exception("Max banks reached!");
        return id_index++;
    }

    public int getMax() throws RemoteException {
        return max_ids;
    }

    public static void main(String[] args) {
        try {
            int max_ids = Integer.parseInt(args[0]);

            Monitor monitor = new Monitor(max_ids);
            Registry registry = null;

            registry = LocateRegistry.createRegistry(1099);
            registry.rebind("//localhost/monitor", monitor);

            System.out.println("Monitor up! Hit enter to stop...");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
