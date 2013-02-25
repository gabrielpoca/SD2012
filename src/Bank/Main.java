package bank;

import java.util.Random;
import java.util.logging.Logger;


public class Main {
    public final static Logger LOGGER = Logger.getLogger("Bank");

    public static void main(String args[]) {
        Random random = new Random(500000);

        int port = Integer.parseInt(args[0]);
        Bank bank = new Bank(random.nextInt());
        Server server = new Server(port, bank);

        Thread t = new Thread(server);
        t.start();
    }
}
