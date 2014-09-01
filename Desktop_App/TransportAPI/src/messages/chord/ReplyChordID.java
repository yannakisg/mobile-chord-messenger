package messages.chord;

import java.nio.ByteBuffer;
import messages.MessageTypes;

public class ReplyChordID extends ChordMessage{
    private String myID;
    
    protected ReplyChordID() {
        super(ChordMessage.REPLY_CHORD_ID);
    }
    
    public ReplyChordID(String myID) {
        super(ChordMessage.REPLY_CHORD_ID);
        this.myID = myID;
    }
    
    @Override
    public byte[] toByteArray() {
        byte[] strBytes = this.myID.getBytes();
        ByteBuffer buffer = ByteBuffer.allocate(2 + strBytes.length);
        buffer.put(MessageTypes.CHORD_MESSAGE);
        buffer.put(ChordMessage.REPLY_CHORD_ID);
        buffer.put(strBytes);
        
        return buffer.array();
    }

    @Override
    public void fromByteArray(byte[] array) {
        ByteBuffer buffer = ByteBuffer.wrap(array);
        buffer.get();
        buffer.get();
        
        byte[] strBytes = new byte[array.length - 2];
        buffer.get(strBytes);
        
        this.myID = new String(strBytes);
    }
    
    public String getID() {
        return this.myID;
    }
    
    public static ReplyChordID parseArray(byte[] array) {
        if (array == null || array.length == 0) {
            return null;
        }
        
        ReplyChordID msg = new ReplyChordID();
        msg.fromByteArray(array);
        return msg;
    }
    
    @Override
    public byte getMessageType() {
        return MessageTypes.CHORD_MESSAGE;
    }
}
