
package stockexchange;

import java.net.Socket;
import java.util.ArrayList;

class DatabaseEntry {
    private Socket socket;
    private int value;
    public DatabaseEntry(Socket socket, int value) {
	this.socket = socket;
	this.value = value;
    }
    public Socket getSocket() {
	return socket;
    }
    public int getValue() {
	return value;
    }
}

class Database {
    private ArrayList<DatabaseEntry> sell_databe;
    private ArrayList<DatabaseEntry> buy_database;
}

public class StockExchange {

    public static void main(String[] args) {

    }
}
