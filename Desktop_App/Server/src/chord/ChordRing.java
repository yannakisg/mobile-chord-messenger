package chord;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import utilities.Pair;

public class ChordRing {
    private final List<ChordNode> ring;

    public ChordRing() {
        ring = Collections.synchronizedList(new ArrayList<ChordNode>());
    }

    private int find(BigInt id) {
        int retValue = -1;
        
        synchronized (ring) {
            int low = 0;
            int high = ring.size() - 1;
            int middle;
            while (low <= high) {
                middle = low + (high - low) / 2;

                int res = id.compareTo(ring.get(middle).getID());
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

    private int find(ChordNode chNode) {
        return find(chNode.getID());
    }

    public void insert(BigInt id, String ip, Integer port) {
        ChordNode chNode = new ChordNode(id, ip, port);
        int pos = find(chNode);

        if (pos != -1) {
            if (!ring.get(pos).getIP().equals(ip)) {
                ring.remove(pos);
            } else {
                return;
            }
        }

        ring.add(chNode);
        
        synchronized(ring) {
            Collections.sort(ring);
        }
    }
    
    public int findPosition(BigInt id) {
        return find(id);
    }

    public Pair<String, Integer> getSuccessorInfo(int pos) {
        if (pos == -1) {
            return null;
        } else {
            ChordNode chNode = ring.get((pos + 1) % ring.size());
            Pair<String, Integer> p = new Pair(chNode.getIP(), chNode.getPort());
            return p;
        }
    }
    
    public Pair<String, Integer> getPredecessorInfo(int pos) {
        if (pos == -1) {
            return null;
        } else {
            if (pos - 1 == -1) {
                pos = ring.size();
            }
            ChordNode chNode = ring.get((pos - 1) % ring.size());
            Pair<String, Integer> p = new Pair(chNode.getIP(), chNode.getPort());
            return p;
        }
    }
    
    public BigInt getPredecessorID(int pos) {
        if (pos == -1) {
            return null;
        } else {
            if (pos - 1 == -1) {
                pos = ring.size();
            }
            return ring.get((pos - 1) % ring.size()).getID();
        }        
    }
    
    public BigInt getSuccessorID(int pos) {
        if (pos == -1) {
            return null;
        } else {
            return ring.get((pos + 1) % ring.size()).getID();
        }
    }
    
    
    @Override
    public int hashCode() {
        int hash = 7;
        hash = 47 * hash + Objects.hashCode(this.ring);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final ChordRing other = (ChordRing) obj;
        
        if (other.ring.size() != ring.size()) {
            return false;
        }
        
        for (int i = 0; i < ring.size(); i++) {
            if (!other.ring.get(i).equals(ring.get(i))) {
                return false;
            }
        }
        
        return true;
    }

    protected class ChordNode implements Comparable<ChordNode> {
        private final BigInt id;
        private final String ip;
        private final Integer port;

        protected ChordNode(BigInt id, String ip, Integer port) {
            this.id = id;
            this.ip = ip;
            this.port = port;
        }

        protected BigInt getID() {
            return this.id;
        }

        protected String getIP() {
            return this.ip;
        }
        
        protected Integer getPort() {
            return this.port;
        }

        @Override
        public int hashCode() {
            int hash = 3;
            hash = 37 * hash + Objects.hashCode(this.id);
            hash = 37 * hash + Objects.hashCode(this.ip);
            hash = 37 * hash + Objects.hashCode(this.port);
            return hash;
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            final ChordNode other = (ChordNode) obj;
            if (!Objects.equals(this.id, other.id)) {
                return false;
            }
            if (!Objects.equals(this.ip, other.ip)) {
                return false;
            }
            if (!Objects.equals(this.port, other.port)) {
                return false;
            }
            return true;
        }
        

        @Override
        public int compareTo(ChordNode o) {
            return this.id.compareTo(o.getID());
        }
    }
}
