package nioservers;

import com.sun.corba.se.pept.transport.Selector;

public class NIOServer extends Thread {
	
	private boolean run = true;

	private Selector selector;
	
	public void run() {

	}

	public void end() {
		run = false;
	}
}
