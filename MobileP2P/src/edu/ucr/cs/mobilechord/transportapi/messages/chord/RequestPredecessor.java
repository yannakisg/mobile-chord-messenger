package edu.ucr.cs.mobilechord.transportapi.messages.chord;

import java.nio.ByteBuffer;

import edu.ucr.cs.mobilechord.transportapi.messages.MessageTypes;

public class RequestPredecessor extends ChordMessage {    
    private String myIP;
    private Integer myPort;
    private byte source;
    
    public static final byte STABILIZE = 0;
    public static final byte TEXT_APPLICATION = 1;
    
    protected RequestPredecessor() {
        super(ChordMessage.REQUEST_PREDECESSOR);
    }

    public RequestPredecessor(String myIP, Integer myPort, byte source) {
        super(ChordMessage.REQUEST_PREDECESSOR);
        this.myIP = myIP;
        this.myPort = myPort;
        this.source = source;
    }
    
    public byte getSource() {
        return this.source;
    }
    
    public String getMyIP() {
        return this.myIP;
    }
    
    public Integer getMyPort() {
        return this.myPort;
    }
    
    @Override
    public byte[] toByteArray() {
        byte[] ipB = myIP.getBytes();
        ByteBuffer buffer = ByteBuffer.allocate(11 + ipB.length);
        buffer.put(MessageTypes.CHORD_MESSAGE);
        buffer.put(super.getChordMessageType());
        buffer.put(source);
        
        buffer.putInt(ipB.length);
        buffer.put(ipB);
        buffer.putInt(myPort);
        
        return buffer.array();
    }

    @Override
    public void fromByteArray(byte[] array) {
        ByteBuffer buffer = ByteBuffer.wrap(array);
        buffer.get();
        buffer.get();
        
        source = buffer.get();
        
        int bIPL = buffer.getInt();
        byte[] ipB = new byte[bIPL];
        buffer.get(ipB);
        myIP = new String(ipB);
        
        myPort = buffer.getInt();
    }

    public static RequestPredecessor parseArray(byte[] array) {
        if (array == null || array.length == 0) {
            return null;
        }

        RequestPredecessor msg = new RequestPredecessor();
        msg.fromByteArray(array);
        return msg;
    }
    
    @Override
    public byte getMessageType() {
        return MessageTypes.CHORD_MESSAGE;
    }
}
