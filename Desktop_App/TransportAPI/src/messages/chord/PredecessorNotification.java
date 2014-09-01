package messages.chord;

import java.nio.ByteBuffer;
import messages.MessageTypes;

public class PredecessorNotification extends ChordMessage{
    private byte[] predecessorID;
    private String predecessorIP;
    private Integer predecessorPort;
    
    protected PredecessorNotification() {
        super(ChordMessage.PREDECESSOR_NOTIFICATION);
    }
    
    public PredecessorNotification(byte[] predecessorID, String predecessorIP, Integer predecessorPort) {
        super(ChordMessage.PREDECESSOR_NOTIFICATION);
        this.predecessorID = predecessorID;
        this.predecessorIP = predecessorIP;
        this.predecessorPort = predecessorPort;
    }
    
    public byte[] getPredecessorID() {
        return this.predecessorID;
    }
    
    public String getPredecessorIP() {
        return this.predecessorIP;
    }
    
    public Integer getPredecessorPort() {
        return this.predecessorPort;
    }

    @Override
    public byte[] toByteArray() {
        byte[] ipBytes = predecessorIP.getBytes();
        ByteBuffer buffer = ByteBuffer.allocate(2 + 12 + ipBytes.length + predecessorID.length);
        buffer.put(MessageTypes.CHORD_MESSAGE);
        buffer.put(super.getChordMessageType());
        
        buffer.putInt(predecessorID.length);
        buffer.put(predecessorID);
        buffer.putInt(ipBytes.length);
        buffer.put(ipBytes);        
        buffer.putInt(predecessorPort);
        
        return buffer.array();
    }

    @Override
    public void fromByteArray(byte[] array) {
        ByteBuffer buffer = ByteBuffer.wrap(array);
        buffer.get();
        buffer.get();
        
        int predIDLen = buffer.getInt();
        predecessorID = new byte[predIDLen];
        buffer.get(predecessorID);
        
        int ipLen = buffer.getInt();
        byte[] ipBytes = new byte[ipLen];
        buffer.get(ipBytes);
        predecessorIP = new String(ipBytes);
        
        predecessorPort = buffer.getInt();
    }
    
    public static PredecessorNotification parseArray(byte[] array) {
        if (array == null || array.length == 0) {
            return null;
        }

        PredecessorNotification msg = new PredecessorNotification();
        msg.fromByteArray(array);
        return msg;
    }
    
    @Override
    public byte getMessageType() {
        return MessageTypes.CHORD_MESSAGE;
    }
}
