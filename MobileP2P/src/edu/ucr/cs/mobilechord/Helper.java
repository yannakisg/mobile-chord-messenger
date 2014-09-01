package edu.ucr.cs.mobilechord;

import java.security.NoSuchAlgorithmException;

import org.apache.http.nio.reactor.IOReactorException;

import edu.ucr.cs.mobilechord.client.client.ClientComponent;
import edu.ucr.cs.mobilechord.transportapi.client.ConnInfo;
import edu.ucr.cs.mobilechord.transportapi.dhash.BigInt;
import edu.ucr.cs.mobilechord.transportapi.messages.TextMessage;
import edu.ucr.cs.mobilechord.transportapi.network_component.NetworkComponent;
import edu.ucr.cs.mobilechord.transportapi.security.SecurityUtils;
import edu.ucr.cs.mobilechord.transportapi.utilities.Pair;
import edu.ucr.cs.mobilechord.transportapi.utilities.ThreadCachePool;

public class Helper {
	
	private static String wifiIP = "127.0.0.1";
	private static final Integer port = 8080;
	
	private static ClientComponent client;
	
	private static TextActivity textActivity = null;
	
	public static void createClientComponent() throws IOReactorException {
		client = new ClientComponent(port, false, false);
		client.setIP(wifiIP);
		ThreadCachePool.execute(client);
	}
	
	public static void setWifiIP(String ip) {
		wifiIP = ip;
	}
	
	public static String getWifiIP() {
		return wifiIP;
	}
	
	public static Integer getPort() {
		return port;
	}
	
	
	public static void register(String username, String password, String serverIP, String portIP) {
		client.registerToServer(serverIP, Integer.parseInt(portIP), username, password, wifiIP, port);
	}
	
	public static void login(String location, String username, String password, String serverIP, String portIP) {
		// I should use the location
		client.loginToServer(serverIP, Integer.parseInt(portIP), username, password, wifiIP, port);
	}
	
	public static void sendTextMessageTo(TextActivity activity, String username, String text, boolean initiate) {	
		textActivity = activity;
		ThreadCachePool.execute(new TextMessageSender(username, text, initiate));
	}
	
	public static void displayMessage(String text) {
		if (textActivity != null) {
			textActivity.displayMessage(text);
		}
	}
	
	public static class TextMessageSender implements Runnable {
		
		private String username;
		private String text;
		private boolean initiate;
		
		public TextMessageSender(String username, String text, boolean initiate) {
			this.username = username;
			this.text = text;
			this.initiate = initiate;
		}
		
		@Override
		public void run() {
			BigInt dstID = null;
			try {
				dstID = new BigInt(SecurityUtils.sha1Bytes(username));
			} catch (NoSuchAlgorithmException e) {
			}
			
			Pair<String, Integer> info = client.findInfo(dstID);
			
			if (initiate) {
				Thread t = new Thread(new SharedKeyExchanger(client, dstID, info.getFirst(), info.getSecond()));
	            t.start();
	            try {
	            	t.join();
	            } catch (InterruptedException ex) {}
			}
			
			TextMessage txtMsg = new TextMessage(text);
			ConnInfo connInfo = new ConnInfo(info.getFirst(), info.getSecond(), NetworkComponent.DEFAULT_PROTOCOL, txtMsg);
	        client.sendMessage(connInfo);
		}
		
	}
	
	 private static class SharedKeyExchanger implements Runnable {
		private final ClientComponent client;
        private final BigInt dstID;
        private final String dstIP;
        private final Integer dstPort;
        
        public SharedKeyExchanger(ClientComponent client, BigInt dstID, String dstIP, Integer dstPort) {
            this.client = client;
            this.dstID = dstID;
            this.dstIP = dstIP;
            this.dstPort = dstPort;
        }
        
        @Override
        public void run() {
            try {
                
                //System.out.println("DstID: " + dstID.toHexString());
                //System.err.println("Sent Time: " + System.currentTimeMillis());
                client.initiateSharedKeyAgreement(dstID, dstIP, dstPort, NetworkComponent.DEFAULT_PROTOCOL, true);
            } catch (NoSuchAlgorithmException ex) {
                System.err.println(ex);
            }
        }
		
	 }
}
