package messages.chord;

import java.nio.ByteBuffer;
import messages.MessageTypes;

public class FingerInfo extends ChordMessage {
    private byte[] fingerID;
    private String fingerIP;
    private Integer fingerPort;
    
    protected FingerInfo() {
        super(ChordMessage.FINGER_INFO);
    }
    
    public FingerInfo( byte[] fingerID, String fingerIP, Integer fingerPort) {
        super(ChordMessage.FINGER_INFO);
        this.fingerID = fingerID;
        this.fingerIP = fingerIP;
        this.fingerPort = fingerPort;
    }
    
    public byte[] getFingerID() {
        return this.fingerID;
    }
    
    public String getFingerIP() {
        return this.fingerIP;
    }
    
    public Integer getFingerPort() {
        return this.fingerPort;
    }
    
    @Override
    public byte[] toByteArray() {
        byte[] fingerIPB = fingerIP.getBytes();
        ByteBuffer buffer = ByteBuffer.allocate(2 + 12 + fingerID.length + fingerIPB.length );
        
        buffer.put(MessageTypes.CHORD_MESSAGE);
        buffer.put(ChordMessage.FINGER_INFO);
        
        buffer.putInt(fingerID.length);
        buffer.put(fingerID);
        buffer.putInt(fingerIPB.length);
        buffer.put(fingerIPB);
        buffer.putInt(fingerPort);
        
        return buffer.array();
    }

    @Override
    public void fromByteArray(byte[] array) {
        ByteBuffer buffer = ByteBuffer.wrap(array);
        buffer.get();
        buffer.get();
        
        int fingerIDL = buffer.getInt();
        fingerID = new byte[fingerIDL];
        buffer.get(fingerID);
        int fingerIPL = buffer.getInt();
        byte[] fingerIPB = new byte[fingerIPL];
        buffer.get(fingerIPB);
        fingerIP = new String(fingerIPB);
        
        fingerPort = buffer.getInt();
    }
    
    public static FingerInfo parseArray(byte[] array) {
        if (array == null || array.length == 0) {
            return null;
        }

         FingerInfo msg = new FingerInfo();
        msg.fromByteArray(array);
        return msg;
    }
    
    @Override
    public byte getMessageType() {
        return MessageTypes.CHORD_MESSAGE;
    }
}
