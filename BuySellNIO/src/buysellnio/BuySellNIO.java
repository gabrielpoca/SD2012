package buysellnio;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
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


/**
 * Stores a socket and a value.
 * Used to as a database entry.
 *
 * @author gabrielpoca
 */
class Entry {

    Socket socket;
    int quantity;

    public Entry(Socket sc, int quantity) {
        this.socket = sc;
        this.quantity = quantity;
    }

    public Entry(Socket socket) {
        this.socket = socket;
    }

    public Socket getSocket() {
        return socket;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
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
     * Starts the server socket channel on a port and configures it.
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
                channel.write(ByteBuffer.wrap("Welcome to stock exchange! You're a buyer!\r\n".getBytes("US-ASCII")));
                buyersDatabase.put(channel, new Entry(socket));
                break;
            case SELL_PORT:
                sellersDatabase.put(channel, new Entry(socket));
                // write welcome message for sellers
                channel.write(ByteBuffer.wrap("Welcome to stock exchange You're a seller!\r\n".getBytes("US-ASCII")));
                sellersDatabase.put(channel, new Entry(socket));
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
            switch (socket.getLocalPort()) {
                case BUY_PORT:
                    buyer = buyersDatabase.get(channel);
                    quantity = buyer.getQuantity() + numReadBytes;
                    buyer.setQuantity(quantity);
                    log("Got: " + numReadBytes + " bytes from buyer, updated to "+quantity+".");
                    seller = findEntry(sellersDatabase);
                    break;
                case SELL_PORT:
                    seller = sellersDatabase.get(channel);
                    quantity = seller.getQuantity() + numReadBytes;
                    seller.setQuantity(quantity);
                    log("Got: " + numReadBytes + " bytes from seller, updated to "+quantity+".");
                    buyer = findEntry(buyersDatabase);
                    break;
            }

            if(buyer != null && seller != null) {
                quantity = updateEntrys(buyer, seller);
                addToBuffer(buyer.getSocket().getChannel(), new byte[quantity]);
                addToBuffer(seller.getSocket().getChannel(), new byte[quantity]);
                key.interestOps(SelectionKey.OP_WRITE);
            }

            // if some bytes are read
            //Entry entry = findEntry(sellersDatabase);
            //if (entry != null) {
            //	updateEntrys(entry, buyersDatabase.get(channel));
            //	validateEntry(entry);
            //  }
            //byte[] data = new byte[numRead];
            //System.arraycopy(buffer.array(), 0, data, 0, numRead);


            // write back to client
            //addToBuffer(key, data);
        }
    }

    /**
     * Performs the transaction between the buyer and the seller and
     * returns the quantity.
     * @param buyer
     * @param seller
     * @return The traded quantity.
     */
    private int updateEntrys(Entry buyer, Entry seller) {
        int quantity = buyer.getQuantity();
        if (seller.getQuantity() < quantity)
            quantity = seller.getQuantity();
        buyer.subQuantity(quantity);
        seller.subQuantity(quantity);
        return quantity;
    }

    /**
     * Selects a entry in the given database.
     * @param database Database to selec.
     * @return Selected entry.
     */
    private Entry findEntry(HashMap<SocketChannel, Entry> database) {
        Entry entry = null;
        if (!database.isEmpty()) {
            for(Entry e : database.values()) {
                entry = e;
                if(entry != null)
                    break;
            }
        }
        return entry;
    }

    private void addToBuffer(SocketChannel channel, byte[] data) {
        List<byte[]> pendingData = this.dataMap.get(channel);
        pendingData.add(data);
        //key.interestOps(SelectionKey.OP_WRITE);
    }


    public void log(String s) {
        System.out.println(s);
    }

    public static void main(String[] args) throws IOException {
        BuySellNIO server = new BuySellNIO(null, 9999);
        server.run();
    }

}
