package server;

import chord.BigInt;
import chord.ChordRing;
import client.ConnInfo;
import java.security.NoSuchAlgorithmException;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;
import messages.DiffieHellmanMessage;
import messages.EncryptedMessage;
import messages.ErrorMessage;
import messages.LoginMessage;
import messages.Message;
import messages.RegisterMessage;
import messages.SuccessMessage;
import messages.SuccessorPredecessorInfo;
import messages.TextMessage;
import messages.chord.ChordMessage;
import network_component.NetworkComponent;
import org.apache.commons.codec.binary.Base64;
import org.apache.http.nio.reactor.IOReactorException;
import security.SecurityUtils;
import store.SQLiteJDBC;
import utilities.Pair;
import utilities.ThreadCachePool;

public class ServerComponent extends NetworkComponent {

    private final SQLiteJDBC store;
    private static final String DATABASE_STR = "database.db";
    private final ChordRing chordRing;

    public ServerComponent(int port, boolean isCentralServer) throws IOReactorException {
        super(port, isCentralServer);
        this.store = new SQLiteJDBC(DATABASE_STR);
        try {
            this.store.connect();
        } catch (Exception ex) {
            System.err.println(ex);
            System.exit(-1);
        }
        this.chordRing = new ChordRing();
    }

    @Override
    public void handleRegister(RegisterMessage msg) {
        try {
            if (store.insertUser(msg)) {
                System.out.println("User was inserted successfully: IP = " + msg.getIP() + " Port: " + msg.getPort());
                SuccessMessage sucMsg = new SuccessMessage(SuccessMessage.REGISTER_SUCCESSFUL);
                client.insert(new ConnInfo(msg.getIP(), msg.getPort(), NetworkComponent.DEFAULT_PROTOCOL, sucMsg));
            } else {
                ErrorMessage errMsg = new ErrorMessage(ErrorMessage.ERROR_USERID_EXISTS, "User already exists");
                client.insert(new ConnInfo(msg.getIP(), msg.getPort(), NetworkComponent.DEFAULT_PROTOCOL, errMsg));
            }
        } catch (SQLException ex) {
            System.err.println(ex);
            ErrorMessage errMsg = new ErrorMessage(ErrorMessage.ERROR_TRY_AGAIN, "Server-side error. Please try again.");
            client.insert(new ConnInfo(msg.getIP(), msg.getPort(), NetworkComponent.DEFAULT_PROTOCOL, errMsg));
        }
    }

    @Override
    public void handleLogin(LoginMessage msg) {
        if (store.findUser(msg.getUsername())) {
            if (store.updateUser(msg)) {
                System.out.println("User's IP and Port were updated successfully: IP = " + msg.getIP() + " Port = " + msg.getPort());

                BigInt id = new BigInt(Base64.decodeBase64(msg.getUsername()));
                chordRing.insert(id, msg.getIP(), msg.getPort());

                Message sucMsg = new SuccessMessage(SuccessMessage.LOGIN_SUCCESSFUL);
                
                // send successorPredecessorInfo message
                int pos = chordRing.findPosition(id);
                
                System.out.println("Position: " + pos);
                Pair<String, Integer> sucInfo = chordRing.getSuccessorInfo(pos);
                System.out.println("HERE0");
                Pair<String, Integer> predInfo = chordRing.getPredecessorInfo(pos);
                 System.out.println("HERE1");
                BigInt sucID = chordRing.getSuccessorID(pos);
                 System.out.println("HERE2");
                BigInt predID = chordRing.getPredecessorID(pos);
                 System.out.println("HERE3");
                Message sucPredMsg = new SuccessorPredecessorInfo(sucInfo.getFirst(), sucInfo.getSecond(), sucID.getBytes(), predInfo.getFirst(), predInfo.getSecond(), predID.getBytes());
                 System.out.println("HERE4");
                System.out.println("Sending SUCCESSOR_PREDECESSOR INFO: " + sucInfo.getFirst() + "/" + sucInfo.getSecond() + " - " + predInfo.getFirst() + "/" + predInfo.getSecond());
                client.insert(new ConnInfo(msg.getIP(), msg.getPort(), NetworkComponent.DEFAULT_PROTOCOL, sucMsg));
                try {
                    Thread.sleep(NetworkComponent.SLEEP_TIME);
                } catch (InterruptedException ex) {
                    Logger.getLogger(ServerComponent.class.getName()).log(Level.SEVERE, null, ex);
                }
                client.insert(new ConnInfo(msg.getIP(), msg.getPort(), NetworkComponent.DEFAULT_PROTOCOL, sucPredMsg));
            }
        } else {
            System.out.println("User does not exist");
            ErrorMessage errMsg = new ErrorMessage(ErrorMessage.ERROR_NO_USER, "User does not exist. Please register.");
            client.insert(new ConnInfo(msg.getIP(), msg.getPort(), NetworkComponent.DEFAULT_PROTOCOL, errMsg));
        }
    }

    @Override
    public void handleError(ErrorMessage msg) {
        // Maybe do nothing
    }

    @Override
    public void handleDiffieHellman(DiffieHellmanMessage msg) {
        // Do nothing
    }

    @Override
    public void handleSuccess(SuccessMessage msg) {
        // Maybe do nothing
    }

    @Override
    public void handleSuccessorPredecessorInfo(SuccessorPredecessorInfo msg) {
        // Do nothing
    }

    @Override
    public void handleTextMessage(TextMessage msg) {
        // Do nothing
    }

    @Override
    public void run() {
        ThreadCachePool.execute(server);
        ThreadCachePool.execute(client);
        /*
         try {
         Thread t1 = new Thread(server);
         Thread t2 = new Thread(client);        
         t2.start();
         t1.start();
            
         t2.join();
         t1.join();
         } catch (InterruptedException ex) {
         System.err.println(ex);
         }*/
    }

    @Override
    public void handleChordMessage(ChordMessage msg) {
        // DO NOTHING
    }

    @Override
    public void handleEncryptedMessage(EncryptedMessage msg) {
        // DO NOTHING
    }
}
