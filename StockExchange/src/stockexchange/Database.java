/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package stockexchange;

import java.io.IOException;
import java.net.Socket;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.TreeMap;
import java.util.logging.Level;
import java.util.logging.Logger;


public class Database {

    LinkedList<Entry> buyers_database;
    LinkedList<Entry> sellers_database;
    HashMap<Long, ClientEntry> clients;

    public Database() {
        buyers_database = new LinkedList<Entry>();
        sellers_database = new LinkedList<Entry>();
        clients = new HashMap<Long, ClientEntry>();
    }
    
    public synchronized void addClient(ClientEntry entry) {
        clients.put(entry.getID(), entry);
    }
    
    public synchronized void updateClientSocket(long id, Socket socket) {
        clients.get(id).setSocket(socket);
    }

    public synchronized void addBuyer(Entry entry) throws IOException {
        buyers_database.add(entry);
        notify();
    }

    public synchronized void addSeller(Entry entry) throws IOException {
        sellers_database.add(entry);
        notify();
    }
    
    public synchronized void notification() {
        try {
            wait();
        } catch (InterruptedException ex) {
            Logger.getLogger(Database.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    public LinkedList<Entry> getBuyersDatabase() {
        return buyers_database;
    }
    
    public LinkedList<Entry> getSellersDatabase() {
        return sellers_database;
    }
}


