package messages.chord;

import java.nio.ByteBuffer;
import messages.MessageTypes;

public class RequestSuccessor extends ChordMessage {    
    private String myIP;
    private Integer myPort;
    
    protected RequestSuccessor() {
        super(ChordMessage.REQUEST_SUCCESSOR);
    }

    public RequestSuccessor(String myIP, Integer myPort) {
        super(ChordMessage.REQUEST_SUCCESSOR);
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
        byte[] ipB = myIP.getBytes();
        ByteBuffer buffer = ByteBuffer.allocate(10 + ipB.length);
        buffer.put(MessageTypes.CHORD_MESSAGE);
        buffer.put(super.getChordMessageType());
        
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
        
        int bIPL = buffer.getInt();
        byte[] ipB = new byte[bIPL];
        buffer.get(ipB);
        myIP = new String(ipB);
        
        myPort = buffer.getInt();
    }

    public static RequestSuccessor parseArray(byte[] array) {
        if (array == null || array.length == 0) {
            return null;
        }

        RequestSuccessor msg = new RequestSuccessor();
        msg.fromByteArray(array);
        return msg;
    }
    
    @Override
    public byte getMessageType() {
        return MessageTypes.CHORD_MESSAGE;
    }
}
