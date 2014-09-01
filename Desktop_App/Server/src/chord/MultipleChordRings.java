package chord;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class MultipleChordRings {
    private final List<MultipleChordRingNode> chordRings;
    
    public MultipleChordRings() {
       this.chordRings = Collections.synchronizedList(new ArrayList< MultipleChordRingNode>());
    }
    
    public void insert(BigInt id, ChordRing chordRing) {
        MultipleChordRingNode multiChordNode = new MultipleChordRingNode(id, chordRing);
        int pos = find(id);

        if (pos != -1) {
            if (!chordRings.get(pos).getChordRing().equals(chordRing)) {
                chordRings.remove(pos);
            } else {
                return;
            }
        }

        chordRings.add(multiChordNode);
        
        synchronized(chordRings) {
            Collections.sort(chordRings);
        }
    }
    
    private int find(BigInt id) {
        int retValue = -1;
        
        synchronized (chordRings) {
            int low = 0;
            int high = chordRings.size() - 1;
            int middle;
            while (low <= high) {
                middle = low + (high - low) / 2;

                int res = id.compareTo(chordRings.get(middle).getID());
                if (res > 0) {
                    low = middle + 1;
                } else if (res < 0) {
                    high = middle - 1;
                } else {
                    retValue = middle;
                    break;
                }
            }
        }
        return retValue;
    }
    
    public int findPosition(BigInt id) {
        return find(id);
    }
    
    public ChordRing getChordRing(int pos) {
        return chordRings.get(pos).getChordRing();
    }
    
    protected class MultipleChordRingNode implements Comparable<MultipleChordRingNode> {
        private final BigInt id;
        private final ChordRing chordRing;
        
        protected MultipleChordRingNode(BigInt id, ChordRing chordRing) {
            this.id = id;
            this.chordRing = chordRing;
        }
        
        protected BigInt getID() {
            return this.id;
        }
        
        protected ChordRing getChordRing() {
            return this.chordRing;
        }
        
        @Override
        public int compareTo(MultipleChordRingNode o) {
            return this.id.compareTo(o.getID());
        }
        
    }
}
