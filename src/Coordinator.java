import java.io.*;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Date;
import java.util.Iterator;
import java.util.Random;
import java.util.TreeMap;

public class Coordinator {
    public static TreeMap<Identifier, Node> buildNetwork(int bits, int nodes) {
        TreeMap<Identifier, Node> network = new TreeMap<>();
        Random random = new Random();
        byte[] bytes = new byte[4];
        Identifier.setBitLength(bits);
        System.out.print("Creating nodes: 0 of " + nodes + ".\r");
        int i = 0;

        while (network.size() < nodes) {
            try {
                random.nextBytes(bytes);
                Node node = new Node(InetAddress.getByAddress(bytes));

                if (network.putIfAbsent(node.getId(), node) == null) {
                    System.out.print("Creating nodes: " + ++i + " of " + nodes + ".\r");
                }
            } catch (UnknownHostException ignore) {}
        }

        i = 0;
        System.out.print("\nCreating finger tables: 0 of " + network.size() + ".\r");

        for (Node node : network.values()) {
            node.updateFingerTable(network);
            System.out.print("Creating finger tables: " + ++i + " of " + network.size() + ".\r");
        }

        return network;
    }

    public static void writeDOTFile(Writer writer, TreeMap<Identifier, Node> network) throws IOException {
        System.out.print("\nWriting nodes to DOT file: 0 of " + network.size() + ".\r");
        writer.append("// BITS: ").append(String.valueOf(Identifier.getBitLength()))
                .append("\n// NODES: ").append(String.valueOf(network.size()))
                .append("\n\ndigraph network {\n");
        int i = 0;

        for (Node node : network.values()) {
            writer.append("\t").append(node.toDOT());
            System.out.print("Writing nodes to DOT file: " + ++i + " of " + network.size() + ".\r");
        }

        writer.append("}\n");
    }

    public static void writeJSONFile(Writer writer, TreeMap<Identifier, Node> network) throws IOException {
        System.out.print("\nRunning simulations: 0 of " + network.size() + ".\r");
        writer.append("{\n\t\"lookups\": [\n\t\t");
        Random random = new Random();
        byte[] bytes = new byte[32];
        int i = 0;

        for (Iterator<Node> iterator = network.values().iterator(); iterator.hasNext(); ) {
            Node node = iterator.next();
            random.nextBytes(bytes);
            Identifier id = new Identifier(bytes);
            writer.append("{\n\t\t\t\"from\": ").append(node.toString())
                    .append(",\n\t\t\t\"to\": ").append(id.toString())
                    .append(",\n\t\t\t\"path\": ").append(node.getPathTo(id).toString())
                    .append("\n\t\t}");
            System.out.print("Running simulations: " + ++i + " of " + network.size() + ".\r");

            if (iterator.hasNext())
                writer.append(", ");
        }

        writer.append("\n\t]\n}\n");
    }

    public static void main(String[] args) {
        Integer bits = 8, nodes = 64;

        try {
            if (args.length >= 1)
                nodes = Integer.parseInt(args[0]);

            if (args.length >= 2)
                bits = Integer.parseInt(args[1]);
        } catch (NumberFormatException e) {
            System.err.println("Arguments must be integers.");
            System.exit(1);
        }

        if (bits < 1 || nodes < 1) {
            System.err.println("Arguments must be greater than 0.");
            System.exit(1);
        }

        if (nodes > (1 << bits)) {
            System.err.println("Number of nodes must be 0 < NODES < 2^BITS.");
            System.exit(1);
        }

        File netFile = new File("./log/network " + new Date() + ".dot");
        File logFile = new File("./log/log " + new Date() + ".json");

        if (!netFile.getParentFile().exists() && !netFile.getParentFile().mkdirs()) {
            System.err.println("Cannot create log folder.");
            System.exit(1);
        }

        TreeMap<Identifier, Node> network = buildNetwork(bits, nodes);

        try (Writer netWriter = new BufferedWriter(new OutputStreamWriter(
                new FileOutputStream(netFile), "utf-8"));
             Writer logWriter = new BufferedWriter(new OutputStreamWriter(
                     new FileOutputStream(logFile), "utf-8"))) {
            writeDOTFile(netWriter, network);
            writeJSONFile(logWriter, network);
            System.out.println("\nDone.");
        } catch (IOException e) {
            System.err.println("I/O Exception: " + e.getMessage());
        }
    }
}
