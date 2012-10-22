package stockexchange;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.LinkedList;
import java.util.TreeMap;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Stores a socket and a value. Used to as a database entry.
 *
 * @author gabrielpoca
 */
class Entry {

    Socket socket;
    int value;
    int quantity;

    public Entry(Socket sc, int value, int quantity) {
        this.socket = sc;
        this.value = value;
        this.quantity = quantity;
    }

    public Entry(Socket socket) {
        this.socket = socket;
        value = 0;
        quantity = 0;
    }

    public Socket getSocket() {
        return socket;
    }

    public int getValue() {
        return value;
    }

    public int getQuantity() {
        return quantity;
    }

    public synchronized void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public String toString() {
        return "Entry:: Socket=>" + socket + ", Value=>" + value + ", Quantity=>" + quantity;
    }
}

class Database {

    TreeMap<Integer, LinkedList<Entry>> buyDatabase;
    TreeMap<Integer, LinkedList<Entry>> sellDatabase;

    public Database() {
        buyDatabase = new TreeMap<Integer, LinkedList<Entry>>();
        sellDatabase = new TreeMap<Integer, LinkedList<Entry>>();
    }

    public synchronized Entry addBuyer(Entry entry) throws IOException {
        Entry selling = null;
        if (sellDatabase.containsKey(entry.getQuantity())) {
            if (!sellDatabase.get(entry.getQuantity()).isEmpty()) {
                LinkedList<Entry> sellList = sellDatabase.get(entry.getQuantity());
                for (int i = 0; i < sellList.size(); i++) {
                    if (sellList.get(i).getValue() <= entry.getValue()) {
                        selling = sellList.get(i);
                        sellList.remove(i);
                    }
                }
            }
        } else {
            if (!buyDatabase.containsKey(entry.getQuantity())) {
                buyDatabase.put(entry.getQuantity(), new LinkedList<Entry>());
            }
            buyDatabase.get(entry.getQuantity()).add(entry);
        }
        return selling;
    }

    public synchronized Entry addSeller(Entry entry) throws IOException {
        Entry buying = null;
        if (buyDatabase.containsKey(entry.getQuantity())) {
            if (!buyDatabase.get(entry.getQuantity()).isEmpty()) {
                LinkedList<Entry> sellList = buyDatabase.get(entry.getQuantity());
                for (int i = 0; i < sellList.size(); i++) {
                    if (sellList.get(i).getValue() >= entry.getValue()) {
                        buying = sellList.get(i);
                        sellList.remove(i);
                    }
                }
            }
        } else {
            if (!sellDatabase.containsKey(entry.getQuantity())) {
                sellDatabase.put(entry.getQuantity(), new LinkedList<Entry>());
            }
            sellDatabase.get(entry.getQuantity()).add(entry);
        }
        return buying;
    }
}


/*
 * Generic agent for buyers and sellers.
 * It asks the user how much he wants to sell/buy.
 * Submits to the database.
 */
class Agent extends Thread {

    Socket socket;
    Database database;

    public Agent(Socket socket, Database database) {
        this.socket = socket;
        this.database = database;
    }

    public void run() {
        try {
            // open read/write strams
            DataInputStream is = new DataInputStream(socket.getInputStream());
            DataOutputStream os = new DataOutputStream(socket.getOutputStream());
            boolean run = false;
            /* while client doesnt send exit. */
            while (!run) {
                // read/parse action and parameters
                String[] action = is.readUTF().split(" ");
                // if action is exit close the thread
                if(action[0].equals("exit")) {
                    run = false;
                    continue;
                }
                Dump.log(action[0] + " " + action[1] + " " + action[2]);                
                int value = Integer.parseInt(action[1]);
                int quantity = Integer.parseInt(action[2]);
                // create record for action
                Entry entry = new Entry(socket, value, quantity);
                Entry match_entry = null;
                // perform action
                if (action[0].equals(("buy"))) {
                    match_entry = database.addBuyer(entry);
                } else if (action[0].equals("sell")) {
                    match_entry = database.addSeller(entry);
                }
                // If a socket was returned close it and the current socket.
                if (match_entry != null) {
                    // value to be returned to each
                    int trade_quantity = entry.getQuantity();
                    if (match_entry.getQuantity() < trade_quantity) {
                        trade_quantity = match_entry.getQuantity();
                    }
                    // update entries
                    updateEntry(entry, trade_quantity);
                    updateEntry(match_entry, trade_quantity);
                    if(action[0].equals("buy")) {
                        sendReport(entry, "[Bought] "+trade_quantity);
                        sendReport(match_entry, "[Sold] "+trade_quantity);
                    } else {
                        sendReport(match_entry, "[Bought] "+trade_quantity);
                        sendReport(entry, "[Sold] "+trade_quantity);                       
                    }
                }
            }
        } catch (IOException ex) {
            Logger.getLogger(Agent.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     * Updates the entry by the given quantity and calls send report 
     * in the socket with the same quantity.
     * @param entry
     * @param quantity
     */
    private void updateEntry(Entry entry, int quantity) {
        entry.setQuantity(entry.getQuantity() - quantity);
    }

    /**
     * Returns the value to the socket.
     * @param entry
     * @param value Value to be returned.
     */
    private void sendReport(Entry entry, String message) {
        DataOutputStream os = null;
        try {
            os = new DataOutputStream(entry.getSocket().getOutputStream());
            os.writeUTF(message);
        } catch (IOException ex) {
            Logger.getLogger(Agent.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}

/*
 * Generic server for buyers and sellers.
 */
class Server extends Thread {

    // Port to listen
    int port;
    // Database
    Database database;

    public Server(int port, Database database) {
        this.port = port;
        this.database = database;
    }

    public void run() {
        try {
            Dump.log("Server starting on port " + port);
            ServerSocket serverSocket = new ServerSocket(port);
            Socket socket = null;
            boolean run = true;
            while (run) {
                socket = serverSocket.accept();
                Thread t = new Thread(new Agent(socket, database));
                t.start();
            }
        } catch (IOException ex) {
            Logger.getLogger(Server.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}

public class StockExchange {

    public static final int PORT = 9999;

    public static void main(String[] args) throws IOException, InterruptedException {
        // Start the database
        Database database = new Database();
        // Create the servers
        Server server = new Server(PORT, database);
        // Run the servers
        Thread threadServer = new Thread(server);
        threadServer.start();
        // Wait for the server to finish
        threadServer.join();
    }
}

class Dump {

    public static void log(String s) {
        System.out.println(s);
    }
}