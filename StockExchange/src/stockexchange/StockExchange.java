package stockexchange;

import java.io.IOException;


public class StockExchange {

    public static final int PORT = 9999;

    public static void main(String[] args) throws IOException, InterruptedException {
        // Start the database
        Database database = new Database();
        // Start server
        Server server = new Server(PORT, database);
        Thread threadServer = new Thread(server);
        threadServer.start();
        // Start middleman
        MiddleMan middleman = new MiddleMan(database);
        Thread thread_middleman = new Thread(middleman);
        thread_middleman.start();
        
        threadServer.join();
        thread_middleman.join();
    }
}
