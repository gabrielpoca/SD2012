/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package stockexchange;

import java.net.Socket;

/**
 *
 * @author gabrielpoca
 */
public class ClientEntry {
    
    private long id;
    private Socket socket;
    
    public ClientEntry(long id, Socket socket) {
        this.id = id;
        this.socket = socket;
    }
    
    public long getID() {
        return id;
    }
    
    public Socket getSocket() {
        return socket;
    }
    
    public void setID(long id) {
        this.id = id;
    }
    
    public void setSocket(Socket socket) {
        this.socket = socket;
    }
    
}
