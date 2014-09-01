package messages;

import java.nio.ByteBuffer;
import org.apache.commons.codec.binary.Base64;
import security.SecurityUtils;

public class LoginMessage implements Message {
    private String username;
    private String password;
    private String ip;
    private Integer port;
    
    private LoginMessage() {
        
    }
    
    public LoginMessage(String username, String password, String ip, Integer port) {
        this.username = username;
        this.password = password;
        this.ip = ip;
        this.port = port;
    }
    
    @Override
    public byte[] toByteArray() {
        // username -> SHA-1(username) -> 160 bits = 20 bytes
        // password -> eSHA-256(password) -> 256 bits = 32 bytes
        try {
            byte[] ipB = ip.getBytes();
            byte[] bUsername = SecurityUtils.sha1Bytes(username);
            byte[] bPassword = SecurityUtils.sha256Bytes(password);
            ByteBuffer byteBuffer = ByteBuffer.allocate(53 + 8 + ipB.length);
            
            byteBuffer.put(MessageTypes.LOGIN_MESSAGE_TYPE);       
            byteBuffer.putInt(ipB.length);
            byteBuffer.put(ipB);
            byteBuffer.putInt(port);
            byteBuffer.put(bUsername);
            byteBuffer.put(bPassword);
            return byteBuffer.array();
        } catch (Exception ex) {
            System.err.println("Exception: " + ex.getMessage());
            return null;
        }
    }

    @Override
    public void fromByteArray(byte[] array) {
        ByteBuffer byteBuffer = ByteBuffer.wrap(array);
        byte[] bUsername = new byte[20];
        byte[] bPassword = new byte[32];
        
        byteBuffer.get();
        
        int ipBL = byteBuffer.getInt();
        byte[] ipBytes = new byte[ipBL];
        byteBuffer.get(ipBytes);
        ip = new String(ipBytes);
        port = byteBuffer.getInt();        
        
        byteBuffer.get(bUsername);
        byteBuffer.get(bPassword);
        
        this.username = Base64.encodeBase64String(bUsername);
        this.password = Base64.encodeBase64String(bPassword);
    }
    
    public String getIP() {
        return this.ip;
    }
    
    public Integer getPort() {
        return this.port;
    }
    
     public static LoginMessage parseArray(byte[] array) {
        if (array == null || array.length == 0) {
            return null;
        }
        
        LoginMessage msg = new LoginMessage();
       msg.fromByteArray(array);
        return msg;
    }
    
    public String getUsername() { 
        return this.username;
    }
    
    public String getPassword() {
        return this.password;
    }
    
    @Override
    public byte getMessageType() {
        return MessageTypes.LOGIN_MESSAGE_TYPE;
    }
}
