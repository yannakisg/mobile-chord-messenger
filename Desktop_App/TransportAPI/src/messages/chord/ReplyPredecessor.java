
package messages.chord;

import java.nio.ByteBuffer;
import messages.MessageTypes;

public class ReplyPredecessor extends ChordMessage {

    private byte[] predecessorID;
    private String predecessorIP;
    private Integer predecessorPort;
    private byte source;
    
    protected ReplyPredecessor() {
        super(ChordMessage.REPLY_PREDECESSOR);
    }
    
    public ReplyPredecessor(byte[] predecessorID, String predecessorIP, Integer port, byte source) {
        super(ChordMessage.REPLY_PREDECESSOR);
        this.predecessorID = predecessorID;
        this.predecessorIP = predecessorIP;
        this.predecessorPort = port;
        this.source = source;
    }
    
    public byte getSource() {
        return this.source;
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
        ByteBuffer buffer = ByteBuffer.allocate(3 + 12 + ipBytes.length + predecessorID.length);
        buffer.put(MessageTypes.CHORD_MESSAGE);
        buffer.put(super.getChordMessageType());
        buffer.put(source);
        
        buffer.putInt(predecessorPort);
        buffer.putInt(predecessorID.length);        
        buffer.put(predecessorID);
        buffer.putInt(ipBytes.length);
        buffer.put(ipBytes);
        
        return buffer.array();
    }

    @Override
    public void fromByteArray(byte[] array) {
        ByteBuffer buffer = ByteBuffer.wrap(array);
        buffer.get();
        buffer.get();
        
        source = buffer.get();
        
        predecessorPort = buffer.getInt();
        int succIDLen = buffer.getInt();
        predecessorID = new byte[succIDLen];
        buffer.get(predecessorID);
        
        int ipLen = buffer.getInt();
        byte[] sucIPBytes = new byte[ipLen];
        buffer.get(sucIPBytes);
        predecessorIP = new String(sucIPBytes);
    }
    
    public static ReplyPredecessor parseArray(byte[] array) {
        if (array == null || array.length == 0) {
            return null;
        }

       ReplyPredecessor msg = new ReplyPredecessor();
        msg.fromByteArray(array);
        return msg;
    }
    
    @Override
    public byte getMessageType() {
        return MessageTypes.CHORD_MESSAGE;
    }
    
}
