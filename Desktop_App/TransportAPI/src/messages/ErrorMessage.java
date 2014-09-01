package messages;

import java.nio.ByteBuffer;

public class ErrorMessage implements Message {
    
    public static final byte ERROR_NO_USER = 0;
    public static final byte ERROR_INVALID_PASSWORD = 1;
    public static final byte ERROR_USERID_EXISTS = 2;
    public static final byte ERROR_TRY_AGAIN = 3;
    
    private byte errorByte;
    private String errorStr;
    
    private ErrorMessage() {
        
    }
    
    public ErrorMessage(byte errorType, String errorStr) {
        this.errorByte = errorType;
        this.errorStr = errorStr;
    }
    
    @Override
    public byte[] toByteArray() {
        byte[] errorStrBytes = errorStr.getBytes();
        ByteBuffer buffer = ByteBuffer.allocate(2 + errorStrBytes.length);
        
        buffer.put(MessageTypes.ERROR_MESSAGE);
        buffer.put(errorByte);
        buffer.put(errorStrBytes);
        
        return buffer.array();
    }

    @Override
    public void fromByteArray(byte[] array) {
        ByteBuffer buffer = ByteBuffer.wrap(array);
        buffer.get();
        
        errorByte = buffer.get();
        byte[] errorStrBytes = new byte[array.length - 2];
        buffer.get(errorStrBytes);
        
        errorStr = new String(errorStrBytes);
        
    }
    
    public static ErrorMessage parseArray(byte[] array) {
        if (array == null || array.length == 0) {
            return null;
        }
        
        ErrorMessage msg = new ErrorMessage();
        msg.fromByteArray(array);
        return msg;
    }
    
    public byte getErrorType() { 
        return this.errorByte;
    }
    
    public String getErrorMsgStr() {
        return this.errorStr;
    }

    @Override
    public byte getMessageType() {
        return MessageTypes.ERROR_MESSAGE;
    }
}
