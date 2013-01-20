/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package bookstore;

import java.rmi.Naming;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.server.UnicastRemoteObject;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import org.apache.derby.jdbc.EmbeddedDataSource;

/**
 *
 * @author gabriel
 */
public class Bookstore extends UnicastRemoteObject implements BookstoreInterface {

    public static final String DATABASE_NAME = "bookstore";

    public Bookstore() throws RemoteException {
    }

    public BookInterface find(int isbn) throws RemoteException {
	return new Book(isbn);
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
	try {
	    EmbeddedDataSource data_source = new EmbeddedDataSource();
	    data_source.setDatabaseName(DATABASE_NAME);
	    // create if doesn't exist
	    data_source.setCreateDatabase("create");

	    Connection connection = data_source.getConnection();
	    Statement statement = connection.createStatement();

	    try {
		statement.executeUpdate("DROP TABLE "+DATABASE_NAME);
	    } catch (SQLException e) {
		System.out.println("Table doesn't exist!");
	    }
	    statement.executeUpdate("CREATE TABLE "+DATABASE_NAME+" (isbn INT, title VARCHAR(150), stock INT)");
	    statement.executeUpdate("insert into "+DATABASE_NAME+" values (1, 'Primeiro Livro', 3), (2, 'Segundo Livro', 2), (3, 'Terceiro Livro', 1)");

	    statement.close();
	    connection.close();

	    Naming.rebind("//localhost/bookstore", new Bookstore());

	    System.out.println("Bookstore ready...");
	} catch (Exception e) {
	    e.printStackTrace();
	}
    }
}
