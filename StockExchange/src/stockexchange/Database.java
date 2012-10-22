/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package stockexchange;

import java.io.IOException;
import java.util.LinkedList;
import java.util.TreeMap;


class Database {

    TreeMap<Integer, LinkedList<Entry>> buyDatabase;
    TreeMap<Integer, LinkedList<Entry>> sellDatabase;

    public Database() {
        buyDatabase = new TreeMap<Integer, LinkedList<Entry>>();
        sellDatabase = new TreeMap<Integer, LinkedList<Entry>>();
    }

    public synchronized Entry addBuyer(Entry entry) throws IOException {
        Entry selling = null;
        if (sellDatabase.containsKey(entry.getQuantity())) {
            if (!sellDatabase.get(entry.getQuantity()).isEmpty()) {
                LinkedList<Entry> sellList = sellDatabase.get(entry.getQuantity());
                for (int i = 0; i < sellList.size(); i++) {
                    if (sellList.get(i).getValue() <= entry.getValue()) {
                        selling = sellList.get(i);
                        sellList.remove(i);
                    }
                }
            }
        } else {
            if (!buyDatabase.containsKey(entry.getQuantity())) {
                buyDatabase.put(entry.getQuantity(), new LinkedList<Entry>());
            }
            buyDatabase.get(entry.getQuantity()).add(entry);
        }
        return selling;
    }

    public synchronized Entry addSeller(Entry entry) throws IOException {
        Entry buying = null;
        if (buyDatabase.containsKey(entry.getQuantity())) {
            if (!buyDatabase.get(entry.getQuantity()).isEmpty()) {
                LinkedList<Entry> sellList = buyDatabase.get(entry.getQuantity());
                for (int i = 0; i < sellList.size(); i++) {
                    if (sellList.get(i).getValue() >= entry.getValue()) {
                        buying = sellList.get(i);
                        sellList.remove(i);
                    }
                }
            }
        } else {
            if (!sellDatabase.containsKey(entry.getQuantity())) {
                sellDatabase.put(entry.getQuantity(), new LinkedList<Entry>());
            }
            sellDatabase.get(entry.getQuantity()).add(entry);
        }
        return buying;
    }
}


