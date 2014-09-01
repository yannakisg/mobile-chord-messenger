package edu.ucr.cs.mobilechord.transportapi.messages.chord;

import java.nio.ByteBuffer;

import edu.ucr.cs.mobilechord.transportapi.messages.MessageTypes;

public class RequestChordID extends ChordMessage {
    private String myID;
    private String myIP;
    private Integer myPort;
    
    protected RequestChordID() {
        super(ChordMessage.REQUEST_CHORD_ID);
    }
    
    public RequestChordID(String myID, String myIP, Integer myPort) {
        super(ChordMessage.REQUEST_CHORD_ID);
        this.myID = myID;
        this.myIP = myIP;
        this.myPort = myPort;
    }
    
    public String getMyIP() {
        return this.myIP;
    }
    
    public Integer getMyPort() {
        return this.myPort;
    }
    
    @Override
    public byte[] toByteArray() {
        byte[] strBytes = this.myID.getBytes();
        byte[] ipBytes = this.myIP.getBytes();
        ByteBuffer buffer = ByteBuffer.allocate(14 + strBytes.length + ipBytes.length);
        buffer.put(MessageTypes.CHORD_MESSAGE);
        buffer.put(ChordMessage.REQUEST_CHORD_ID);
        
        buffer.putInt(strBytes.length);
        buffer.put(strBytes);
        buffer.putInt(ipBytes.length);
        buffer.put(ipBytes);
        buffer.putInt(myPort);
        
        return buffer.array();
    }

    @Override
    public void fromByteArray(byte[] array) {
        ByteBuffer buffer = ByteBuffer.wrap(array);
        buffer.get();
        buffer.get();
        
        int strBytesL = buffer.getInt();
        byte[] strBytes = new byte[strBytesL];
        buffer.get(strBytes);
        this.myID = new String(strBytes);
        
        int ipBytesL = buffer.getInt();
        byte[] ipBytes = new byte[ipBytesL];
        buffer.get(ipBytes);
        this.myIP = new String(ipBytes);
        
        this.myPort = buffer.getInt();
    }
    
    public String getID() {
        return this.myID;
    }
    
    public static RequestChordID parseArray(byte[] array) {
        if (array == null || array.length == 0) {
            return null;
        }
        
        RequestChordID msg = new RequestChordID();
        msg.fromByteArray(array);
        return msg;
    }
    
    @Override
    public byte getMessageType() {
        return MessageTypes.CHORD_MESSAGE;
    }
}
