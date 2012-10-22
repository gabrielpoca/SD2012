/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package stockexchange;

import java.net.Socket;
import java.util.ArrayList;

class Entry {

    Socket socket;
    int value;
    int quantity;
    ArrayList<Integer> log;

    public Entry(Socket sc, int value, int quantity) {
        this.socket = sc;
        this.value = value;
        this.quantity = quantity;
        log = new ArrayList<Integer>();
    }

    public Entry(Socket socket) {
        this.socket = socket;
        value = 0;
        quantity = 0;
    }

    public Socket getSocket() {
        return socket;
    }

    public int getValue() {
        return value;
    }

    public int getQuantity() {
        return quantity;
    }

    public synchronized void setQuantity(int quantity) {
        this.quantity = quantity;
    }
    
    public void subQuantity(int quantity) {
        log.add(quantity);
        this.quantity -= quantity;
    }

    public String toString() {
        return "Entry:: Socket=>" + socket + ", Value=>" + value + ", Quantity=>" + quantity;
    }
}