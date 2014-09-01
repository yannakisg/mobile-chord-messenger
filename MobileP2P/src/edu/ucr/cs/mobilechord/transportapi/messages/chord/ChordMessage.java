package edu.ucr.cs.mobilechord.transportapi.messages.chord;

import edu.ucr.cs.mobilechord.transportapi.messages.Message;





public abstract class ChordMessage implements Message {
    public static final byte PREDECESSOR_NOTIFICATION = 0;
    public static final byte SUCCESSOR_NOTIFICATION = 1;
    public static final byte REQUEST_CHORD_ID = 2;
    public static final byte REPLY_CHORD_ID = 3;
    public static final byte REPLY_FINGERS = 4;
    public static final byte REQUEST_FINGERS = 5;
    public static final byte REQUEST_PREDECESSOR = 6; 
    public static final byte REQUEST_FINGER = 7;
    public static final byte FINGER_INFO = 8;
    public static final byte REPLY_PREDECESSOR = 9;
    public static final byte REQUEST_SUCCESSOR = 10;
    public static final byte REPLY_SUCCESSOR  = 11;
    
    private final byte msgType;
    
    public ChordMessage(byte msgType) {
        this.msgType = msgType;
    }
    
    public byte getChordMessageType() {
        return this.msgType;
    }   
    
    public static ChordMessage parseArray(byte[] array) {
        if (array == null || array.length == 0) {
            return null;
        }
        
       ChordMessage msg = null;
       byte type = array[1];
       
       System.out.println("ChordMessageType: " + type);
       
       switch (type) {
           case ChordMessage.PREDECESSOR_NOTIFICATION:
                msg = new PredecessorNotification();
                break;
            case ChordMessage.REPLY_CHORD_ID:
                msg = new ReplyChordID();
                break;
            case ChordMessage.FINGER_INFO:
                msg = new FingerInfo();
                break;
            case ChordMessage.REPLY_FINGERS:
                System.out.println("Reply Fingers");
                msg = new ReplyFingers();
                break;
            case ChordMessage.REQUEST_CHORD_ID:
                msg = new RequestChordID();
                break;
            case ChordMessage.REQUEST_FINGER:
                msg = new RequestFinger();
                break;
            case ChordMessage.REQUEST_FINGERS:
                msg = new RequestFingers();
                break;
            case ChordMessage.REQUEST_PREDECESSOR:
                msg = new RequestPredecessor();
                break;
            case ChordMessage.SUCCESSOR_NOTIFICATION:
                msg = new SuccessorNotification();
                break;
            case ChordMessage.REPLY_PREDECESSOR:
                msg = new ReplyPredecessor();
                break;
            case ChordMessage.REQUEST_SUCCESSOR:
                msg = new RequestSuccessor();
                break;
            case ChordMessage.REPLY_SUCCESSOR:
                msg = new ReplySuccessor();
                break;
            default:
                System.err.println("Unknown ChordMessage Type: " + type);
       }
       
       
        if (msg == null) {
            return null;
        } else {
            msg.fromByteArray(array);
            return msg;
        }
    }
}
