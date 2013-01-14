/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package bookstore;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.sql.ResultSet;
import org.apache.derby.jdbc.EmbeddedXADataSource;
import tx.TxObject;

import static bookstore.Bookstore.DATABASE_NAME;
import java.sql.Connection;
import java.sql.ResultSetMetaData;
import java.sql.Statement;
import javax.sql.XAConnection;
import javax.transaction.xa.XAResource;
import org.apache.derby.jdbc.EmbeddedDataSource;
import tx.XidImpl;

/**
 *
 * @author gabriel
 */
public class Book extends TxObject implements BookInterface {
    
    private int isbn;
    
    public Book(int isbn) throws RemoteException {
	super();
        this.isbn = isbn;
    }
    
    public int getISBN() throws RemoteException {
        return isbn;
    }
    
    public String getTitle() throws RemoteException {
	return "";
    }
    
    public int getStock(int xid) throws RemoteException {
	enter(xid);
	int stock = 0;
	try {
	    EmbeddedXADataSource data_source = new EmbeddedXADataSource();
	    data_source.setDatabaseName(DATABASE_NAME);
	    
	    XAConnection xaconnection = data_source.getXAConnection();
	    XAResource resource = xaconnection.getXAResource();
	    resource.start(new XidImpl(xid), XAResource.TMNOFLAGS);
	    
	    Connection connection = xaconnection.getConnection();
	    Statement statement = connection.createStatement();
	    
	    ResultSet result = statement.executeQuery("select * from books where isbn = "+isbn);
	    result.next();
	    stock = result.getInt(1);
	    
	    statement.close();
	    connection.close();
	    
	    resource.end(new XidImpl(xid), XAResource.TMSUCCESS);
	    
	} catch (Exception e) {
	    e.printStackTrace();
	}
	return stock;
    }
    
    public void buy(int xid) throws RemoteException {
	enter(xid);
	try {
	    EmbeddedDataSource data_source = new EmbeddedDataSource();
	    data_source.setDatabaseName(DATABASE_NAME);
	    Connection connection = data_source.getConnection();
	    Statement statement = connection.createStatement();
	    
	    statement.executeQuery("update books set sotck = stock - 1 where isbn = "+isbn);
	    statement.close();
	    connection.close();
	    
	} catch (Exception e) {
	    e.printStackTrace();
	}
    }
}
