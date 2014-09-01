package edu.ucr.cs.mobilechord.transportapi.client;

import edu.ucr.cs.mobilechord.transportapi.messages.Message;


public class ConnInfo {
    
    private final String ip;
    private final int port;
    private final String protocol;
    private Message message;
    
    public ConnInfo (String ip, int port, String protocol, Message message) {
        this.ip = ip;
        this.port = port;
        this.protocol = protocol;
        this.message = message;
    }
    
    public void replaceMessage(Message message) {
        this.message = message;
    }
    
    public int getPort() {
        return this.port;
    }
    
    public String getIP() {
        return this.ip;
    }
    
    public String getProtocol() {
        return this.protocol;
    }
    
    public Message getMessage() {
        return this.message;
    }
}
