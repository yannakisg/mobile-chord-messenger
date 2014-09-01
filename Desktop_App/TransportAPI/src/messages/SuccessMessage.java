package messages;

public class SuccessMessage implements Message {
    
    public static final byte REGISTER_SUCCESSFUL = 0;
    public static final byte LOGIN_SUCCESSFUL = 1;
    public static final byte TEXT_RECEIVED = 2;
    
    
    private byte msgType;
    
    private SuccessMessage() {
        
    }
    
    public SuccessMessage(byte msgType) {
        this.msgType = msgType;
    }
    
    @Override
    public byte[] toByteArray() {
        byte[] array = new byte[2];
        array[0] = MessageTypes.SUCCESS_MESSAGE;
        array[1] = msgType;
        return array;
    }

    @Override
    public void fromByteArray(byte[] array) {
        msgType = array[1];
    }
    
    public byte getSuccessMessageType() {
        return this.msgType;
    }
    
    public static SuccessMessage parseArray(byte[] array) {
        if (array == null || array.length == 0) {
            return null;
        }
        
        SuccessMessage msg = new SuccessMessage();
       msg.fromByteArray(array);
        return msg;
    }
    
    @Override
    public byte getMessageType() {
        return MessageTypes.SUCCESS_MESSAGE;
    }
}
