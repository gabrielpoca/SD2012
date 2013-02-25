package bank;

import java.net.ServerSocket;
import java.net.Socket;
import java.util.logging.Logger;

class ClientThread extends Thread {

    private Socket client;

    public ClientThread(Socket client) {
        this.client = client;
    }

    public void run() {

    }
}

public class Server extends Thread {

    private final static Logger LOGGER = Logger.getLogger(Server.class.getName());

    private boolean run = true;
    private int PORT;
    private Bank bank;

    public Server(int port, Bank bank) {
        PORT = port;
        this.bank = bank;
    }

    public void run() {
        try {
            ServerSocket server = new ServerSocket(PORT);

            while(run) {
                LOGGER.finest("Accepting connections");
                Socket client = server.accept();
                LOGGER.finest("Connection accepted");
                Thread t = new Thread(new ClientThread(client));
                t.start();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void end() {
        run = false;
    }
}
