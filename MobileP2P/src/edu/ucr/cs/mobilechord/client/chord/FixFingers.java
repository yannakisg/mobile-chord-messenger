package edu.ucr.cs.mobilechord.client.chord;


import java.util.ArrayList;
import java.util.List;

import edu.ucr.cs.mobilechord.transportapi.dhash.BigInt;
import edu.ucr.cs.mobilechord.transportapi.utilities.Pair;


public class FixFingers implements Runnable {

    private final Node node;

    public FixFingers(Node node) {
        this.node = node;
    }

    @Override
    public void run() {
        try {
            System.out.println("Running FixFingers");
            List<BigInt> fingers = node.getFingerList();
            List<BigInt> newFingers = new ArrayList<BigInt>();
            List<Pair<String, Integer>> newFingersInfo = new ArrayList<Pair<String, Integer>>();
            BigInt myID = node.getMyID();

            for (int i = 0; i < fingers.size(); i++) {
                if (!fingers.get(i).equals(myID)) {
                    BigInt successor = node.findSuccessor(fingers.get(i));
                    newFingers.add(successor);
                    newFingersInfo.add(node.findFromIDsToIPPortMap(successor));
                } else {
                    newFingers.add(myID);
                    newFingersInfo.add(node.findFromIDsToIPPortMap(myID));
                }
            }

            node.setFingers(newFingers, newFingersInfo);
        } catch (InterruptedException ex) {

        }
    }

}
