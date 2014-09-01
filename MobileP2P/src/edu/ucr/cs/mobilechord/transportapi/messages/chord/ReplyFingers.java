package edu.ucr.cs.mobilechord.transportapi.messages.chord;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

import edu.ucr.cs.mobilechord.transportapi.messages.MessageTypes;
import edu.ucr.cs.mobilechord.transportapi.utilities.Pair;

public class ReplyFingers extends ChordMessage {

    private List<byte[]> fingerList;
    private List<Pair<String, Integer>> fingersInfo;
    
    protected ReplyFingers() {
        super(ChordMessage.REPLY_FINGERS);
        this.fingerList = new ArrayList<byte[]>();
        this.fingersInfo = new ArrayList<Pair<String, Integer>>();
    }

    public ReplyFingers(List<byte[]> fingerList, List<Pair<String, Integer>> fingersInfo) {
        super(ChordMessage.REPLY_FINGERS);
        this.fingerList = fingerList;
        this.fingersInfo = fingersInfo;
    }
    
    public List<byte[]> getFingerList() {
        return this.fingerList;
    }
    
    public List<Pair<String, Integer>> getFingersInfo() {
        return this.fingersInfo;
    }

    @Override
    public byte[] toByteArray() {
        int length = 6;
        for (byte[] b : fingerList) {
            length += b.length + 4;
        }
        for (Pair<String, Integer> p : fingersInfo) {
            length += p.getFirst().getBytes().length + 8;
        }

        ByteBuffer buffer = ByteBuffer.allocate(length);
        buffer.put(MessageTypes.CHORD_MESSAGE);
        buffer.put(ChordMessage.REPLY_FINGERS);
        buffer.putInt(fingerList.size());
        for (byte[] b : fingerList) {
            buffer.putInt(b.length);
            buffer.put(b);
        }
        for (Pair<String, Integer> p : fingersInfo) {
            byte[] b = p.getFirst().getBytes();
            buffer.putInt(b.length);
            buffer.put(b);
            buffer.putInt(p.getSecond());
        }

        return buffer.array();
    }

    @Override
    public void fromByteArray(byte[] array) {
        ByteBuffer buffer = ByteBuffer.wrap(array);
        buffer.get();
        buffer.get();
        
        int total = buffer.getInt();
        for (int i = 0; i < total; i++) {
            byte[] b = new byte[buffer.getInt()];
            buffer.get(b);
            fingerList.add(b);
        }
        
        for (int i = 0; i < total; i++) {
            byte[] b = new byte[buffer.getInt()];
            buffer.get(b);
            String ip = new String(b);
            Integer port = buffer.getInt();
            Pair<String, Integer> p = new Pair(ip, port);
            fingersInfo.add(p);
        }
    }

    public static ReplyFingers parseArray(byte[] array) {
        if (array == null || array.length == 0) {
            return null;
        }

        ReplyFingers msg = new ReplyFingers();
        msg.fromByteArray(array);
        return msg;
    }
    
    @Override
    public byte getMessageType() {
        return MessageTypes.CHORD_MESSAGE;
    }
}
