package edu.ucr.cs.mobilechord.client.chord;


import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import edu.ucr.cs.mobilechord.client.client.ClientComponent;
import edu.ucr.cs.mobilechord.transportapi.client.ConnInfo;
import edu.ucr.cs.mobilechord.transportapi.dhash.BigInt;
import edu.ucr.cs.mobilechord.transportapi.messages.chord.FingerInfo;
import edu.ucr.cs.mobilechord.transportapi.messages.chord.ReplyFingers;
import edu.ucr.cs.mobilechord.transportapi.messages.chord.ReplySuccessor;
import edu.ucr.cs.mobilechord.transportapi.security.SecurityUtils;
import edu.ucr.cs.mobilechord.transportapi.utilities.Pair;

public class Node {

    private final BigInt myID;
    private final String myUsername;
    private final Map<Pair<String, Integer>, BigInt> ipPortToIDs; // for now. We have to have <Pair<String, Integer>, BigInt>.  {IP, PORT} -> ID
    private final Map<BigInt, Pair<String, Integer>> idsToIpPort;
    private BigInt successorID;
    private BigInt predecessorID;
    private List<BigInt> fingerList;
    private ScheduledExecutorService scheduler;
    private ScheduledFuture<?> stabilizeFuture;
    private Stabilize stabilizeInstance;
    private ScheduledFuture<?> fixFingersFuture;
    private FixFingers fixFingersInstance;
    private final ClientComponent client;

    public Node(String myUsername, ClientComponent client) throws NoSuchAlgorithmException {
        this.client = client;
        this.myUsername = myUsername;
        this.myID = new BigInt(SecurityUtils.sha1Bytes(myUsername));
        this.ipPortToIDs = Collections.synchronizedMap(new HashMap<Pair<String, Integer>, BigInt>());
        this.idsToIpPort = Collections.synchronizedMap(new HashMap<BigInt, Pair<String, Integer>>());
        this.successorID = this.predecessorID = null;
        this.fingerList = Collections.synchronizedList(new ArrayList<BigInt>());

        Pair<String, Integer> pair = new Pair<String, Integer>(client.getIP(), client.getPort());
        this.ipPortToIDs.put(pair, myID);
        this.idsToIpPort.put(myID, pair);
    }

    public List<BigInt> getFingerList() {
        return this.fingerList;
    }

    public void setPredecessor(BigInt predecessorID, String predecessorIP, Integer port) {
        this.predecessorID = predecessorID;
        Pair<String, Integer> p = new Pair<String, Integer>(predecessorIP, port);
        this.ipPortToIDs.put(p, predecessorID);
        this.idsToIpPort.put(predecessorID, p);
    }

    public BigInt getPredecessorID() {
        return this.predecessorID;
    }

    public Pair<String, Integer> getPredecessorInfo() {
        return idsToIpPort.get(predecessorID);
    }

    public Integer getMyPort() {
        return client.getPort();
    }

    public void setSuccessor(BigInt successorID, String successorIP, Integer port) {
        this.successorID = successorID;
        Pair<String, Integer> p = new Pair<String, Integer>(successorIP, port);
        this.ipPortToIDs.put(p, successorID);
        this.idsToIpPort.put(successorID, p);
    }

    public BigInt getSuccessorID() {
        return this.successorID;
    }

    public Pair<String, Integer> getSuccessorInfo() {
        return this.idsToIpPort.get(successorID);
    }

    public BigInt getMyID() {
        return this.myID;
    }

    public String getMyUsername() {
        return this.myUsername;
    }

    public String getMyIP() {
        return this.client.getIP();
    }

    public void setFingers(List<BigInt> fingerList, List<Pair<String, Integer>> fingersInfo) {
        this.fingerList = fingerList;

        for (int i = 0; i < fingerList.size(); i++) {
            idsToIpPort.put(fingerList.get(i), fingersInfo.get(i));
            ipPortToIDs.put(fingersInfo.get(i), fingerList.get(i));
        }
    }

    public void sendMessage(ConnInfo connInfo) {
        this.client.sendMessage(connInfo);
    }

    public BigInt getFinger(int i) {
        return this.fingerList.get(i);
    }

    public void createChordRing() {
        Chord.create(this);
        createThreads();
    }

    public void notifyNode(BigInt possiblePredecessorID, String possiblePredecessorIP, Integer possiblePredecessorPort) {
        Chord.notifyNode(this, possiblePredecessorID, possiblePredecessorIP, possiblePredecessorPort);
    }

    public void join(BigInt successorID, String successorIP, Integer successorPort, BigInt predecessorID, String predecessorIP, Integer predecessorPort) {
        setPredecessor(predecessorID, predecessorIP, predecessorPort);
        setSuccessor(successorID, successorIP, successorPort);

        System.out.println("Join: SuccessorID = " + successorID.toHexString() + " IP = " + successorIP + " "
                + "PredecessorID = " + predecessorID.toHexString() + " IP = " + predecessorIP);

        if (successorID.equals(myID) && predecessorID.equals(myID)) {
            System.out.println("No Need to call join. I am alone");
        } else {
            if (successorID.equals(myID) || predecessorID.equals(myID)) {
                System.err.println("Something is wrong");
                return;
            }
            try {
                Chord.join(this, successorID, successorIP, successorPort, predecessorID, predecessorIP, predecessorPort);
            } catch (InterruptedException ex) {
                System.err.println(ex);
            }
        }
    }

    public BigInt findSuccessor(BigInt id) throws InterruptedException {
        return Chord.findSuccessor(this, id);
    }

    public void disconnect() {
        Chord.disconnect(this);
    }

    protected ClientComponent getClient() {
        return this.client;
    }

    private void createThreads() {
        scheduler = Executors.newScheduledThreadPool(2);
        fixFingersInstance = new FixFingers(this);
        stabilizeInstance = new Stabilize(this);
        fixFingersFuture = scheduler.scheduleAtFixedRate(fixFingersInstance, 15, 43, TimeUnit.SECONDS);
        stabilizeFuture = scheduler.scheduleAtFixedRate(stabilizeInstance, 50, 33, TimeUnit.SECONDS);
    }

    public FixFingers getFixFingersInstance() {
        return this.fixFingersInstance;
    }

    public Stabilize getStabilizeInstance() {
        return this.stabilizeInstance;
    }

    public void initializeFingerTable() {
        for (int i = 0; i < 32; i++) {
            this.fingerList.add(myID);
        }
    }

    public void insertToMap(BigInt userID, String dstIP, Integer port) {
        Pair<String, Integer> p = new Pair<String, Integer>(dstIP, port);
        this.ipPortToIDs.put(p, userID);
        this.idsToIpPort.put(userID, p);

    }

    public BigInt findFromIpPortToIDsMap(Pair<String, Integer> p) {
        return ipPortToIDs.get(p);
    }

    public Pair<String, Integer> findFromIDsToIPPortMap(BigInt id) {
        return idsToIpPort.get(id);
    }

    public void insertReplyFingers(ReplyFingers msg) {
        Chord.insertReplyFingers(msg);
    }

    public void insertReplySuccessor(ReplySuccessor msg) {
        Chord.insertReplySuccessor(msg);
    }

    public void insertFingerInfo(FingerInfo msg) {
        Chord.insertFingerInfo(msg);
    }

    public FingerInfo findClosestPrecedingFinger(byte[] id) {
        BigInt finger = Chord.closestPrecedingFinger(this, new BigInt(id));
        Pair<String, Integer> info = idsToIpPort.get(finger);
        return new FingerInfo(finger.getBytes(), info.getFirst(), info.getSecond());
    }

}
