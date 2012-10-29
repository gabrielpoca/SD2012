/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package stockexchange;

import java.net.Socket;
import java.util.ArrayList;
import java.util.concurrent.locks.ReentrantLock;

class Entry {

    private ClientEntry client;
    private int value;
    private int quantity;
    
    private ReentrantLock lock = new ReentrantLock();

    public Entry(int value, int quantity, ClientEntry client) {
        this.value = value;
        this.quantity = quantity;
        this.client = client;
    }

    public Entry() {
        value = 0;
        quantity = 0;
    }

    public Socket getSocket() {
        return client.getSocket();
    }

    public int getValue() {
        return value;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.lock();
        this.quantity = quantity;
        this.unlock();
    }
    
    public void subQuantity(int quantity) {
        this.lock();
        this.quantity -= quantity;
        this.unlock();
    }
    
    public void lock() {
        lock.lock();
    }
    
    public void unlock() {
        lock.unlock();
    }

    @Override
    public String toString() {
        return "Entry:: ID => "+client.getID()+" Value => "+ value + ", Quantity=>" + quantity;
    }
}