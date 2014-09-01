package edu.ucr.cs.mobilechord.transportapi.messages;

import java.nio.ByteBuffer;

public class TextMessage implements Message {
    
    private String textMsg;
    
    private TextMessage() {
        
    }
   
    public TextMessage(String textMsg) {
        this.textMsg = textMsg;
    }
    
    @Override
    public byte[] toByteArray() {
        byte[] textMsgBytes = textMsg.getBytes();
        ByteBuffer buffer = ByteBuffer.allocate(1 + textMsgBytes.length);
        
        buffer.put(MessageTypes.TEXT_MESSAGE);
        buffer.put(textMsgBytes);
        
        return buffer.array();
    }

    @Override
    public void fromByteArray(byte[] array) {
        ByteBuffer buffer = ByteBuffer.wrap(array);
        buffer.get();
        
        byte[] textMsgBytes = new byte[array.length - 1];
        buffer.get(textMsgBytes);
        textMsg = new String(textMsgBytes);
        
    }
    
     public static TextMessage parseArray(byte[] array) {
        if (array == null || array.length == 0) {
            return null;
        }
        
        TextMessage msg = new TextMessage();
       msg.fromByteArray(array);
        return msg;
    }
    
    public String getTextMsg() {
        return this.textMsg;
    }
    
    @Override
    public byte getMessageType() {
        return MessageTypes.TEXT_MESSAGE;
    }
}   
