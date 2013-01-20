/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package bookstore;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.List;

import tx.TxObject;

/**
 * @author gabriel
 */
public class Cart extends TxObject implements CartInterface {

    private List<BookInterface> cart = new ArrayList<BookInterface>();

    public Cart() throws RemoteException {
        super();
        cart = new ArrayList<BookInterface>();
    }

    public void add(int xid, Book book) throws RemoteException {
    }

    public synchronized boolean buy(int xid) throws RemoteException {
        enter(xid);
        for (BookInterface b : cart) {
            if (b.getStock(xid) < 1) {
//                System.out.println(b.getStock(xid));
                return false;
            }
        }
        for (BookInterface b : cart) {
            b.buy(xid);
        }
        return true;
    }

    @Override
    public void add(BookInterface book, int xid) throws RemoteException {
        enter(xid);
        cart.add(book);
    }
}
