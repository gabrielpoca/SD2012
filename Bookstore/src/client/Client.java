/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package client;

import bookstore.BookInterface;
import bookstore.Bookstore;
import bookstore.BookstoreInterface;
import bookstore.CartFactoryInterface;
import bookstore.CartInterface;
import java.lang.management.MonitorInfo;
import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.util.jar.Attributes;
import java.util.logging.Level;
import java.util.logging.Logger;
import tx.MonitorInterface;

/**
 *
 * @author gabriel
 */
public class Client {
    public static void main(String[] args) {
	try {
	    BookstoreInterface bookstore = (BookstoreInterface) Naming.lookup("//localhost/bookstore");
	    MonitorInterface monitor = (MonitorInterface) Naming.lookup("//localhost/monitor");
	    CartFactoryInterface carts = (CartFactoryInterface) Naming.lookup("//localhost/carts");
	    
	    CartInterface cart = carts.make();
	    
	    int xid = monitor.begin();
	    
	    BookInterface book =  bookstore.find(1);
	    
	    System.out.println(book.getStock(xid));
	} catch (Exception ex) {
	    ex.printStackTrace();
	}
    }
    
    private static void log(String s) {
	System.out.println("[Client] "+s);
    }
}
