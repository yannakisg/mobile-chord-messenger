package messages;

public interface Message {
    public byte getMessageType();
    
    public byte[] toByteArray();
    
    public void fromByteArray(byte[] array);
    
    @Override
    public String toString();
}
