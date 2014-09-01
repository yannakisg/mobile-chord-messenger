package network_component;

import client.HttpClient;
import messages.DiffieHellmanMessage;
import messages.EncryptedMessage;
import messages.ErrorMessage;
import messages.LoginMessage;
import messages.RegisterMessage;
import messages.SuccessMessage;
import messages.SuccessorPredecessorInfo;
import messages.TextMessage;
import messages.chord.ChordMessage;
import org.apache.http.nio.reactor.IOReactorException;
import server.HttpServer;

public abstract class NetworkComponent implements Runnable {
    public static final int DEFAULT_PORT = 8080;
    public static final String DEFAULT_PROTOCOL = "http";
    public static final String DEFAULT_SERVER_IP = "127.0.0.1";
    
    public static int SLEEP_TIME = 10;
    
    private String ip;
    private final Integer port;
    
    protected HttpClient client;
    protected HttpServer server;
    
    public NetworkComponent(int port , boolean isCentralServer) throws IOReactorException {
        this.client = new HttpClient();
        this.server = new HttpServer(this, port, client, isCentralServer);
        this.port = server.getPort();
    }
    
    public HttpClient getHttpClient() {
        return this.client;
    }
    
    public HttpServer getHttpServer() {
        return this.server;
    }
    
    public void setIP(String ip) {
        this.ip = ip;
    }
    
    public String getIP() {
        return ip;
    }
    
    public Integer getPort() {
        return port;
    }
    
    public abstract void handleRegister(RegisterMessage msg);
    
    public abstract void handleLogin(LoginMessage msg);
    
    public abstract void handleError(ErrorMessage msg);
    
    public abstract void handleDiffieHellman(DiffieHellmanMessage msg);
    
    public abstract void handleSuccess(SuccessMessage msg);
    
    public abstract void handleSuccessorPredecessorInfo(SuccessorPredecessorInfo msg);
    
    public abstract void handleTextMessage(TextMessage msg);
    
    public abstract void handleChordMessage(ChordMessage msg);
    
    public abstract void handleEncryptedMessage(EncryptedMessage msg);
}
