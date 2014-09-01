package edu.ucr.cs.mobilechord.transportapi.messages;

import java.nio.ByteBuffer;

public class SuccessorPredecessorInfo implements Message {
    
    private String ipSuccessor;
    private String ipPredecessor;
    private Integer portSuccessor;
    private Integer portPredecessor;
    private byte[] successorID;
    private byte[] predecessorID;
    
    private SuccessorPredecessorInfo() {
        
    }
    
    public SuccessorPredecessorInfo(String ipSuccessor, Integer portSuccessor, byte[] successorID, String ipPredecessor, Integer portPredecessor, byte[] predecessorID) {
        this.ipSuccessor = ipSuccessor;
        this.portSuccessor = portSuccessor;
        this.ipPredecessor = ipPredecessor;
        this.portPredecessor = portPredecessor;
        this.predecessorID = predecessorID;
        this.successorID = successorID;
    }
    
    @Override
    public byte[] toByteArray() {
        byte[] ipSucBytes = ipSuccessor.getBytes();
        byte[] ipPredBytes = ipPredecessor.getBytes();
        ByteBuffer buffer = ByteBuffer.allocate(25 + ipSucBytes.length + ipPredBytes.length + predecessorID.length + successorID.length);
        
        buffer.put(MessageTypes.SUCCESSOR_PREDECESSOR_INFO);
        buffer.putInt(ipSucBytes.length);
        buffer.put(ipSucBytes);
        buffer.putInt(portSuccessor);
        buffer.putInt(successorID.length);
        buffer.put(successorID);
        buffer.putInt(ipPredBytes.length);
        buffer.put(ipPredBytes);
        buffer.putInt(portPredecessor);
        buffer.putInt(predecessorID.length);
        buffer.put(predecessorID);
        
        return buffer.array();
    }

    @Override
    public void fromByteArray(byte[] array) {
        ByteBuffer buffer = ByteBuffer.wrap(array);
        buffer.get();
        
        int ipSucLen = buffer.getInt();
        byte[] ipSucBytes = new byte[ipSucLen];
        buffer.get(ipSucBytes);
        portSuccessor = buffer.getInt();
        
        int sucIDLen = buffer.getInt();
        successorID = new byte[sucIDLen];
        buffer.get(successorID);
        
        int ipPredLen = buffer.getInt();
        byte[] ipPredBytes = new byte[ipPredLen];
        buffer.get(ipPredBytes);
        
        portPredecessor = buffer.getInt();
        
        int predIDLen = buffer.getInt();
        predecessorID = new byte[predIDLen];
        buffer.get(predecessorID);
        
        this.ipSuccessor = new String(ipSucBytes);
        this.ipPredecessor = new String(ipPredBytes);
    }
    
     public static SuccessorPredecessorInfo parseArray(byte[] array) {
        if (array == null || array.length == 0) {
            return null;
        }
        
        SuccessorPredecessorInfo msg = new SuccessorPredecessorInfo();
        msg.fromByteArray(array);
        return msg;
    }
     
    public Integer getSuccessorPort() {
        return this.portSuccessor;
    }
    
    public Integer getPredecessorPort() {
        return this.portPredecessor;
    }
    
    public byte[] getSuccessorID() {
        return this.successorID;
    }
    
    public byte[] getPredecessorID() {
        return this.predecessorID;
    }
     
    public String getSuccessorIP() { 
        return this.ipSuccessor;
    }
    
    public String getPredecessorIP() {
        return this.ipPredecessor;
    }
    
    @Override
    public byte getMessageType() {
        return MessageTypes.SUCCESSOR_PREDECESSOR_INFO;
    }
}
