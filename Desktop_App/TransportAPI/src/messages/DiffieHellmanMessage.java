package messages;

import dhash.BigInt;
import java.math.BigInteger;
import java.nio.ByteBuffer;

public class DiffieHellmanMessage implements Message {
    private BigInt id;
    private BigInteger g;
    private BigInteger p;
    private int l;
    private byte[] encPubKey;
    private String ip;
    private Integer port;
    
    private  DiffieHellmanMessage() {
        
    }
    
    public DiffieHellmanMessage(String ip, Integer port, BigInt id, byte[] encPubKey, BigInteger p, BigInteger g, int l) {        
        this.ip = ip;
        this.port = port;
        this.id = id;
        this.encPubKey = encPubKey;
        this.p = p;
        this.g = g;
        this.l = l;
    }
    
    public String getIP() {
        return this.ip;
    }
    
    public Integer getPort() {
        return this.port;
    }
    
    public BigInt getID() {
        return this.id;
    }
    
    public byte[] getEncodedPublicKey() {
        return this.encPubKey;
    }
    
    public BigInteger getP() {
        return this.p;
    }
    
    public BigInteger getG() {
        return this.g;
    }
    
    public int getL() {
        return this.l;
    }

    @Override
    public byte[] toByteArray() {
       byte[] bG = g.toByteArray();
       byte[] bP = p.toByteArray();
       byte[] bUsername = id.getBytes();
       byte[] bIP = ip.getBytes();
       ByteBuffer buffer = ByteBuffer.allocate(29 + bG.length + bP.length + encPubKey.length + bUsername.length + bIP.length);
       
       buffer.put(MessageTypes.DIFFIE_HELLMAN_MESSAGE);
       buffer.putInt(bIP.length);
       buffer.put(bIP);
       buffer.putInt(port);
       buffer.putInt(bUsername.length);
       buffer.put(bUsername);
       buffer.putInt(encPubKey.length);
       buffer.put(encPubKey);
       buffer.putInt(bG.length);
       buffer.put(bG);
       buffer.putInt(bP.length);
       buffer.put(bP);
       buffer.putInt(l);
       
       return buffer.array();
    }

    @Override
    public void fromByteArray(byte[] array) {
        ByteBuffer buffer = ByteBuffer.wrap(array);
        buffer.get();
        
        int ipLen = buffer.getInt();
        byte[] bIP = new byte[ipLen];
        buffer.get(bIP);
        ip = new String(bIP);
        port = buffer.getInt();
        
        int usernameLen = buffer.getInt();
        byte[] bUsername = new byte[usernameLen];
        buffer.get(bUsername);
        id = new BigInt(bUsername);
        
        int lEncPubKey = buffer.getInt();
        encPubKey = new byte[lEncPubKey];
        buffer.get(encPubKey);
        
        int lbG = buffer.getInt();
        byte[] bG = new byte[lbG];
        buffer.get(bG);
        
        int lbP = buffer.getInt();
        byte[] bP = new byte[lbP];
        buffer.get(bP);
        
        l = buffer.getInt();
        g = new BigInteger(bG);
        p = new BigInteger(bP);
    }
    
    public static DiffieHellmanMessage parseArray(byte[] array) {
        if (array == null || array.length == 0) {
            return null;
        }
        
        DiffieHellmanMessage msg = new DiffieHellmanMessage();
       msg.fromByteArray(array);
        return msg;
    }
    
    @Override
    public byte getMessageType() {
        return MessageTypes.DIFFIE_HELLMAN_MESSAGE;
    }
}
