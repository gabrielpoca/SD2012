package hello;

import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

public class Client {
	
	private Client() {}
	
	public static void main(String[] args) {
		try {
			String host = (args.length < 1) ? null : args[0];
			Registry registry;
			registry = LocateRegistry.getRegistry(host);
			Hello stub = (Hello) registry.lookup("Hello");
			String response = stub.sayHello();
			System.out.println("response: "+response);
		} catch (Exception e) {
			System.err.println("Client exception: "+e.toString());
			e.printStackTrace();
		}
	}
	
}
