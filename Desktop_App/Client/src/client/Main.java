package client;

import dhash.BigInt;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import messages.TextMessage;
import network_component.NetworkComponent;
import org.apache.http.nio.reactor.IOReactorException;
import security.SecurityUtils;
import utilities.ThreadCachePool;

public class Main {
    
    private static final String CENTRAL_SERVER_IP = "192.168.1.2";
    private static final Integer CENTRAL_SERVER_PORT = 8080;
    
    private static String MY_SERVER_IP;// = "192.168.1.3";
    private static Integer MY_SERVER_PORT;// = 8080;
    
    private static final String USERNAME = "test104";
    private static final String PASSWORD = "test104";
    
    private static final String DST_USERNAME = "test200";
    private static final String DST_IP = "192.168.1.6";
    
    private static void register(ClientComponent client) {
        client.registerToServer(CENTRAL_SERVER_IP, CENTRAL_SERVER_PORT, USERNAME, PASSWORD, MY_SERVER_IP, MY_SERVER_PORT);        
    }
    
    private static void login(ClientComponent client) {
        client.loginToServer(CENTRAL_SERVER_IP, CENTRAL_SERVER_PORT, USERNAME, PASSWORD, MY_SERVER_IP, MY_SERVER_PORT);
    }
    
    private static void sendTextMessageTo(ClientComponent client, String dstID, String dstIP, Integer dstPort, String message, boolean firstTime) throws NoSuchAlgorithmException, InterruptedException {
        TextMessage txtMsg = new TextMessage(message);
        if (firstTime) {
            Thread t = new Thread(new SharedKeyExchanger(client, dstID, dstIP, dstPort));
            t.start();
            t.join();
        }
        
        ConnInfo connInfo = new ConnInfo(dstIP, dstPort, NetworkComponent.DEFAULT_PROTOCOL, txtMsg);
        client.sendMessage(connInfo);
    }
    
    public static void main(String[] args) throws IOReactorException, InterruptedException, NoSuchAlgorithmException {
        MY_SERVER_IP = args[0];
        MY_SERVER_PORT = Integer.parseInt(args[1]);
        System.out.println("My Server IP: " + MY_SERVER_IP + " My Server Port: " + MY_SERVER_PORT);
        
        ClientComponent client = new ClientComponent(MY_SERVER_PORT, false, false);
        client.setIP(MY_SERVER_IP);
        ThreadCachePool.execute(client);
        
        
        // For debugging purposes
        register(client);
        Thread.sleep(2000);
        login(client);
        
        Thread.sleep(3000);    
        sendTextMessageTo(client, DST_USERNAME, DST_IP, 8080, "Hello World", true);
        
        for (int i = 0; i < 9; i++) {
            Thread.sleep(3000); 
            System.err.println("Sent Time: " + System.currentTimeMillis());
            sendTextMessageTo(client, DST_USERNAME, DST_IP, 8080, "Hello World", false);
        }
    }
    
    private static class SharedKeyExchanger implements Runnable {
        private final ClientComponent client;
        private final String dstUsername;
        private final String dstIP;
        private final Integer dstPort;
        
        public SharedKeyExchanger(ClientComponent client, String dstUsername, String dstIP, Integer dstPort) {
            this.client = client;
            this.dstUsername = dstUsername;
            this.dstIP = dstIP;
            this.dstPort = dstPort;
        }
        
        @Override
        public void run() {
            try {
                BigInt dstID = new BigInt(SecurityUtils.sha1Bytes(dstUsername));
                System.out.println("DstID: " + dstID.toHexString());
                System.err.println("Sent Time: " + System.currentTimeMillis());
                client.initiateSharedKeyAgreement(dstID, dstIP, dstPort, NetworkComponent.DEFAULT_PROTOCOL, true);
            } catch (NoSuchAlgorithmException ex) {
                System.err.println(ex);
            }
        }
        
    }
}
