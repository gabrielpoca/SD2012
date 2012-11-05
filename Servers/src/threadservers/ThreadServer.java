package threadservers;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ThreadServer extends Thread {

    /* Port it listens to. */
    int read_port;
    Agent agent;
    boolean run = true;

    public ThreadServer(int read_port, Agent agent) {
        this.read_port = read_port;
	this.agent = agent;
    }

    public void run() {
	try {
	    log("Starting on port " + read_port);
	    ServerSocket serverSocket = new ServerSocket(read_port);
	    Socket socket = null;
	    long id = 1;
	    while (run) {
		socket = serverSocket.accept();
		log("Connection accepted...");
		Agent new_agent = (Agent) agent.clone();
		new_agent.setSocket(socket);
		Thread t = new Thread(new_agent);
		t.start();
		id++;
	    }
	} catch (IOException ex) {
	    Logger.getLogger(ThreadServer.class.getName()).log(Level.SEVERE, null, ex);
	}
    }
    
    public void end() {
	run = false;
    }
    
    public void log(String s) {
        System.out.println("[ThreadServer] "+s);
    }
}
