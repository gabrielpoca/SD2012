/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package stockexchangeclient;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.logging.Level;
import java.util.logging.Logger;

class Reader extends Thread {
    
    Socket socket;
    private boolean run;
    
    public Reader(Socket socket) {
        this.socket = socket;
        run = true;
    }
        
    public void run() {
        try {
            log("Strating reader...");
            DataInputStream input_stream = new DataInputStream(socket.getInputStream());
            String message = "";
            while(run) {
                message = input_stream.readUTF();
                log(message);
            }
            log("Closing reader...");
            input_stream.close();
        } catch (IOException ex) {
            Logger.getLogger(Reader.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    public void end() {
        run = false;
    }
    
    private void log(String s) {
        System.out.println("[Reader] "+s);
    }
}

public class StockExchangeClient {

    public static void main(String args[]) throws IOException, UnknownHostException, InterruptedException {
	Socket socket = new Socket("localhost", 9999);
        
        // start reader
        Reader reader = new Reader(socket);
        Thread thread_reader = new Thread(reader);
        thread_reader.start();
        // start writter
        boolean run = true;
	DataOutputStream output_stream = new DataOutputStream(socket.getOutputStream());
	String current;
        BufferedReader sin = new BufferedReader(new InputStreamReader(System.in));        
        while(run) {
            current = sin.readLine();
            if(current.equals("exit")) {
                run = false;
            } else if (current.equals("help")) {
                System.out.println("\tCommands:\n\tbuy value quantity\n\tsell value quantity\n\tset id\n");
                continue;
            }
            output_stream.writeUTF(current);
        }
        // stop reader
        reader.end();
        thread_reader.join();
        // close sockets and streams
	output_stream.close();
	sin.close();
	socket.close();
    }
}
