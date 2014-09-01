package client;

import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import messages.DiffieHellmanMessage;
import messages.ErrorMessage;
import messages.LoginMessage;
import messages.RegisterMessage;
import messages.SuccessMessage;
import messages.SuccessorPredecessorInfo;
import messages.TextMessage;
import messages.chord.ChordMessage;
import network_component.NetworkComponent;
import chord.Node;
import utilities.Pair;
import dhash.BigInt;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.crypto.spec.SecretKeySpec;
import messages.EncryptedMessage;
import messages.MessageTypes;
import messages.chord.FingerInfo;
import messages.chord.PredecessorNotification;
import messages.chord.ReplyChordID;
import messages.chord.ReplyFingers;
import messages.chord.ReplyPredecessor;
import messages.chord.ReplySuccessor;
import messages.chord.RequestChordID;
import messages.chord.RequestFinger;
import messages.chord.RequestFingers;
import messages.chord.RequestPredecessor;
import messages.chord.RequestSuccessor;
import messages.chord.SuccessorNotification;
import org.apache.http.nio.reactor.IOReactorException;
import security.SecurityUtils;
import security.SharedKeyAgreement;
import utilities.ThreadCachePool;

public class ClientComponent extends NetworkComponent {    
    
    private Node node;
    private boolean regRes;
    private boolean logRes;
    private String username;
    private final Map<BigInt, SharedKeyAgreement> userToKeyAgreement;        
    
    public ClientComponent(int port, boolean isCentralServer, boolean isRegistered) throws IOReactorException {
        super(port, isCentralServer);
        this.username = null;
        this.node = null;
        this.regRes = isRegistered;
        this.logRes = false;
        this.userToKeyAgreement = Collections.synchronizedMap(new HashMap<BigInt, SharedKeyAgreement>());
    }
    
    @Override
    public void run() {
        /*try {           
            Thread t1 = new Thread(server);
            Thread t2 = new Thread(client);        
            t2.start();
            t1.start();
            
            t2.join();
            t1.join();
        } catch (InterruptedException ex) {
            System.err.println(ex);
        } */
        
        ThreadCachePool.execute(server);
        ThreadCachePool.execute(client);
    }
    
    // maybe it is better to run it in a different thread
    public boolean initiateSharedKeyAgreement(BigInt userID, String dstIP, int dstPort, String protocol, boolean isInitiator) throws NoSuchAlgorithmException {
        node.insertToMap(userID, dstIP, dstPort);
        
        if (userToKeyAgreement.containsKey(userID)) {
            return false;
        } else {
            SharedKeyAgreement sharedKeyAgreement = new SharedKeyAgreement(node.getMyID(), node.getMyIP(), node.getMyPort(), this.client, dstIP, dstPort, protocol, isInitiator);
            //sharedKeyAgreement.run();            
            ThreadCachePool.execute(sharedKeyAgreement);
            userToKeyAgreement.put(userID, sharedKeyAgreement);
            return true;
        }
    }
    
    public void sendMessage(ConnInfo connInfo) {
        // If it is a text message, encrypt it.
        if (connInfo.getMessage().getMessageType() == MessageTypes.TEXT_MESSAGE) {
            TextMessage txtMsg = (TextMessage) connInfo.getMessage(); 
           
            BigInt userID = node.findFromIpPortToIDsMap(new Pair(connInfo.getIP(), connInfo.getPort()));
            SharedKeyAgreement keyAgreement = userToKeyAgreement.get(userID);
            if (keyAgreement == null) {
                System.err.println("Sth happened. UserID: " + userID.toHexString());
            }
            while (!keyAgreement.success()) {
                System.out.println("Waiting...");
                try {
                    Thread.sleep(500);
                } catch (InterruptedException ex) {
                }
            }
            //SecretKeySpec keySpec = keyAgreement.getSharedKey();
            try {
                byte[] encrMsgB = SecurityUtils.encrypt(keyAgreement.getSecretKey(), txtMsg.toByteArray());
                EncryptedMessage encrMsg = new EncryptedMessage(node.getMyID().getBytes(), encrMsgB);
                connInfo.replaceMessage(encrMsg);
            } catch (Exception ex) {
                System.err.println(ex);
            }
        }
        this.client.insert(connInfo);
    }
    
    public void registerToServer(String serverIP, Integer serverPort, String username, String password, String ip, Integer port) {
        RegisterMessage rMsg = new RegisterMessage(username, password, ip, port);
        ConnInfo connInfo = new ConnInfo(serverIP, serverPort, NetworkComponent.DEFAULT_PROTOCOL, rMsg);
        client.insert(connInfo);
    }
    
    public void loginToServer(String serverIP, Integer serverPort, String username, String password, String ip, Integer port) {
        LoginMessage rMsg = new LoginMessage(username, password, ip, port);
        ConnInfo connInfo = new ConnInfo(serverIP, serverPort, NetworkComponent.DEFAULT_PROTOCOL, rMsg);
        client.insert(connInfo);
        this.username = username;
    }

    @Override
    public void handleRegister(RegisterMessage msg) {
        // DO NOTHING
    }

    @Override
    public void handleLogin(LoginMessage msg) {
        // DO NOTHING
    }

    @Override
    public void handleError(ErrorMessage msg) {
        // Depending on the error, either display the error message or quit the application
        System.err.println("Error Message: " + msg.getErrorMsgStr());
    }

    @Override
    public void handleDiffieHellman(DiffieHellmanMessage msg) {
       BigInt userID = msg.getID();
       SharedKeyAgreement sharedKeyAgreement;
       try {
            if ( (sharedKeyAgreement = userToKeyAgreement.get(userID)) == null) {
                sharedKeyAgreement = new SharedKeyAgreement(node.getMyID(), node.getMyIP(), node.getMyPort(), client, msg.getIP(), msg.getPort(), NetworkComponent.DEFAULT_PROTOCOL, false);                
                node.insertToMap(msg.getID(), msg.getIP(), msg.getPort());
                ThreadCachePool.execute(sharedKeyAgreement);
                userToKeyAgreement.put(userID, sharedKeyAgreement);
            }
            
            sharedKeyAgreement.insert(msg);
            
       } catch (NoSuchAlgorithmException ex) {
           System.err.println(ex);
       }
    }

    @Override
    public void handleSuccess(SuccessMessage msg) {
        switch (msg.getSuccessMessageType()) {
            case SuccessMessage.LOGIN_SUCCESSFUL:
                System.out.println("Login was successful");
                logRes = true;
            try {             
                this.node = new Node(username, this);
            } catch (NoSuchAlgorithmException ex) {
                System.err.println(ex);
                System.exit(-1);
            }
                break;
            case SuccessMessage.REGISTER_SUCCESSFUL:
                System.out.println("Register was successful");
                regRes = true;
                break;
            case SuccessMessage.TEXT_RECEIVED:
                System.err.println("Received Time: " + System.currentTimeMillis());
                break;
            default:
                System.err.println("Unknown SuccessMessage");
                break;
        }
    }

    @Override
    public void handleTextMessage(TextMessage msg) {
        // Display it to screen
        
        System.out.println("Received Text: " + msg.getTextMsg());
    }
    
    
    @Override
    public void handleSuccessorPredecessorInfo(SuccessorPredecessorInfo msg) {
        node.createChordRing();
        
        node.join(new BigInt(msg.getSuccessorID()), msg.getSuccessorIP(), msg.getSuccessorPort(), new BigInt(msg.getPredecessorID()), msg.getPredecessorIP(), msg.getPredecessorPort());
    }
    
    private void handlePredecessorNotification(PredecessorNotification msg) {
        node.setPredecessor(new BigInt(msg.getPredecessorID()), msg.getPredecessorIP(), msg.getPredecessorPort());
    }   
    
    private void handleReplyChordID(ReplyChordID msg) {
        
    }
    
    private void handleFingerInfo(FingerInfo msg) {
        node.insertFingerInfo(msg);
    }
    
    private void handleReplyFingers(ReplyFingers msg) {
        node.insertReplyFingers(msg);
    }
    
    private void handleRequestChordID(RequestChordID msg) {
       // ReplyChordID reply = new ReplyChordID(node.getMyUsername());
        
    }
    
    private void handleRequestFinger(RequestFinger msg) {
        FingerInfo fingerInfo = node.findClosestPrecedingFinger(msg.getID());
        ConnInfo connInfo = new ConnInfo(msg.getMyIP(), msg.getMyPort(), NetworkComponent.DEFAULT_PROTOCOL, fingerInfo);
        sendMessage(connInfo);
    }
    
    private void handleRequestFingers(RequestFingers msg) {
        List<BigInt> fingers = node.getFingerList();
        List<byte[]> fingersBytes = new ArrayList<>();
        List<Pair<String, Integer>> fingersInfo = new ArrayList<>();
        for (int i = 0; i < fingers.size() - 1; i++) {
            fingersBytes.add(fingers.get(i).getBytes());
            fingersInfo.add(node.findFromIDsToIPPortMap(fingers.get(i)));
        }
        
        ReplyFingers replyFingers = new ReplyFingers(fingersBytes, fingersInfo);
        ConnInfo connInfo = new ConnInfo(msg.getMyIP(), msg.getMyPort(), NetworkComponent.DEFAULT_PROTOCOL, replyFingers);
        sendMessage(connInfo);
    }
    
    private void handleRequestPredecessor(RequestPredecessor msg) {
        Pair<String, Integer> info = node.getPredecessorInfo();
        BigInt predID = node.getPredecessorID();
        
        ReplyPredecessor reply = new ReplyPredecessor(predID.getBytes(), info.getFirst(), info.getSecond(), msg.getSource());
        ConnInfo connInfo = new ConnInfo(msg.getMyIP(), msg.getMyPort(), NetworkComponent.DEFAULT_PROTOCOL, reply);
        sendMessage(connInfo);
    }
    
    private void handleReplyPredecessor(ReplyPredecessor msg) {
        if (msg.getSource() == RequestPredecessor.STABILIZE) {
            node.getStabilizeInstance().insert(msg);
        } else if (msg.getSource() == RequestPredecessor.TEXT_APPLICATION) {
            // TODO
        }
    }
    
    private void handleSuccessorNotification(SuccessorNotification msg) {
        node.setSuccessor(new BigInt(msg.getSuccesorID()), msg.getSuccessorIP(), msg.getSuccessorPort());
        // update Fingers
    }
    
    private void handleRequestSuccessor(RequestSuccessor msg) {
        Pair<String, Integer> info = node.getSuccessorInfo();
        ReplySuccessor replySuccessor = new ReplySuccessor(node.getSuccessorID().getBytes(), info.getFirst(), info.getSecond());
        ConnInfo connInfo = new ConnInfo(msg.getMyIP(), msg.getMyPort(), NetworkComponent.DEFAULT_PROTOCOL, replySuccessor);
        sendMessage(connInfo);
    }
    
    private void handleReplySuccessor(ReplySuccessor msg) {
        node.insertReplySuccessor(msg);
    }
    
    @Override
    public void handleChordMessage(ChordMessage msg) {
        System.out.println(msg.getChordMessageType());
        switch(msg.getChordMessageType()) {            
            case ChordMessage.PREDECESSOR_NOTIFICATION:
                System.out.println("ChordMessage: PREDECESSOR_NOTIFICATION");
                handlePredecessorNotification((PredecessorNotification) msg);
                break;
            case ChordMessage.REPLY_CHORD_ID:
                System.out.println("ChordMessage: REPLY_CHORD_ID");
                handleReplyChordID((ReplyChordID) msg);
                break;
            case ChordMessage.FINGER_INFO:
                System.out.println("ChordMessage: FINGER_INFO");
                handleFingerInfo((FingerInfo) msg);
                break;
            case ChordMessage.REPLY_FINGERS:
                System.out.println("ChordMessage: REPLY_FINGERS");
                handleReplyFingers((ReplyFingers) msg);
                break;
            case ChordMessage.REQUEST_CHORD_ID:
                System.out.println("ChordMessage: REQUEST_CHORD_ID");
                handleRequestChordID((RequestChordID) msg);
                break;
            case ChordMessage.REQUEST_FINGER:
                System.out.println("ChordMessage: REQUEST_FINGER");
                handleRequestFinger((RequestFinger) msg);
                break;
            case ChordMessage.REQUEST_FINGERS:
                System.out.println("ChordMessage: REQUEST_FINGERS");
                handleRequestFingers((RequestFingers) msg);
                break;
            case ChordMessage.REQUEST_PREDECESSOR:
                System.out.println("ChordMessage: REQUEST_PREDECESSOR");
                handleRequestPredecessor((RequestPredecessor) msg);
                break;
            case ChordMessage.SUCCESSOR_NOTIFICATION:
                System.out.println("ChordMessage: SUCCESSOR_NOTIFICATION");
                handleSuccessorNotification((SuccessorNotification) msg);
                break;
            case ChordMessage.REPLY_PREDECESSOR:
                System.out.println("ChordMessage: REPLY_PREDECESSOR");
                handleReplyPredecessor((ReplyPredecessor) msg);
                break;
            case ChordMessage.REQUEST_SUCCESSOR:
                System.out.println("ChordMessage: REQUEST_SUCCESSOR");
                handleRequestSuccessor((RequestSuccessor) msg);
                break;
            case ChordMessage.REPLY_SUCCESSOR:
                System.out.println("ChordMessage: REPLY_SUCCESSOR");
                handleReplySuccessor((ReplySuccessor) msg);
                break;
            default:
                System.err.println("Unkwown ChordMessage Type");                
        }
    }

    @Override
    public void handleEncryptedMessage(EncryptedMessage encrMsg) {
        BigInt userID = new BigInt(encrMsg.getUserID());
        
        if (!userToKeyAgreement.containsKey(userID)) {
            System.err.println("You need to perform a Key Agreement first");
        } else {
            SecretKeySpec secretKey = userToKeyAgreement.get(userID).getSharedKey();
            try {
                byte[] decrBytes = SecurityUtils.decrypt(secretKey, encrMsg.getEncryptedBytes());
                
                if (decrBytes[0] == MessageTypes.TEXT_MESSAGE) {
                    TextMessage txtMsg = TextMessage.parseArray(decrBytes);
                    handleTextMessage(txtMsg);
                } else {
                    System.out.println("Something wrong happened. Only Text Messages can be encrypted");
                }
                
            } catch (Exception ex) {
                System.err.println(ex);
            }
        }
        
        
    }
    
}
