/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package peer;

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
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;
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
        init();
        while (run) {
            try {
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
                                log("Mutex aquired...");
                                lock.setOwnerTrue();
                            } else {
                                log("Mutex ignored...");
                                release();
                            }
                        }
                    }
                }
            } catch (IOException ex) {
                Logger.getLogger(Mutex.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    /**
     * Initialize the ServerSocketChannel in the READ_PORT port.
     */
    private void init() {
        try {
            selector = Selector.open();
            ServerSocketChannel serverChannel = ServerSocketChannel.open();
            serverChannel.configureBlocking(false);
            serverChannel.socket().bind(new InetSocketAddress("localhost", READ_PORT));
            serverChannel.register(selector, SelectionKey.OP_ACCEPT);
        } catch (IOException ex) {
            Logger.getLogger(Mutex.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void lock() {
        try {
            log("Trying to aquire lock...");
            lock.lock();
            log("Lock aquired!");
        } catch (InterruptedException ex) {
            Logger.getLogger(Mutex.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

    public void unlock() {
        log("Releasing lock...");
        // set to unlock
        lock.setOwnerFalse();
        release();
    }

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

    private void log(String s) {
        System.out.println("[Mutex] " + s);
    }
}
