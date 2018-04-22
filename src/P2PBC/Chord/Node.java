package P2PBC.Chord;

import java.math.BigInteger;
import java.net.InetAddress;
import java.util.*;
import java.util.Map.Entry;

public class Node {
    private Identifier id;
    private InetAddress address;
    private Node[] fingerTable;
    private Node predecessor;

    public Node(InetAddress address) {
        this.address = address;
        this.id = new Identifier(address.getAddress());
        this.fingerTable = new Node[Identifier.getBitLength()];
    }

    public InetAddress getAddress() {
        return address;
    }

    public Identifier getId() {
        return id;
    }

    public Node getPredecessor() {
        return predecessor;
    }

    public Node[] getFingerTable() {
        return fingerTable;
    }

    public void updateFingerTable(TreeMap<Identifier, Node> network) {
        BigInteger gap = BigInteger.ONE;
        Entry<Identifier, Node> entry = network.lowerEntry(id);
        predecessor = entry == null? network.lastEntry().getValue() : entry.getValue();

        for (int i = 0; i < Identifier.getBitLength(); i++) {
            entry = network.ceilingEntry(id.add(gap));
            fingerTable[i] = entry == null? network.firstEntry().getValue() : entry.getValue();
            gap = gap.shiftLeft(1);
        }
    }

    @Override
    public String toString() {
        return id.toString();
    }

    public String toSIFString() {
        return toSIFString(true);
    }

    public String toSIFString(boolean asMultigraph) {
        StringBuilder result = new StringBuilder();
        Collection<Node> neighbours = Arrays.asList(fingerTable);

        if (!asMultigraph)
            neighbours = new HashSet<>(neighbours);

        result.append(id.toString()).append(" link");

        for (Node link: neighbours) {
            result.append(" ").append(link.toString());
        }

        return result.append("\n").toString();
    }

    public String toDOTString() {
        return toDOTString(true);
    }

    public String toDOTString(boolean asMultigraph) {
        StringBuilder result = new StringBuilder();
        Collection<Node> neighbours = Arrays.asList(fingerTable);

        if (!asMultigraph)
            neighbours = new HashSet<>(neighbours);

        result.append(id.toString()).append(" [label=\"").append(address.toString()).append("\"]; ")
                .append(id.toString()).append(" -> {");

        for (Node link: neighbours) {
            result.append(" ").append(link.toString());
        }

        return result.append(" }\n").toString();
    }

    public List<Node> getPathTo(Identifier identifier) {
        ArrayList<Node> result = new ArrayList<>();
        result.add(this);

        if (identifier.isBetween(predecessor.id, id))
            return result;

        if (identifier.isBetween(id, fingerTable[0].id)) {
            result.add(fingerTable[0]);

            return result;
        }

        Node predecessor = findPredecessor(identifier);

        if (predecessor == this)
            return result;

        result.addAll(predecessor.getPathTo(identifier));

        return result;
    }

    private Node findPredecessor(Identifier identifier) {
        for (int i = Identifier.getBitLength() - 1; i >= 0 ; i--)
            if (fingerTable[i].id.isBetween(id, identifier))
                return fingerTable[i];

        return this;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof Node)
            return id.equals(((Node) obj).id);

        return false;
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }
}
