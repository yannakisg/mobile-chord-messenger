package chord;

import utilities.Pair;
import client.ConnInfo;
import dhash.BigInt;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import messages.chord.PredecessorNotification;
import messages.chord.ReplyPredecessor;
import messages.chord.RequestPredecessor;
import network_component.NetworkComponent;

public class Stabilize implements Runnable {
    private final Node node;
    private final BlockingQueue<ReplyPredecessor> blockingQueue;
    
    public Stabilize(Node node) {
        this.node = node;
        this.blockingQueue = new LinkedBlockingQueue<>();
    }

    @Override
    public void run() {
        System.out.println("Running Stabilize");
        BigInt curSuccessorID = node.getSuccessorID();
        Pair<String, Integer> curSuccessor = node.getSuccessorInfo();
        
        if (curSuccessorID.equals(node.getMyID())) {
            System.out.println("No need to run stabilize. Return");
            return;
        }
        
        RequestPredecessor msg = new RequestPredecessor(node.getMyIP(), node.getMyPort(), RequestPredecessor.STABILIZE);
        ConnInfo connInfo = new ConnInfo(curSuccessor.getFirst(), curSuccessor.getSecond(), NetworkComponent.DEFAULT_PROTOCOL, msg);
        node.sendMessage(connInfo);
        try {
            Thread.sleep(NetworkComponent.SLEEP_TIME);
       
            ReplyPredecessor replyMsg = blockingQueue.take(); // Better to use poll.
            BigInt newSuccID = new BigInt(replyMsg.getPredecessorID());
            if (! newSuccID.equals(node.getMyID())) {
                if (BigInt.isBetween(newSuccID, node.getMyID(), curSuccessorID)) {
                    node.setSuccessor(newSuccID, replyMsg.getPredecessorIP(), replyMsg.getPredecessorPort());
                    
                    PredecessorNotification predNot = new PredecessorNotification(node.getMyID().getBytes(), node.getMyIP(), node.getMyPort());
                    connInfo = new ConnInfo(replyMsg.getPredecessorIP(), replyMsg.getPredecessorPort(), NetworkComponent.DEFAULT_PROTOCOL, predNot);
                    node.sendMessage(connInfo);
                    Thread.sleep(NetworkComponent.SLEEP_TIME);
                }
            }
        } catch (InterruptedException ex) {
            System.err.println(ex);
        }
    }
    
    public void insert(ReplyPredecessor msg) {
        this.blockingQueue.offer(msg);
    }
}
