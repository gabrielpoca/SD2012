/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package stockexchange;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Handles clients allowing each to submit new requests to the database.
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
                if (action[0].equals("exit")) {
                    run = false;
                    continue;
                }
                log(action[0] + " " + action[1] + " " + action[2]);
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
                    if (action[0].equals("buy")) {
                        sendReport(entry, "[Bought] " + trade_quantity);
                        sendReport(match_entry, "[Sold] " + trade_quantity);
                    } else {
                        sendReport(match_entry, "[Bought] " + trade_quantity);
                        sendReport(entry, "[Sold] " + trade_quantity);
                    }
                }
            }
        } catch (IOException ex) {
            Logger.getLogger(Agent.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     * Updates the entry by the given quantity and calls send report in the
     * socket with the same quantity.
     *
     * @param entry
     * @param quantity
     */
    private void updateEntry(Entry entry, int quantity) {
        entry.setQuantity(entry.getQuantity() - quantity);
    }

    /**
     * Returns the value to the socket.
     *
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
    
    private void log(String s) {
        System.out.println("[Agent] "+s);
    }
}