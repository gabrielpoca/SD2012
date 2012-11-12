import java.io.DataOutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.util.Iterator;

import dumpit.IT;


public class Mutex extends Thread {
	
	private boolean temp_owner;
	
	// main while condition
	private boolean run = true;
	// server read port
	private int read_port;
	// server write port
	private int write_port;
	
	/**
	 * Mutex uses lock to find if there is a someone waiting
	 * for the mutex.
	 */
	private Lock lock = new Lock();
	
	public Mutex(int read_port, int write_port, boolean owner) {
		this.write_port = write_port;
		this.read_port = read_port;
		temp_owner = owner;
	}
	
	public void run() {
		try { 
			// start read server
			Selector selector = Selector.open();
			ServerSocketChannel server_channel = ServerSocketChannel.open();
			server_channel.configureBlocking(false);
			server_channel.socket().bind(new InetSocketAddress("localhost", read_port));
			server_channel.register(selector, SelectionKey.OP_ACCEPT);
			/*
			 * In order to not having to make many changes in the code it starts by sending
			 * the mutex if it has it.
			 */
			if(temp_owner)
				sendMutex();
			while(run) {
				selector.select();
				Iterator<SelectionKey> keys = selector.selectedKeys().iterator();
				while(keys.hasNext() && run) {
					SelectionKey key = (SelectionKey) keys.next();
					keys.remove();
					if (!key.isValid())
						continue;
					/*
					 * If someone connects it gave the mutex. 
					 * Having waiters the server keeps the mutex.
					 * Not having waiters sends the mutex to the next peer.
					 */
					if(!key.isConnectable()) {
						IT.log("Mutex received...");
						if(lock.hasWaiters()) {
							lock.giveMutexToHandler();
						} else {
							sendMutex();
						}
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public void lock(Handler h) {
		lock.handlerWaitMutex(h);
	}
	
	public void unlock() {
		lock.handlerReturnMutex();
	}
	
	/**
	 * Used to send to mutex to the next peer in the ring. It
	 * accomplishes it by establishing a connection to the write_port.
	 */
	private void sendMutex() {
		try {
			IT.log("Sending mutex...");
			Socket socket = new Socket("localhost", write_port);
			DataOutputStream out_stream = new DataOutputStream(socket.getOutputStream());
			out_stream.close();
			socket.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Turns the main while condition false.
	 */
	public void end() {
		run = false;
	}


}
