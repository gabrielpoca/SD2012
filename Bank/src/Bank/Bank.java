package Bank;

import Monitor.MonitorInterface;

import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.Random;

public class Bank extends UnicastRemoteObject implements BankInterface {


    private int id;
    private int balance;

    private int[] clock;
    private int max_ids;

    private MonitorInterface monitor;
    private Registry registry;

    public Bank(int id, int max_ids, MonitorInterface monitor, int balance, Registry registry) throws RemoteException {
        this.balance = balance;
        this.id = id;
        this.monitor = monitor;
        this.max_ids = max_ids;
        clock = new int[max_ids];
        this.registry = registry;
    }

    private void sync(int[] other) {
        for(int i = 0; i < other.length; i++) {
            if(other[i] > clock[i])
                clock[i] = other[i];
        }
    }

    public synchronized int[] receive(int balance, int[] sequence) throws RemoteException {
        sync(sequence);
        this.balance += balance;
        return sequence;
    }

    public synchronized void transfer(int amount, int destiny) throws RemoteException {
        clock[id]++;
        monitor.begin(id, destiny, amount, clock);
        balance -= amount;
        BankInterface bank = null;

        try {
            bank = (BankInterface) registry.lookup("//localhost/bank" + destiny);
        } catch (Exception e) {
            e.printStackTrace();
        }

        sync(bank.receive(amount, clock));
        clock[id]++;
        monitor.end(id, destiny, amount, clock);
    }

    public static void main(String[] args) {
        try {
            Registry registry = LocateRegistry.getRegistry(1099);
            MonitorInterface monitor = (MonitorInterface) registry.lookup("//localhost/monitor");

            Random random = new Random();
            int id = monitor.getID();
            int max = monitor.getMax();
            Bank bank = new Bank(id, max, monitor, random.nextInt(100), registry);

            registry.bind("//localhost/bank"+id, bank);
            System.out.println("Bank "+id+" ready! Hit to start...");
            System.in.read();

            for(int j = 0; j < 1000; j++) {
                int d = random.nextInt(max-1);
                if (d>=id) d++;

                int q = random.nextInt(10);

                bank.transfer(q, d);
                Thread.sleep(random.nextInt(5000));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
