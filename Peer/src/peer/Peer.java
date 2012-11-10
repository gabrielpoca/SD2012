/*
 * A ideia é criar um anel de peers. Cada um deles deve ler de um e escrever
 * para outro. Os peers devem receber porta de leitura e escrita tal como
 * uma variável que indique se é o dono do testemunho ou não.
 * 
 * A ordem de establecer os server sockets é relevante. A solução é cada
 * peer deve começar por criar o server socket mas apenas criar o channel
 * quando receber o testemunho pela primeira vez.
 */
package peer;

import dumpit.IT;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import threadservers.Agent;
import threadservers.ThreadServer;


class PeerClient extends Agent {
    
    Mutex mutex;
    
    public PeerClient(Mutex mutex) {
	super();
	this.mutex = mutex;
    }
    
    public PeerClient(Agent agent, Mutex mutex) {
	super(agent.getSocket());
	this.mutex = mutex;
    }
    
    public void run() {
	try {
	    // open read/write strams
	    DataInputStream is = new DataInputStream(this.getSocket().getInputStream());
	    DataOutputStream os = new DataOutputStream(this.getSocket().getOutputStream());
	    boolean run = false;
	    // while client doesnt send exit.
	    while (!run) {
		try {
		    os.writeUTF("ENTER to lock mutex...");
		    mutex.lock();
		    is.readUTF();		    
		    os.writeUTF("ENTER to release mutex...");
		    mutex.unlock();
		    is.readUTF();
		} catch (EOFException e) {
		    IT.log("Client closed");
		    run = true;
		}
	    }
	} catch (IOException ex) {
	    Logger.getLogger(PeerClient.class.getName()).log(Level.SEVERE, null, ex);
	}
    }
    
    public PeerClient clone() {
	return new PeerClient(super.clone(), mutex);
    }
}

public class Peer {

    /**
     * Main thread.
     * @param args 
     * args[0] should be the read port. 
     * args[1] should be the write port. 
     * args[2] should be true or false telling if it is the owner or not.
     * args[3] should be the local port to list to clients.
     */
    public static void main(String[] args) {
	try {
	    Mutex mutex = new Mutex(Integer.valueOf(args[0]), Integer.valueOf(args[1]), args[2]);
	    Thread thread_mutex = new Thread(mutex);
	    thread_mutex.start();
	    ThreadServer server = new ThreadServer(Integer.valueOf(args[3]), new PeerClient(mutex));
	    Thread t = new Thread(server);
	    t.start();
	    System.out.println("Enter to end process...");
	    System.in.read();
	    server.end();
	    mutex.end();
	    t.join();
	    thread_mutex.join();
	} catch (InterruptedException ex) {
	    Logger.getLogger(Peer.class.getName()).log(Level.SEVERE, null, ex);
	} catch (IOException ex) {
	    Logger.getLogger(Peer.class.getName()).log(Level.SEVERE, null, ex);
	}    
    }

}
