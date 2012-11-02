package buysellnio;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.sound.midi.SysexMessage;

class ValidationException extends Exception {

    public ValidationException(String msg) {
	super(msg);
	System.err.println(msg);
    }
}

class User {

    String username;
    String password;
    Socket socket;

    public User() {
    }

    public User(Socket socket) {
	this.socket = socket;
    }

    public void setUsername(String username) {
	this.username = username;
    }

    public void setPassword(String password) {
	this.password = password;
    }

    public Socket getSocket() {
	return socket;
    }

    public String getUsername() {
	return username;
    }

    public String getPassword() {
	return password;
    }
}

/**
 * Stores a socket and a value. Used to as a database entry.
 *
 * @author gabrielpoca
 */
class Entry {

    User user;
    int quantity;
    int transacted_quantity;
    SelectionKey key;

    public Entry(Socket sc, int quantity, SelectionKey key) {
	this.user = new User(sc);
	this.quantity = quantity;
	this.key = key;
	transacted_quantity = 0;
    }

    public Entry(Socket socket, SelectionKey key) {
	this.user = new User(socket);
	this.key = key;
	transacted_quantity = 0;
    }

    public Entry(Socket socket) {
	this.user = new User(socket);
	transacted_quantity = 0;
    }

    public Socket getSocket() {
	return user.getSocket();
    }

    public User getUser() {
	return user;
    }

    public int getQuantity() {
	return quantity;
    }

    public SelectionKey getKey() {
	return key;
    }

    public void setQuantity(int quantity) {
	this.quantity = quantity;
    }

    public void setKey(SelectionKey key) {
	this.key = key;
    }

    public void addTransactedQuantity(int quantity) throws ValidationException {
	this.transacted_quantity += quantity;
	if (this.quantity < transacted_quantity) {
	    throw new ValidationException("Entry exception! Transacted quantity bigger that avaliable quantity!");
	}
    }

    public int addQuantity(int quantity) {
	this.quantity += quantity;
	return this.quantity;
    }

    public int remainingQuantity() {
	return quantity - transacted_quantity;
    }
}

public class BuySellNIO {

    static interface Handler {

	void accept(SelectionKey key);

	void read(SelectionKey key);

	void write(SelectionKey key);
    }
    private InetAddress addr;
    private Map<SocketChannel, List<byte[]>> dataMap;
    private HashMap<SocketChannel, Entry> buyersDatabase;
    private HashMap<SocketChannel, Entry> sellersDatabase;
    private int sum_want_to_buy, sum_bought, sum_want_to_sell, sum_sold;
    private static final int BUY_PORT = 9999;
    private static final int SELL_PORT = 9998;

    public BuySellNIO() {
	dataMap = new HashMap<SocketChannel, List<byte[]>>();
	buyersDatabase = new HashMap<SocketChannel, Entry>();
	sellersDatabase = new HashMap<SocketChannel, Entry>();
	sum_bought = sum_sold = sum_want_to_buy = sum_want_to_sell = 0;
    }

    class BuyerAccept implements Handler {

	public void accept(SelectionKey key) {
	    try {
		// handle new connection
		ServerSocketChannel serverChannel = (ServerSocketChannel) key.channel();
		SocketChannel channel = serverChannel.accept();
		channel.configureBlocking(false);
		Socket socket = channel.socket();
		// register channel buffer
		dataMap.put(channel, new ArrayList<byte[]>());
		buyersDatabase.put(channel, new Entry(socket));
		// write welcome message for buyers
		addToChannelBuffer(channel, "Welcome to stock exchange! You're a buyer!".getBytes());
		addToChannelBuffer(channel, "Insert your username: ".getBytes());
		// register new key to write
		SelectionKey nkey = channel.register(key.selector(), SelectionKey.OP_WRITE);
		Buyer buyer = new Buyer(nkey);
		nkey.attach(buyer);
	    } catch (IOException ex) {
		Logger.getLogger(BuySellNIO.class.getName()).log(Level.SEVERE, null, ex);
	    }
	}

	public void read(SelectionKey key) {
	    throw new UnsupportedOperationException("Not supported yet.");
	}

	public void write(SelectionKey key) {
	    throw new UnsupportedOperationException("Not supported yet.");
	}
    }

    class Buyer implements Handler {

	private SelectionKey key;

	public Buyer(SelectionKey key) {
	    this.key = key;
	}

	public void accept(SelectionKey key) {
	    throw new UnsupportedOperationException("Not supported yet.");
	}

	public void read(SelectionKey key) {
	    try {
		// read the number of bytes
		SocketChannel channel = (SocketChannel) key.channel();
		ByteBuffer buffer = ByteBuffer.allocate(1000);
		int numReadBytes = -1;
		numReadBytes = channel.read(buffer);
		if (numReadBytes == -1) {
		    // if no bytes are read close the connection
		    buyersDatabase.remove(channel);
		    channel.close();
		    key.cancel();
		} else {
		    Entry seller = null;
		    Entry buyer = null;
		    buffer.flip();
		    buyer = buyersDatabase.get(channel);
		    buyer.setKey(key);
		    // if username and password are not set read them
		    if (!setUsernameAndPassword(buyer, String.valueOf(buffer.asCharBuffer()))) {
			buyer.setKey(key);
			buyer.addQuantity(numReadBytes);
			sum_want_to_buy += numReadBytes;
			log("Got: " + numReadBytes + " bytes from buyer, updated to " + buyer.remainingQuantity() + ".");
			seller = findEntryForTransaction(sellersDatabase);
		    } else {
			requestUsernameOrPassword(buyer, channel);
		    }
		    /* if there is a match for transaction perform it. */
		    performTransaction(buyer, seller);
		}
	    } catch (IOException ex) {
		Logger.getLogger(BuySellNIO.class.getName()).log(Level.SEVERE, null, ex);
	    } catch (ValidationException ex) {
		System.err.println("Validation Error!");
	    }
	}

	public void write(SelectionKey key) {
	    try {
		SocketChannel channel = (SocketChannel) key.channel();
		List<byte[]> pendingData = dataMap.get(channel);
		Iterator<byte[]> items = pendingData.iterator();
		while (items.hasNext()) {

		    byte[] item = items.next();
		    items.remove();
		    channel.write(ByteBuffer.wrap(item));

		}
		key.interestOps(SelectionKey.OP_READ);
	    } catch (IOException ex) {
		Logger.getLogger(BuySellNIO.class.getName()).log(Level.SEVERE, null, ex);
	    }
	}
    }

    class SellerAccept implements Handler {

	public void accept(SelectionKey key) {
	    try {
		ServerSocketChannel serverChannel = (ServerSocketChannel) key.channel();
		SocketChannel channel = serverChannel.accept();
		channel.configureBlocking(false);
		Socket socket = channel.socket();
		// register channel buffer
		dataMap.put(channel, new ArrayList<byte[]>());
		// register channel in database
		sellersDatabase.put(channel, new Entry(socket));
		// write welcome message for sellers
		addToChannelBuffer(channel, "Welcome to stock exchange! You're a seller!".getBytes());
		addToChannelBuffer(channel, "Insert your username: ".getBytes());
		// register new key to write
		SelectionKey nkey = channel.register(key.selector(), SelectionKey.OP_WRITE);
		Seller seller = new Seller(nkey);
		nkey.attach(seller);
	    } catch (IOException ex) {
		Logger.getLogger(BuySellNIO.class.getName()).log(Level.SEVERE, null, ex);
	    }
	}

	public void read(SelectionKey key) {
	    throw new UnsupportedOperationException("Not supported yet.");
	}

	public void write(SelectionKey key) {
	    throw new UnsupportedOperationException("Not supported yet.");
	}
    }

    class Seller implements Handler {

	private SelectionKey key;

	public Seller(SelectionKey key) {
	    this.key = key;
	}

	public void accept(SelectionKey key) {
	    throw new UnsupportedOperationException("Not supported yet.");
	}

	public void read(SelectionKey key) {
	    try {
		// read the number of bytes
		SocketChannel channel = (SocketChannel) key.channel();
		ByteBuffer buffer = ByteBuffer.allocate(1000);
		int numReadBytes = -1;
		numReadBytes = channel.read(buffer);
		Socket socket = channel.socket();
		if (numReadBytes == -1) {
		    // if no bytes are read close the connection
		    dataMap.remove(channel);
		    sellersDatabase.remove(channel);
		    channel.close();
		    key.cancel();
		} else {
		    Entry seller = null;
		    Entry buyer = null;
		    buffer.flip();
		    seller = sellersDatabase.get(channel);
		    seller.setKey(key);
		    // if username and password are not set read them
		    if (!setUsernameAndPassword(seller, String.valueOf(buffer.asCharBuffer()))) {
			seller.setKey(key);
			seller.addQuantity(numReadBytes);
			sum_want_to_sell += numReadBytes;
			log("Got: " + numReadBytes + " bytes from seller, updated to " + seller.remainingQuantity() + ".");
			buyer = findEntryForTransaction(buyersDatabase);
		    } else {
			requestUsernameOrPassword(seller, channel);
		    }
		    /* if there is a match for transaction perform it. */
		    performTransaction(buyer, seller);
		}
	    } catch (IOException ex) {
		Logger.getLogger(BuySellNIO.class.getName()).log(Level.SEVERE, null, ex);
	    } catch (ValidationException ex) {
//		System.err.append("Validation Error!");
	    }
	}

	public void write(SelectionKey key) {
	    try {
		SocketChannel channel = (SocketChannel) key.channel();
		List<byte[]> pendingData = dataMap.get(channel);
		Iterator<byte[]> items = pendingData.iterator();
		while (items.hasNext()) {

		    byte[] item = items.next();
		    items.remove();
		    channel.write(ByteBuffer.wrap(item));

		}
		key.interestOps(SelectionKey.OP_READ);
	    } catch (IOException ex) {
		Logger.getLogger(BuySellNIO.class.getName()).log(Level.SEVERE, null, ex);
	    }
	}
    }

    public void run() throws IOException {

	boolean run = true;

	Selector selector = Selector.open();

	{
	    ServerSocketChannel serverChannel = ServerSocketChannel.open();
	    serverChannel.configureBlocking(false);
	    serverChannel.socket().bind(new InetSocketAddress(addr, SELL_PORT));
	    serverChannel.register(selector, SelectionKey.OP_ACCEPT, new SellerAccept());
	}
	{
	    ServerSocketChannel serverChannel = ServerSocketChannel.open();
	    serverChannel.configureBlocking(false);
	    serverChannel.socket().bind(new InetSocketAddress(addr, BUY_PORT));
	    serverChannel.register(selector, SelectionKey.OP_ACCEPT, new BuyerAccept());
	}

	while (run) {
	    selector.select();
	    Iterator keys = selector.selectedKeys().iterator();
	    while (keys.hasNext()) {
		SelectionKey key = (SelectionKey) keys.next();
		keys.remove();
		if (!key.isValid()) {
		    continue;
		}
		if (key.isAcceptable()) {
		    Handler handler = (Handler) key.attachment();
		    handler.accept(key);
		} else if (key.isReadable()) {
		    Handler handler = (Handler) key.attachment();
		    handler.read(key);
		} else if (key.isWritable()) {
		    Handler handler = (Handler) key.attachment();
		    handler.write(key);
		}
	    }
	}
    }

    /**
     * Define username or password if not defined.
     *
     * @param entry
     * @param s
     * @return Return true if something changes and false if not.
     */
    private boolean setUsernameAndPassword(Entry entry, String s) {
	if (entry.getUser().getUsername() == null) {
	    entry.getUser().setUsername(s);
	    return true;
	} else if (entry.getUser().getPassword() == null) {
	    entry.getUser().setPassword(s);
	    return true;
	} else {
	    return false;
	}
    }

    /**
     * Write to user username or password request message and set the selection
     * key to write.
     *
     * @param entry
     * @param channel
     * @throws IOException
     */
    private void requestUsernameOrPassword(Entry entry, SocketChannel channel) throws IOException {
	if (entry.getUser().getUsername() == null) {
	    addToChannelBuffer(channel, "Insert your username: ".getBytes());
	    entry.getKey().interestOps(SelectionKey.OP_WRITE);
	} else if (entry.getUser().getPassword() == null) {
	    addToChannelBuffer(channel, "Insert your password: ".getBytes());
	    entry.getKey().interestOps(SelectionKey.OP_WRITE);
	}
    }

    /**
     * Perform the quantity transaction between two entrys. First validates if
     * neither is null. If not calls exchangeQuantity on both. Then puts the
     * quantity each channel buffer and makes each key interestOPS to write.
     *
     * @param buyer
     * @param seller
     */
    private void performTransaction(Entry buyer, Entry seller) throws ValidationException {
	int quantity = 0;
	if (buyer != null && seller != null) {
	    quantity = exchangeQuantity(buyer, seller);
	    sum_bought += quantity;
	    sum_sold += quantity;
	    log("SOLD "+sum_sold+" BOUGHT "+sum_bought+" WSELL "+sum_want_to_sell+" WBUY "+sum_want_to_buy);
	    if(sum_sold > Math.min(sum_want_to_sell, sum_want_to_buy) || sum_bought > Math.max(sum_want_to_buy, sum_want_to_sell)) {
		throw new ValidationException("Sum validation error! Sould or bought more that max avaliable!");
	    }
	    /* if some quantity was traded output it to each channel. */
	    if (quantity != 0) {
		addToChannelBuffer(buyer.getSocket().getChannel(), ("Bought: " + quantity + " Remaining: " + buyer.remainingQuantity()).getBytes());
		addToChannelBuffer(seller.getSocket().getChannel(), ("Sold " + quantity + " Remaining: " + seller.remainingQuantity()).getBytes());
		seller.getKey().interestOps(SelectionKey.OP_WRITE);
		buyer.getKey().interestOps(SelectionKey.OP_WRITE);
	    }
	}
    }

    /**
     * Performs the quantity transaction between the buyer and the seller.
     *
     * @param buyer
     * @param seller
     * @return Returns the traded quantity.
     */
    private int exchangeQuantity(Entry buyer, Entry seller) throws ValidationException {
	int quantity = buyer.remainingQuantity();
	if (seller.remainingQuantity() < quantity) {
	    quantity = seller.remainingQuantity();
	}
	buyer.addTransactedQuantity(quantity);
	seller.addTransactedQuantity(quantity);
	return quantity;
    }

    /**
     * Selects a random entry in the given database.
     *
     * @param database Database to selec.
     * @return Selected entry.
     */
    private Entry findEntryForTransaction(HashMap<SocketChannel, Entry> database) {
	Entry entry = null;
	if (!database.isEmpty()) {
	    for (Entry e : database.values()) {
		entry = e;
		if (entry != null) {
		    break;
		}
	    }
	}
	return entry;
    }

    /**
     * Add data to the channel buffer to be sent later.
     *
     * @param channel
     * @param data
     */
    private void addToChannelBuffer(SocketChannel channel, byte[] data) {
	List<byte[]> pendingData = this.dataMap.get(channel);
	pendingData.add(data);
    }

    public void log(String s) {
	System.out.println(s);
    }

    public static void main(String[] args) throws IOException {
	BuySellNIO server = new BuySellNIO();
	server.run();
    }
}
