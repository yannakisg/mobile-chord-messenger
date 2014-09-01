package edu.ucr.cs.mobilechord.transportapi.messages;

import java.nio.ByteBuffer;

public class EncryptedMessage implements Message {
    
    private byte[] encrBytes;
    private byte[] userID;
    
    private EncryptedMessage() {
        
    }
    
    public EncryptedMessage(byte[] userID, byte[] encrBytes) {
        this.userID = userID;
        this.encrBytes = encrBytes;
    }
    
    @Override
    public byte getMessageType() {
        return MessageTypes.ENCRYPTED_MESSAGE;
    }
    
    public byte[] getEncryptedBytes() {
        return this.encrBytes;
    }
    
    public byte[] getUserID() {
        return this.userID;
    }
    
    @Override
    public byte[] toByteArray() {
       ByteBuffer buffer = ByteBuffer.allocate(1 + 8 + encrBytes.length + userID.length);
       buffer.put(MessageTypes.ENCRYPTED_MESSAGE);
       buffer.putInt(userID.length);
       buffer.put(userID);
       buffer.putInt(encrBytes.length);
       buffer.put(encrBytes);
       
       return buffer.array();
    }

    @Override
    public void fromByteArray(byte[] array) {
       ByteBuffer buffer = ByteBuffer.wrap(array);
       buffer.get();
       
       int userIDL = buffer.getInt();
       userID = new byte[userIDL];
       buffer.get(userID);
       
       int encrBytesL = buffer.getInt();
       encrBytes = new byte[encrBytesL];
       buffer.get(encrBytes);
    }
    
    public static EncryptedMessage parseArray(byte[] array) {
        if (array == null || array.length == 0) {
            return null;
        }
        
        EncryptedMessage msg = new EncryptedMessage();
        msg.fromByteArray(array);
        return msg;
    }
}
