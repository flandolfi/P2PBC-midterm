package P2PBC.Chord;

import java.io.*;
import java.math.BigInteger;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.util.*;
import java.util.Map.Entry;


public class Node {
    private Identifier id;
    private InetSocketAddress address;
    private Node[] fingerTable;
    private Node predecessor;

    public static Collection<Node> buildNetwork(int bits, int nodes) {
        TreeMap<Identifier, Node> network = new TreeMap<>();
        Random random = new Random();
        byte[] bytes = new byte[4];
        Identifier.setBitLength(bits);

        while (network.size() < nodes) {
            try {
                random.nextBytes(bytes);
                Node node = new Node(InetAddress.getByAddress(bytes), random.nextInt(65536));
                network.putIfAbsent(node.getId(), node);
            } catch (UnknownHostException ignore) {}
        }

        for (Node node : network.values())
            node.initializeFingerTable(network);

        return network.values();
    }

    public static void writeDOTFile(Writer writer, Collection<Node> network) throws IOException {
        writeDOTFile(writer, network, true);
    }

    public static void writeDOTFile(Writer writer, Collection<Node> network, boolean asMultigraph) throws IOException {
        writer.append("// BITS: ").append(String.valueOf(Identifier.getBitLength()))
                .append("\n// NODES: ").append(String.valueOf(network.size()))
                .append("\n\ndigraph network {\n");

        for (Node node : network)
            writer.append("\t").append(node.toDOTString(asMultigraph));

        writer.append("}\n");
    }

    public static void writeSIFFile(Writer writer, Collection<Node> network) throws IOException {
        writeSIFFile(writer, network, true);
    }

    public static void writeSIFFile(Writer writer, Collection<Node> network, boolean asMultigraph) throws IOException{
        for (Node node : network)
            writer.append(node.toSIFString(asMultigraph));
    }

    public Node(InetAddress address, int port) {
        this(new InetSocketAddress(address, port));
    }

    public Node(InetSocketAddress address) {
        this.address = address;
        ByteBuffer buffer = ByteBuffer.allocate(8)
                .put(address.getAddress().getAddress())
                .putInt(address.getPort());
        this.id = new Identifier(buffer.array());
        this.fingerTable = new Node[Identifier.getBitLength()];
    }

    public InetSocketAddress getAddress() {
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

    private void initializeFingerTable(TreeMap<Identifier, Node> network) {
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

    private String toSIFString() {
        return toSIFString(true);
    }

    private String toSIFString(boolean asMultigraph) {
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

    private String toDOTString() {
        return toDOTString(true);
    }

    private String toDOTString(boolean asMultigraph) {
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

        Node predecessor = closestPrecedingNode(identifier);

        if (predecessor == this)
            return result;

        result.addAll(predecessor.getPathTo(identifier));

        return result;
    }

    private Node closestPrecedingNode(Identifier identifier) {
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
