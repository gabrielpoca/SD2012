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
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

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
    SelectionKey key;

    public Entry(Socket sc, int quantity, SelectionKey key) {
        this.user = new User(sc);
        this.quantity = quantity;
        this.key = key;
    }

    public Entry(Socket socket, SelectionKey key) {
        this.user = new User(socket);
        this.key = key;
    }

    public Entry(Socket socket) {
        this.user = new User(socket);
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

    public int subQuantity(int quantity) {
        this.quantity -= quantity;
        return this.quantity;
    }

    public int addQuantity(int quantity) {
        this.quantity += quantity;
        return this.quantity;
    }
}

public class BuySellNIO {

    private InetAddress addr;
    private int port;
    private Selector selector;
    private Map<SocketChannel, List<byte[]>> dataMap;
    private HashMap<SocketChannel, Entry> buyersDatabase;
    private HashMap<SocketChannel, Entry> sellersDatabase;
    private static final int BUY_PORT = 9999;
    private static final int SELL_PORT = 9998;

    public BuySellNIO(InetAddress addr, int port) {
        this.addr = addr;
        this.port = port;
        dataMap = new HashMap<SocketChannel, List<byte[]>>();
        buyersDatabase = new HashMap<SocketChannel, Entry>();
        sellersDatabase = new HashMap<SocketChannel, Entry>();
    }

    public void run() throws IOException {

        boolean run = true;

        selector = Selector.open();

        startServerSocket(BUY_PORT);
        startServerSocket(SELL_PORT);

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
                    accept(key);
                } else if (key.isReadable()) {
                    read(key);
                } else if (key.isWritable()) {
                    write(key);
                }
            }
        }
    }

    /**
     * Starts and configures the server socket channel on a port.
     *
     * @param port Port to listen too.
     */
    private void startServerSocket(int port) throws IOException {
        ServerSocketChannel serverChannel = ServerSocketChannel.open();
        serverChannel.configureBlocking(false);
        serverChannel.socket().bind(new InetSocketAddress(addr, port));
        serverChannel.register(selector, SelectionKey.OP_ACCEPT);
    }

    private void accept(SelectionKey key) throws IOException {
        ServerSocketChannel serverChannel = (ServerSocketChannel) key.channel();
        SocketChannel channel = serverChannel.accept();
        channel.configureBlocking(false);
        Socket socket = channel.socket();
        // register channel buffer
        dataMap.put(channel, new ArrayList<byte[]>());
        // register channel in database
        switch (socket.getLocalPort()) {
            case BUY_PORT:
                buyersDatabase.put(channel, new Entry(socket));
                // write welcome message for buyers
                channel.write(ByteBuffer.wrap("Welcome to stock exchange! You're a buyer!\r\nInsert your username: ".getBytes("US-ASCII")));
                break;
            case SELL_PORT:
                sellersDatabase.put(channel, new Entry(socket));
                // write welcome message for sellers
                channel.write(ByteBuffer.wrap("Welcome to stock exchange You're a seller!\r\nInsert your username ".getBytes("US-ASCII")));
                break;
        }
        // register next action
        channel.register(this.selector, SelectionKey.OP_READ);
    }

    private void write(SelectionKey key) throws IOException {
        SocketChannel channel = (SocketChannel) key.channel();
        List<byte[]> pendingData = this.dataMap.get(channel);
        Iterator<byte[]> items = pendingData.iterator();
        while (items.hasNext()) {
            byte[] item = items.next();
            items.remove();
            channel.write(ByteBuffer.wrap(item));
        }
        key.interestOps(SelectionKey.OP_READ);
    }

    private void read(SelectionKey key) throws IOException {
        // read the number of bytes
        SocketChannel channel = (SocketChannel) key.channel();
        ByteBuffer buffer = ByteBuffer.allocate(1000);
        int numReadBytes = -1;
        numReadBytes = channel.read(buffer);
        Socket socket = channel.socket();
        if (numReadBytes == -1) {
            // if no bytes are read close the connection
            this.dataMap.remove(channel);
            switch (socket.getLocalPort()) {
                case BUY_PORT:
                    buyersDatabase.remove(channel);
                    break;
                case SELL_PORT:
                    sellersDatabase.remove(channel);
                    break;
            }
            channel.close();
            key.cancel();
        } else {
            int quantity = 0;
            Entry seller = null;
            Entry buyer = null;
            buffer.flip();
            switch (socket.getLocalPort()) {
                /* update the quantity and find a match entry to perform transaction. */
                case BUY_PORT:
                    buyer = buyersDatabase.get(channel);
                    buyer.setKey(key);
                    // if username and password are not set read them
                    if (!setUsernameAndPassword(buyer, String.valueOf(buffer.asCharBuffer()))) {
                        buyer.setKey(key);
                        quantity = buyer.getQuantity() + numReadBytes;
                        buyer.setQuantity(quantity);
                        log("Got: " + numReadBytes + " bytes from buyer, updated to " + quantity + ".");
                        seller = findEntry(sellersDatabase);
                    } else {
                        requestUsernameOrPassword(buyer, channel);
                    }
                    break;
                case SELL_PORT:
                    seller = sellersDatabase.get(channel);
                    seller.setKey(key);
                    // if username and password are not set read them
                    if (!setUsernameAndPassword(seller, String.valueOf(buffer.asCharBuffer()))) {
                        seller.setKey(key);
                        quantity = seller.getQuantity() + numReadBytes;
                        seller.setQuantity(quantity);
                        log("Got: " + numReadBytes + " bytes from seller, updated to " + quantity + ".");
                        buyer = findEntry(buyersDatabase);
                    } else {
                        requestUsernameOrPassword(seller, channel);
                    }
                    break;
            }
            /* if there is a match for transaction perform it. */
            if (buyer != null && seller != null) {
                quantity = tradeEntrys(buyer, seller);
                /* if some quantity was traded output it to each channel. */
                if (quantity != 0) {
                    addToChannelBuffer(buyer.getSocket().getChannel(), ("" + quantity).getBytes());
                    addToChannelBuffer(seller.getSocket().getChannel(), ("" + quantity).getBytes());
                    seller.getKey().interestOps(SelectionKey.OP_WRITE);
                    buyer.getKey().interestOps(SelectionKey.OP_WRITE);
                }
            }
            // set next action
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
     * Performs the transaction between the buyer and the seller and returns the
     * traded quantity.
     *
     * @param buyer
     * @param seller
     * @return The traded quantity.
     */
    private int tradeEntrys(Entry buyer, Entry seller) {
        int quantity = buyer.getQuantity();
        if (seller.getQuantity() < quantity) {
            quantity = seller.getQuantity();
        }
        buyer.subQuantity(quantity);
        seller.subQuantity(quantity);
        return quantity;
    }

    /**
     * Selects a random entry in the given database.
     *
     * @param database Database to selec.
     * @return Selected entry.
     */
    private Entry findEntry(HashMap<SocketChannel, Entry> database) {
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
        BuySellNIO server = new BuySellNIO(null, 9999);
        server.run();
    }
}
