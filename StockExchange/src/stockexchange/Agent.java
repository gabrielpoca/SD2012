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
                    database.addBuyer(entry);
                } else if (action[0].equals("sell")) {
                    database.addSeller(entry);
                }
            }
        } catch (IOException ex) {
            Logger.getLogger(Agent.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    
    private void log(String s) {
        System.out.println("[Agent] "+s);
    }
}