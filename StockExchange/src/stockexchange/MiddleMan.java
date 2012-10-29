package stockexchange;

import java.io.DataOutputStream;
import java.io.IOException;
import java.util.LinkedList;
import java.util.logging.Level;
import java.util.logging.Logger;

class MiddleMan extends Thread {

    Database database;
    private boolean run;
    
    private final int INFINIT = 99999;

    public MiddleMan(Database database) {
        this.database = database;
        run = true;
    }

    public void run() {
        LinkedList<Entry> buyers = database.getBuyersDatabase();
        LinkedList<Entry> sellers = database.getSellersDatabase();
        while (run) {
            database.notification();
            if (!buyers.isEmpty() && !sellers.isEmpty()) {
                Entry buyer = null;
                Entry seller = null;
                int range = INFINIT;
                for(Entry b : buyers) {
                    for(Entry s : sellers) {
                        // Lock entrys for process.
                        b.lock();
                        s.lock();
                        if(b.getQuantity() == 0)
                            continue;                        
                        if(s.getQuantity() == 0)
                            continue;
                        int temp_range = b.getValue() - s.getValue();
                        if(temp_range >= 0 && temp_range < range) {
                            range = temp_range;
                            buyer = b;
                            seller = s;
                        } else {
                            // If no entry is going to be used unlock them
                            b.unlock();
                            s.unlock();
                        }
                    }
                }
                if(buyer != null && seller != null) {
                    int quantity = buyer.getQuantity();
                    if (seller.getQuantity() < quantity) {
                        quantity = seller.getQuantity();
                    }
                    updateEntry(buyer, quantity);
                    updateEntry(seller, quantity);
                    sendReport(buyer, "Bought "+quantity+" at price "+buyer.getValue());
                    sendReport(seller, "Sold "+quantity+" at price "+seller.getValue());
                    // Unlock entrys after update.
                    buyer.unlock();
                    seller.unlock();
                }
            }
        }
    }

    /**
     * Updates the entry by the given quantity.
     *
     * @param entry
     * @param quantity
     */
    private void updateEntry(Entry entry, int quantity) {
        entry.subQuantity(quantity);
        log("Update "+entry.toString());
    }

    /**
     * Returns a message to the entry.
     *
     * @param entry
     * @param message Message to be returned.
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

    public void end() {
        run = false;
    }
    
    private void log(String s) {
        System.out.println(s);
    }
}