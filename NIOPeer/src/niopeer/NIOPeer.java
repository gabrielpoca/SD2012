package niopeer;

import dumpit.IT;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Iterator;

public class NIOPeer extends Thread {

    /**
     * Main while condition.
     */
    private boolean run = true;
    /**
     * Port to listen to.
     */
    private int port;
    /**
     * Communication mutex.
     */
    private Mutex mutex;

    public NIOPeer(int port, Mutex mutex) {
	this.port = port;
	this.mutex = mutex;
    }

    public void run() {

	try {
	    Selector selector = Selector.open();
	    ServerSocketChannel server_channel = ServerSocketChannel.open();
	    server_channel.configureBlocking(false);
	    server_channel.socket().bind(new InetSocketAddress("localhost", this.port));
	    server_channel.register(selector, SelectionKey.OP_ACCEPT);	    
	    while (run) {
		selector.select();
		Iterator<SelectionKey> keys = selector.selectedKeys().iterator();
		while (keys.hasNext() && run) {
		    SelectionKey key = keys.next();
		    keys.remove();
		    if (!key.isValid()) {
			continue;
		    }
		    if (key.isAcceptable()) {
			IT.log("Accepting local connection...");			
			ServerSocketChannel key_server_channel = (ServerSocketChannel) key.channel();
			SocketChannel channel = key_server_channel.accept();
			channel.configureBlocking(false);
			SelectionKey nkey = channel.register(key.selector(), SelectionKey.OP_WRITE);
			Handler handler = new Handler(nkey, mutex);
			nkey.attach(handler);
			/**
			 * After accepting a new connection put it in wait for the lock.
			 */
			mutex.lock(handler);
		    } else if (key.isReadable()) {
			Handler handler = (Handler) key.attachment();
			handler.read(key);
		    } else if (key.isWritable()) {
			Handler handler = (Handler) key.attachment();
			handler.write(key);
		    }
		}
	    }
	} catch (IOException e) {
	    e.printStackTrace();
	}
    }

    public static void main(String[] args) {
	Mutex mutex = new Mutex(Integer.valueOf(args[0]), Integer.valueOf(args[1]), Boolean.valueOf(args[2]));
	Thread t_mutex = new Thread(mutex);
	t_mutex.start();
	NIOPeer peer = new NIOPeer(Integer.valueOf(args[3]), mutex);
	Thread t_peer = new Thread(peer);
	t_peer.start();
	try {
	    t_mutex.join();
	    t_peer.join();
	} catch (InterruptedException e) {
	    e.printStackTrace();
	}
    }
}
