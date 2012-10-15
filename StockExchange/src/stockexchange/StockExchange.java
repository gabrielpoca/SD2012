
package stockexchange;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.LinkedList;
import java.util.TreeMap;
import java.util.logging.Level;
import java.util.logging.Logger;



/**
 * Stores a socket and a value.
 * Used to as a database entry.
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
    
    public Socket getSocket() {
	return socket;
    }
    
    public int getValue() {
	return value;
    }
    
    public int getQuantity() {
	return quantity;
    }

    public String toString() {
	return "Entry:: Socket=>"+socket+", Value=>"+value+", Quantity=>"+quantity;
    }
}

class Database {
    
    TreeMap<Integer, LinkedList<Entry>> buyDatabase;
    TreeMap<Integer, LinkedList<Entry>> sellDatabase;
    
    public Database() {
	buyDatabase = new TreeMap<Integer, LinkedList<Entry>>();
	sellDatabase = new TreeMap<Integer, LinkedList<Entry>>();
    }
    
    public synchronized Entry addBuyer(Socket socket, int value, int quantity) throws IOException {
	Entry selling = null;
	if(sellDatabase.containsKey(quantity)) {
	    if(!sellDatabase.get(quantity).isEmpty()) {
		LinkedList<Entry> sellList = sellDatabase.get(quantity);
		for(int i = 0; i < sellList.size(); i++) {
		    if(sellList.get(i).getValue() <= value) {
			selling = sellList.get(i);
			sellList.remove(i);
		    }
		}
	    }
	} else {
	    if(!buyDatabase.containsKey(quantity))
		buyDatabase.put(quantity, new LinkedList<Entry>());
	    buyDatabase.get(quantity).add(new Entry(socket, value, quantity));	    
	}
	return selling;
    }
    
    public synchronized Entry addSeller(Socket socket, int value, int quantity) throws IOException {
	Entry buying = null;
	if(buyDatabase.containsKey(quantity)) {
	    if(!buyDatabase.get(quantity).isEmpty()) {
		LinkedList<Entry> sellList = buyDatabase.get(quantity);
		for(int i = 0; i < sellList.size(); i++) {
		    if(sellList.get(i).getValue() >= value) {
			buying = sellList.get(i);
			sellList.remove(i);
		    }
		}
	    }
	} else {
	    if(!sellDatabase.containsKey(quantity))
		sellDatabase.put(quantity, new LinkedList<Entry>());
	    sellDatabase.get(quantity).add(new Entry(socket, value, quantity));	    
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
	    // read the action to take
	    String[] action = is.readUTF().split(" ");
	    Dump.log(action[0]+" "+action[1]+" "+action[2]);
	    	    
	    int value = Integer.parseInt(action[1]);
	    int quantity = Integer.parseInt(action[2]);
	    
	    Entry databaseReturnEntry = null;
	    // run the action
	    if(action[0].equals(("buy"))) {
		databaseReturnEntry = database.addBuyer(socket, value, quantity);
	    } else if (action[0].equals("sell")) {
		databaseReturnEntry = database.addSeller(socket, value, quantity);
	    } 
	    // If a socket was returned then close it and the current socket.
	    if (databaseReturnEntry != null) {
		Dump.log(databaseReturnEntry.toString());
		// value to be returned to each
		int returningValue = value;
		if (databaseReturnEntry.getValue() < returningValue)
		    returningValue = databaseReturnEntry.getValue();
		sendEqualResponse(socket, databaseReturnEntry.getSocket(), returningValue);
	    }
	} catch (IOException ex) {
	    Logger.getLogger(Agent.class.getName()).log(Level.SEVERE, null, ex);
	}
    }
    
    /**
     * Returns the value and closes each socket.
     * @param s1 First socket.
     * @param socket2 Seconds socket.
     * @param value Value to be returned.
     */
    private void sendEqualResponse(Socket socket1, Socket socket2, int value) throws IOException {
	DataOutputStream os1 = new DataOutputStream(socket1.getOutputStream());
	os1.writeInt(value);
	DataOutputStream os2 = new DataOutputStream(socket2.getOutputStream());
	os2.writeInt(value);
	os1.close();
	os2.close();
	socket1.close();
	socket2.close();
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
	    Dump.log("Server starting on port "+port);
	    ServerSocket serverSocket = new ServerSocket(port);
	    Socket socket = null;
	    boolean run = true;
	    while(run) {
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