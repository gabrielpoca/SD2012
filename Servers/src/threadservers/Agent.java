/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package threadservers;

import java.net.Socket;

public class Agent extends Thread {
    
    Socket socket;
    
    public Agent() {
	
    }
    
    public Agent(Socket socket) {
	this.socket = socket;
    }
    
    public void setSocket(Socket socket) {
	this.socket = socket;
    }
    
    public Socket getSocket() {
	return socket;
    }

    public Agent clone() {
	return new Agent(socket);
    }    
}
