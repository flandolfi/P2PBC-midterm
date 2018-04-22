package P2PBC.Chord;

import java.io.*;
import java.math.BigInteger;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.util.*;
import java.util.Map.Entry;

/**
 * This class models a peer in the Chord network.
 */
public class Node {
    private Identifier id;
    private InetSocketAddress address;
    private Node[] fingerTable;
    private Node predecessor;

    /**
     * Builds a ready-to-use Chord network with {@code nodes} nodes and
     * finger tables of size {@code bits}.
     *
     * @param bits the size (in bits) of the identifier or, equivalently,
     *             the size of the finger tables.
     * @param nodes the number of nodes in the network.
     * @return A collection of {@link Node}s, with finger table set as a
     * Chord ring.
     */
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

    /**
     * Writes the given collection {@code network} as a DOT file, using the
     * {@code writer}.
     *
     * @param writer A character-stream writer.
     * @param network The network to export.
     * @throws IOException If an I/O error occurs.
     */
    public static void writeDOTFile(Writer writer, Collection<Node> network) throws IOException {
        writeDOTFile(writer, network, true);
    }

    /**
     * Writes the given collection {@code network} as a DOT file, using the
     * {@code writer}.
     *
     * @param writer A character-stream writer.
     * @param network The network to export.
     * @param asMultigraph If {@code true}, writes the network as it is, otherwise
     *                     ignores duplicated edges.
     * @throws IOException If an I/O error occurs.
     */
    public static void writeDOTFile(Writer writer, Collection<Node> network, boolean asMultigraph) throws IOException {
        writer.append("// BITS: ").append(String.valueOf(Identifier.getBitLength()))
                .append("\n// NODES: ").append(String.valueOf(network.size()))
                .append("\n\ndigraph network {\n");

        for (Node node : network)
            writer.append("\t").append(node.toDOTString(asMultigraph));

        writer.append("}\n");
    }

    /**
     * Writes the given collection {@code network} as a SIF file, using the
     * {@code writer}.
     *
     * @param writer A character-stream writer.
     * @param network The network to export.
     * @throws IOException If an I/O error occurs.
     */
    public static void writeSIFFile(Writer writer, Collection<Node> network) throws IOException {
        writeSIFFile(writer, network, true);
    }

    /**
     * Writes the given collection {@code network} as a SIF file, using the
     * {@code writer}.
     *
     * @param writer A character-stream writer.
     * @param network The network to export.
     * @param asMultigraph If {@code true}, writes the network as it is, otherwise
     *                     ignores duplicated edges.
     * @throws IOException If an I/O error occurs.
     */
    public static void writeSIFFile(Writer writer, Collection<Node> network, boolean asMultigraph) throws IOException{
        for (Node node : network)
            writer.append(node.toSIFString(asMultigraph));
    }

    /**
     * Creates a new {@link Node} using the {@link InetAddress} and the port
     * number of the peer.
     *
     * @param address the address of the peer.
     * @param port the port number of the peer.
     */
    public Node(InetAddress address, int port) {
        this(new InetSocketAddress(address, port));
    }

    /**
     * Creates a new {@link Node} using the {@link InetSocketAddress} and the
     * port number of the peer.
     *
     * @param address the address of the peer.
     */
    public Node(InetSocketAddress address) {
        this.address = address;
        ByteBuffer buffer = ByteBuffer.allocate(8)
                .put(address.getAddress().getAddress())
                .putInt(address.getPort());
        this.id = new Identifier(buffer.array());
        this.fingerTable = new Node[Identifier.getBitLength()];
    }

    /**
     * Returns the address of the peer.
     *
     * @return the {@link InetSocketAddress} of the peer.
     */
    public InetSocketAddress getAddress() {
        return address;
    }

    /**
     * Returns the identifier of the node.
     *
     * @return the {@link Identifier} of the node.
     */
    public Identifier getId() {
        return id;
    }

    /**
     * Returns the current predecessor of the node in the Chord ring.
     *
     * @return the predecessor {@link Node} in the network.
     */
    public Node getPredecessor() {
        return predecessor;
    }

    /**
     * Returns the current finger table of the node.
     *
     * @return an array of {@link Node}s, representing the finger table of the
     * node.
     */
    public Node[] getFingerTable() {
        return fingerTable;
    }

    // Initializes the finger table by the given network
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

    /**
     * Returns the {@link Identifier} of the node as a {@link String}.
     *
     * @return a string representation of the node.
     */
    @Override
    public String toString() {
        return id.toString();
    }

    private String toSIFString() {
        return toSIFString(true);
    }

    // Builds the SIF representation of the node
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

    // Builds the DOT representation of the node
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

    /**
     * Computes and returns the path to the successor of the {@link Identifier}
     * {@code identifier}. The last {@link Node} in the path may contain the
     * searched key.
     *
     * @param identifier the {@link Identifier} of the key to be searched in
     *                   the Chord network.
     * @return an ordered {@link List} of {@link Node}s representing the path
     * computed by the Chord algorithm, containing also the {@link Node} who
     * called this method and having as last element the (possible) owner of
     * the key.
     */
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

        // Prevents stack overflow when the network is circular
        if (predecessor == this)
            return result;

        result.addAll(predecessor.getPathTo(identifier));

        return result;
    }

    // Computes the closest preceding node of the identifier
    private Node closestPrecedingNode(Identifier identifier) {
        for (int i = Identifier.getBitLength() - 1; i >= 0 ; i--)
            if (fingerTable[i].id.isBetween(id, identifier))
                return fingerTable[i];

        return this;
    }

    /**
     * Test whether two nodes have the same {@link Identifier}.
     *
     * @param obj the {@link Node} to be compared.
     * @return {@code true} if {@code obj} is a {@link Node} and has the same
     * {@link Identifier} of {@code this}, {@code false} otherwise.
     */
    @Override
    public boolean equals(Object obj) {
        if (obj instanceof Node)
            return id.equals(((Node) obj).id);

        return false;
    }

    /**
     * Returns the hashcode of the node, which is uniquely identified by its
     * {@link Identifier}.
     *
     * @return the computed hashcode.
     */
    @Override
    public int hashCode() {
        return id.hashCode();
    }
}
