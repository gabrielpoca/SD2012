/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package bookstore;

import java.rmi.Naming;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;

/**
 *
 * @author gabriel
 */
public class CartFactory extends UnicastRemoteObject implements CartFactoryInterface{

    public CartFactory() throws RemoteException {
	super();
    }
    
    public CartInterface make() throws RemoteException {
	return (CartInterface) new Cart();
    }
    
    public static void main(String[] args) {
	try {
	    Naming.rebind("//localhost/cart", new CartFactory());
	} catch (Exception e) {
	    e.printStackTrace();
	}
    }
    
}
