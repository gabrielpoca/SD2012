package cliproto;

import java.io.*;
import java.net.*;

public class CliProto {

    public static void main(String args[])
            throws IOException, UnknownHostException {
        Socket cs = new Socket("127.0.0.1", 9999);

        DataInputStream is = new DataInputStream(cs.getInputStream());
        DataOutputStream os = new DataOutputStream(cs.getOutputStream());

        String current;
        BufferedReader sin = new BufferedReader(new InputStreamReader(System.in));

        current = sin.readLine();
        while (!current.equals("quit")) {
            String[] parse = current.split(" ");
            os.writeUTF(parse[0]);
            os.writeInt(Integer.parseInt(parse[1]));
            current = sin.readLine();
        }
        os.writeUTF(current);

        is.close();
        os.close();
        sin.close();
        cs.close();
    }
}
