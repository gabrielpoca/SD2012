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


public class StockExchangeClient {

    public static void main(String args[]) throws IOException, UnknownHostException {
	Socket cs = new Socket("localhost", 9999);

	DataInputStream is = new DataInputStream(cs.getInputStream());
	DataOutputStream os = new DataOutputStream(cs.getOutputStream());

	String current;
	BufferedReader sin = new BufferedReader(new InputStreamReader(System.in));

	os.writeUTF(sin.readLine());
	System.out.println(is.readInt());
	current = sin.readLine();
	os.writeUTF(current);

	is.close();
	os.close();
	sin.close();
	cs.close();
    }
}
