package messages.chord;

import java.nio.ByteBuffer;
import messages.MessageTypes;

public class SuccessorNotification extends ChordMessage{
    private byte[] successorID;
    private String successorIP;
    private Integer successorPort;
    
    protected SuccessorNotification() {
        super(ChordMessage.SUCCESSOR_NOTIFICATION);
    }
    
    public SuccessorNotification(byte[] successorID, String successorIP, Integer port) {
        super(ChordMessage.SUCCESSOR_NOTIFICATION);
        this.successorID = successorID;
        this.successorIP = successorIP;
        this.successorPort = port;
    }
    
    public byte[] getSuccesorID() {
        return this.successorID;
    }
    
    public String getSuccessorIP() {
        return this.successorIP;
    }
    
    public Integer getSuccessorPort() {
        return this.successorPort;
    }

    @Override
    public byte[] toByteArray() {
        byte[] ipBytes = successorIP.getBytes();
        ByteBuffer buffer = ByteBuffer.allocate(2 + 12 + ipBytes.length + successorID.length);
        buffer.put(MessageTypes.CHORD_MESSAGE);
        buffer.put(super.getChordMessageType());
        buffer.putInt(successorPort);
        buffer.putInt(successorID.length);        
        buffer.put(successorID);
        buffer.putInt(ipBytes.length);
        buffer.put(ipBytes);
        
        return buffer.array();
    }

    @Override
    public void fromByteArray(byte[] array) {
        ByteBuffer buffer = ByteBuffer.wrap(array);
        buffer.get();
        buffer.get();
        
        successorPort = buffer.getInt();
        int succIDLen = buffer.getInt();
        successorID = new byte[succIDLen];
        buffer.get(successorID);
        
        int ipLen = buffer.getInt();
        byte[] sucIPBytes = new byte[ipLen];
        buffer.get(sucIPBytes);
        successorIP = new String(sucIPBytes);
    }
    
    public static SuccessorNotification parseArray(byte[] array) {
        if (array == null || array.length == 0) {
            return null;
        }

       SuccessorNotification msg = new SuccessorNotification();
        msg.fromByteArray(array);
        return msg;
    }
    
    @Override
    public byte getMessageType() {
        return MessageTypes.CHORD_MESSAGE;
    }
}

