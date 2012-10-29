/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package stockexchange;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.logging.Level;
import java.util.logging.Logger;


/**
 * Generic server for buyers and sellers. It is responsible for
 * listening on port and create an agent for each  new connection.
 */
public class Server extends Thread {

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
            log("Starting on port " + port);
            ServerSocket serverSocket = new ServerSocket(port);
            Socket socket = null;
            boolean run = true;
            long id = 1;
            while (run) {
                socket = serverSocket.accept();
                Thread t = new Thread(new Agent(socket, database, id));
                t.start();
                id++;
            }
        } catch (IOException ex) {
            Logger.getLogger(Server.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    public static void log(String s) {
        System.out.println("[Server] "+s);
    }    
}