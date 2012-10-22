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
            log("Starting on port " + port);
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
    
    public static void log(String s) {
        System.out.println("[Server] "+s);
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
