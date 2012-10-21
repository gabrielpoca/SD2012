package servproto;

import java.io.*;
import java.net.*;

public class ServProto {

    public static void main(String args[]) throws IOException {
	ServerSocket ss = new ServerSocket(9999);

	Socket cs = ss.accept();

	DataInputStream is = new DataInputStream(cs.getInputStream());
	DataOutputStream os = new DataOutputStream(cs.getOutputStream());

	String cmd = is.readUTF();

	int val = 0;
	int in_val = 0;
	while (!cmd.equals("quit")) {
	    try {
		in_val = is.readInt();
		if (cmd.equals("sub")) {
		    in_val = -in_val;
		}
		val += in_val;
		System.out.println(val);
		cmd = is.readUTF();
	    } catch (Exception e) {
		break;
	    }
	}
	is.close();
	os.close();
	cs.close();
	ss.close();
    }
}
