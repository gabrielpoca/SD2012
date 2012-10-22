/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package stockexchange;

import java.net.Socket;

class Entry {

    Socket socket;
    int value;
    int quantity;

    public Entry(Socket sc, int value, int quantity) {
        this.socket = sc;
        this.value = value;
        this.quantity = quantity;
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

    public String toString() {
        return "Entry:: Socket=>" + socket + ", Value=>" + value + ", Quantity=>" + quantity;
    }
}