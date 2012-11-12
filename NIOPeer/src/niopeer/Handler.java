package niopeer;

import dumpit.IT;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;
import java.util.AbstractList;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class Handler {

    List<byte[]> buffer;
    /**
     * Stores the selection key in order to make it readable after having lock.
     */
    SelectionKey key;
    /**
     * State of the client: 1 - Waiting for lock, 2 - Lock, 3 - Releasing lock.
     */
    private static final int LOCKED = 2;
    private int state = 1;
    private Mutex mutex;

    public Handler(SelectionKey key, Mutex mutex) {
	this.key = key;
	this.mutex = mutex;
	this.buffer = new ArrayList<byte[]>();
    }

    /**
     * Gives the lock the client. Having the lock it changes the state and make
     * is interest operations either read or write.
     */
    public void lock() {
	IT.log("[Handler] having mutex..");
	this.addToBuffer("You got the lock! Close the connection to release...".getBytes());
	state = LOCKED;
	key.interestOps(SelectionKey.OP_WRITE);
//	key.interestOps(SelectionKey.OP_READ);
    }

    public void read(SelectionKey key) {
	IT.log("[Handler] reading...");
	try {
	    SocketChannel channel = (SocketChannel) key.channel();
	    ByteBuffer buffer = ByteBuffer.allocate(100);
	    int read_bytes_count = -1;
	    read_bytes_count = channel.read(buffer);
	    /*
	     * If it read no bytes and the state is locked it means the
	     * connection was closed and it should remove the key and release
	     * the lock.
	     */
	    if (read_bytes_count == -1 && state == LOCKED) {
		channel.close();
		key.cancel();
		mutex.unlock();
	    }
	} catch (Exception e) {
	    e.printStackTrace();
	}
    }

    public void write(SelectionKey key) {
	try {
	    SocketChannel channel = (SocketChannel) key.channel();
	    Iterator<byte[]> items = buffer.iterator();
	    while (items.hasNext()) {
		byte[] item = items.next();
		items.remove();
		channel.write(ByteBuffer.wrap(item));

	    }
	    key.interestOps(SelectionKey.OP_READ);
	} catch (Exception e) {
	    e.printStackTrace();
	}
    }

    /**
     * Stores data that will later be sent to the client when key.isWritable().
     *
     * @param data Data to be stored.
     */
    private void addToBuffer(byte[] data) {
	buffer.add(data);
    }
}