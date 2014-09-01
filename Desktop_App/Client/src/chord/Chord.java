package chord;

import client.ConnInfo;
import dhash.BigInt;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import messages.chord.FingerInfo;
import messages.chord.PredecessorNotification;
import messages.chord.ReplyFingers;
import messages.chord.ReplySuccessor;
import messages.chord.RequestFinger;
import messages.chord.RequestFingers;
import messages.chord.RequestSuccessor;
import messages.chord.SuccessorNotification;
import network_component.NetworkComponent;
import utilities.Pair;

public class Chord {
    
    private static final BlockingQueue<ReplyFingers> blockingQueueFingers = new LinkedBlockingQueue<>();
    private static final BlockingQueue<ReplySuccessor> blockingQueueSuccessor = new LinkedBlockingQueue<>();
    private static final BlockingQueue<FingerInfo> blockingQueueFinger = new LinkedBlockingQueue<>();
    
    public static void create(Node node) {
        node.setSuccessor(node.getMyID(), node.getMyIP(), node.getMyPort());
        node.setPredecessor(node.getMyID(), node.getMyIP(), node.getMyPort());
        
        node.initializeFingerTable();
    }
    
    public static void notifyNode(Node self, BigInt possiblePredecessorID, String possiblePredecessorIP, Integer possiblePredecessorPort) {
        if (self.getPredecessorID() == null || BigInt.isBetween(possiblePredecessorID, self.getPredecessorID(), self.getMyID()))  {
            self.setPredecessor(possiblePredecessorID, possiblePredecessorIP, possiblePredecessorPort);
        }
    }
    
    public static void join(Node self, BigInt successorID, String successorIP, Integer succesorPort, BigInt predecessorID, String predecessorIP, Integer predecessorPort) throws InterruptedException {
        PredecessorNotification predNotification = new PredecessorNotification(self.getMyID().getBytes(), self.getMyIP(), self.getMyPort());
        ConnInfo connInfoSuc = new ConnInfo(successorIP, succesorPort, NetworkComponent.DEFAULT_PROTOCOL, predNotification);
        self.sendMessage(connInfoSuc);        
        Thread.sleep(NetworkComponent.SLEEP_TIME);
        
        SuccessorNotification succNotification = new SuccessorNotification(self.getMyID().getBytes(), self.getMyIP(), self.getMyPort());
        ConnInfo connInfoPred = new ConnInfo(predecessorIP, predecessorPort, NetworkComponent.DEFAULT_PROTOCOL, succNotification);
        self.sendMessage(connInfoPred);
         Thread.sleep(NetworkComponent.SLEEP_TIME);
        
        RequestFingers reqFingers = new RequestFingers(self.getMyIP(), self.getMyPort());
        ConnInfo connInfoSucFingers = new ConnInfo(successorIP, succesorPort, NetworkComponent.DEFAULT_PROTOCOL, reqFingers);
        self.sendMessage(connInfoSucFingers);
        Thread.sleep(NetworkComponent.SLEEP_TIME);
         
        try {
            ReplyFingers replyFingersSuc = blockingQueueFingers.take();
            
            ConnInfo connInfoPredFingers = new ConnInfo(predecessorIP, predecessorPort, NetworkComponent.DEFAULT_PROTOCOL, reqFingers);
            self.sendMessage(connInfoPredFingers);
             Thread.sleep(NetworkComponent.SLEEP_TIME);
            ReplyFingers replyFingersPred = blockingQueueFingers.take();
            
            updateFingers(self, replyFingersSuc, replyFingersPred);
        } catch (InterruptedException ex) {
            System.err.println(ex);
        }
        
        
    }
    
    private static void updateFingers(Node self, ReplyFingers fingersSuc, ReplyFingers fingersPred) {
        List<BigInt> fingers = new ArrayList<>();
        List<Pair<String, Integer>> fingersInfo = new ArrayList<>();
        
        List<BigInt> fingersSucB = new ArrayList<>();
        List<BigInt> fingersPredB = new ArrayList<>();
        List<Pair<String, Integer>> fingersInfoSuc = fingersSuc.getFingersInfo();
        List<Pair<String, Integer>> fingersInfoPred = fingersPred.getFingersInfo();
        fingers.add(self.getSuccessorID());
        
        for (int i = 0; i < fingersSuc.getFingerList().size(); i++) {
            fingersSucB.add(new BigInt(fingersSuc.getFingerList().get(i)));
        }
        
        for (int i = 0; i < fingersPred.getFingerList().size(); i++) {
            fingersPredB.add(new BigInt(fingersPred.getFingerList().get(i)));
        }
        
        BigInt myID = self.getMyID();
        BigInt fID;
        BigInt curSuc = self.getSuccessorID();
        Pair<String, Integer> curSucInfo = self.getSuccessorInfo();
        
        for (int i = 1; i < 160; i++) {
           fID = new BigInt(myID.addpowerOfTwo(i).modM(160));
           
           for (int j = 0; j < fingersSucB.size(); j++) {
               if (BigInt.isBetween(fID, myID, fingersSucB.get(j)) && curSuc.compareTo(fingersSucB.get(j)) > 0) {
                   curSuc = fingersSucB.get(j);
                   curSucInfo = fingersInfoSuc.get(j);
               }
           }
           for (int j = 0; j < fingersPredB.size(); j++) {
               if (BigInt.isBetween(fID, myID, fingersPredB.get(j)) && curSuc.compareTo(fingersPredB.get(j)) > 0) {
                   curSuc = fingersSucB.get(j);
                   curSucInfo = fingersInfoPred.get(j);
               }
           }
           
           fingers.add(curSuc);
           fingersInfo.add(curSucInfo);
        }
        
        self.setFingers(fingers, fingersInfo);
    }
    
    public static void disconnect(Node self) {
        // TODO one day 
    }

    public static BigInt findSuccessor(Node self, BigInt id) throws InterruptedException {
        BigInt successor = findPredecessor(self, id);
        return successor;
    }    
    
    private static BigInt findPredecessor(Node self, BigInt id) throws InterruptedException {
        BigInt successor = self.getSuccessorID();
        BigInt nID = self.getMyID();
        String nIP = self.getMyIP();
        Integer nPort = self.getMyPort();
        
        while (!BigInt.isBetween(id, nID, successor)) {
            if (nID.equals(self.getMyID())) {
                nID = closestPrecedingFinger(self, id); 
                Pair<String, Integer> info = self.findFromIDsToIPPortMap(nID);
                nIP = info.getFirst();
                nPort = info.getSecond();
            } else {
                
                RequestFinger requestFinger = new RequestFinger(self.getMyIP(), self.getMyPort(), id.getBytes());
                ConnInfo connInfo = new ConnInfo(nIP, nPort, NetworkComponent.DEFAULT_PROTOCOL, requestFinger);
                self.sendMessage(connInfo);
                Thread.sleep(NetworkComponent.SLEEP_TIME);
                 try {
                    FingerInfo fingerInfo = blockingQueueFinger.take();
                    nID = new BigInt(fingerInfo.getFingerID());
                    nIP = fingerInfo.getFingerIP();
                    nPort = fingerInfo.getFingerPort();
                 } catch (InterruptedException ex) {
                    System.err.println(ex);
                }
                // request closest preceding finger from successor
               // nID = reply ID and replyID.successor
            }
            
            
            RequestSuccessor reqSuc = new RequestSuccessor(self.getMyIP(), self.getMyPort());
            ConnInfo connInfo = new ConnInfo(nIP, nPort, NetworkComponent.DEFAULT_PROTOCOL, reqSuc);
            self.sendMessage(connInfo);            
            try {
                Thread.sleep(NetworkComponent.SLEEP_TIME);
                ReplySuccessor replySuc = blockingQueueSuccessor.take();
                successor = new BigInt(replySuc.getSuccessorID());
                self.insertToMap(successor, replySuc.getSuccessorIP(), replySuc.getSuccessorPort());
            } catch (InterruptedException ex) {
                System.err.println(ex);
            }
        }
        
        return successor;
    }
    
    public static BigInt closestPrecedingFinger(Node self, BigInt id) {
        for (int i = 159; i >= 0; i--) {
            if (BigInt.isBetween(self.getFinger(i), self.getMyID(), id)) {
                return self.getFinger(i);
            }
        }
        
        return self.getMyID();
    }
    
    public static void insertFingerInfo(FingerInfo fingerInfo) {
        blockingQueueFinger.add(fingerInfo);
    }
    
    public static void insertReplyFingers(ReplyFingers replyFingers) {
        blockingQueueFingers.add(replyFingers);
    }
    
    public static void insertReplySuccessor(ReplySuccessor replySuccessor) {
        blockingQueueSuccessor.add(replySuccessor);
    }
}
