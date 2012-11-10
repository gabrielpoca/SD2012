/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package peer;

import dumpit.IT;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Mutex extends Thread {

    private int READ_PORT;
    private int WRITE_PORT;
    private Selector selector;
    private boolean run;
    Lock lock;

    public Mutex(int read_port, int write_port, String owner) {
	READ_PORT = read_port;
	WRITE_PORT = write_port;
	run = true;
	if (owner.equals(("true"))) {
	    lock = new Lock(true);
	} else {
	    lock = new Lock(false);
	}
    }

    /*
     * CONSIDERAR USAR A CHAVE OP_CONNECT e key.isConnectable()
     * 
     * selector.wakeup() existes.
     */
    public void run() {
	try {
	    selector = Selector.open();
	    ServerSocketChannel serverChannel = ServerSocketChannel.open();
	    serverChannel.configureBlocking(false);
	    serverChannel.socket().bind(new InetSocketAddress("localhost", READ_PORT));
	    serverChannel.register(selector, SelectionKey.OP_ACCEPT);
	    while (run) {
		selector.select();
		Iterator keys = selector.selectedKeys().iterator();
		while (keys.hasNext() && run) {
		    SelectionKey key = (SelectionKey) keys.next();
		    keys.remove();
		    if (!key.isValid()) {
			continue;
		    }
		    if (key.isAcceptable()) {
			ServerSocketChannel server_channel = (ServerSocketChannel) key.channel();
			SocketChannel channel = server_channel.accept();
			channel.configureBlocking(false);
			channel.register(selector, SelectionKey.OP_READ);
		    } else if (key.isReadable()) {
			SocketChannel channel = (SocketChannel) key.channel();
			ByteBuffer buffer = ByteBuffer.allocate(1000);
			int numReadBytes = -1;
			numReadBytes = channel.read(buffer);
			if (numReadBytes != -1) {
			    if (lock.testOwner()) {
				IT.log("Mutex aquired...");
				lock.setOwnerTrue();
			    } else {
				IT.log("Mutex ignored...");
				release();
			    }
			}
		    }
		}
	    }
	} catch (IOException ex) {
	    Logger.getLogger(Mutex.class.getName()).log(Level.SEVERE, null, ex);
	}
    }

    public void lock() {
	try {
	    IT.log("Trying to aquire lock...");
	    lock.lock();
	    IT.log("Lock aquired!");
	} catch (InterruptedException ex) {
	    Logger.getLogger(Mutex.class.getName()).log(Level.SEVERE, null, ex);
	}

    }

    public void unlock() {
	IT.log("Releasing lock...");
	// set to unlock
	lock.setOwnerFalse();
	release();
    }

    /**
     * Release mutex to the next peer in the ring. Starts a connection to the
     * WRITE_PORT, writes something and closes the connection.
     */
    private void release() {
	try {
	    // pass to other mutex
	    Socket socket = new Socket("127.0.0.1", WRITE_PORT);
	    DataOutputStream output_stream = new DataOutputStream(socket.getOutputStream());
	    output_stream.writeUTF("");
	    output_stream.close();
	    socket.close();
	} catch (UnknownHostException ex) {
	    Logger.getLogger(Mutex.class.getName()).log(Level.SEVERE, null, ex);
	} catch (IOException ex) {
	    Logger.getLogger(Mutex.class.getName()).log(Level.SEVERE, null, ex);
	}
    }

    public void end() {
	run = false;
    }
}
