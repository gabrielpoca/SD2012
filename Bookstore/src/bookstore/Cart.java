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
 *
 * @author gabriel
 */
public class Cart extends TxObject{
    
    private List<Book> cart = new ArrayList<Book>();
    
    public Cart() throws RemoteException {
	super();
	cart = new ArrayList<Book>();
    }
    
    public void add(int xid, Book book) throws RemoteException {
	enter(xid);
	cart.add(book);
    }
    
    public synchronized boolean buy(int xid) throws RemoteException {
	enter(xid);
	for(Book b : cart) {
	    if(b.getStock(xid) < 1)
		return false;
	}
	for(Book b : cart)
	    b.buy(xid);
	return true;
    }
    
    public static void main(String[] args) {
	
    }
    
}
