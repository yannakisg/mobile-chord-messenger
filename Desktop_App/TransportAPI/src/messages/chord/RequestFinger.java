package messages.chord;

import java.nio.ByteBuffer;
import messages.MessageTypes;

public class RequestFinger extends ChordMessage {
    private String myIP;
    private Integer myPort;
    private byte[] id;
    
    protected RequestFinger() {
        super(ChordMessage.REQUEST_FINGER);
    }
    
     public RequestFinger(String myIP, Integer myPort, byte[] id) {
        super(ChordMessage.REQUEST_FINGER);
        this.myIP = myIP;
        this.myPort = myPort;
        this.id = id;
    }
     
     public byte[] getID() {
         return this.id;
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
        ByteBuffer buffer = ByteBuffer.allocate(14 + ipB.length + id.length);
        buffer.put(MessageTypes.CHORD_MESSAGE);
        buffer.put(ChordMessage.REQUEST_FINGER);
        
        buffer.putInt(ipB.length);
        buffer.put(ipB);
        buffer.putInt(myPort);
        
        buffer.putInt(id.length);
        buffer.put(id);
        
        return buffer.array();
    }

    @Override
    public void fromByteArray(byte[] array) {
        ByteBuffer buffer = ByteBuffer.wrap(array);
        buffer.get();
        buffer.get();
        
        int bIPL = buffer.getInt();
        byte[] ipB = new byte[bIPL];
        buffer.get(ipB);
        myIP = new String(ipB);
        
        myPort = buffer.getInt();
        
        int idL = buffer.getInt();
        id = new byte[idL];
        buffer.get(id);
    }
    
    public static RequestFinger parseArray(byte[] array) {
        if (array == null || array.length == 0) {
            return null;
        }

        RequestFinger msg = new RequestFinger();
        msg.fromByteArray(array);
        return msg;
    }
    
    @Override
    public byte getMessageType() {
        return MessageTypes.CHORD_MESSAGE;
    }
    
}
