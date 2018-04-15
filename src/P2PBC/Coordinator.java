package P2PBC;

import P2PBC.Chord.*;

import org.apache.commons.cli.*;

//import org.jgrapht.alg.shortestpath.GraphMeasurer;
import org.jgrapht.alg.shortestpath.DijkstraShortestPath;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.DirectedPseudograph;

import org.json.JSONObject;
import org.json.JSONTokener;

import java.io.*;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.*;
import java.util.stream.Collectors;

public class Coordinator {
    public static DirectedPseudograph<Node, DefaultEdge> buildNetwork(int bits, int nodes) {
        DirectedPseudograph<Node, DefaultEdge> model = new DirectedPseudograph<>(DefaultEdge.class);
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
                model.addVertex(node);

                if (network.putIfAbsent(node.getId(), node) == null) {
                    System.out.print("Creating nodes: " + ++i + " of " + nodes + ".\r");
                }
            } catch (UnknownHostException ignore) {}
        }

        i = 0;
        System.out.print("\nCreating finger tables: 0 of " + network.size() + ".\r");

        for (Node node : network.values()) {
            node.updateFingerTable(network);

            for (Node edge: node.getFingerTable())
                model.addEdge(node, edge);

            System.out.print("Creating finger tables: " + ++i + " of " + network.size() + ".\r");
        }

        return model;
    }

    public static void writeDOTFile(String path, Collection<Node> network) {
        try (Writer writer = new BufferedWriter(new OutputStreamWriter(
                new FileOutputStream(path), "utf-8"))) {
            System.out.print("\nWriting DOT file... ");
            writer.append("// BITS: ").append(String.valueOf(Identifier.getBitLength()))
                    .append("\n// NODES: ").append(String.valueOf(network.size()))
                    .append("\n\ndigraph network {\n");

            for (Node node : network) {
                writer.append("\t").append(node.toDOT());
            }

            writer.append("}\n");
            System.out.println("Done.");
        } catch (IOException e) {
            System.err.println("I/O Exception: " + e.getMessage());
        }
    }

    public static void writeSIFFile(String path, Collection<Node> network) {
        try (Writer writer = new BufferedWriter(new OutputStreamWriter(
                new FileOutputStream(path), "utf-8"))) {
            System.out.print("\nWriting SIF file... ");

            for (Node node : network)
                writer.append(node.toSIF());

            System.out.println("Done.");
        } catch (IOException e) {
            System.err.println("I/O Exception: " + e.getMessage());
        }
    }

    public static void main(String[] args) {
        Integer nBits = 16, nNodes = 1024;
        Options options =  new Options();
        CommandLineParser parser = new DefaultParser();
        HelpFormatter formatter = new HelpFormatter();
        CommandLine cmd = null;

        Option nodesOpt = new Option("n", "nodes", true, "Number of nodes (default: 1024)");
        Option bitsOpt = new Option("b", "bits", true, "Number of bits (default: 16)");
        Option SIFOpt = new Option("s", "sif", true, "Export graph to SIF file");
        Option DOTOpt = new Option("d", "dot", true, "Export graph to DOT file");
        Option logOpt = new Option("l", "log", true, "Append log statistics to JSON file");
        Option helpOpt = new Option("h", "help", false, "Show this help text and exit");

        options.addOption(nodesOpt).addOption(bitsOpt).addOption(SIFOpt)
                .addOption(DOTOpt).addOption(logOpt).addOption(helpOpt);

        try {
            cmd = parser.parse(options, args);

            if (cmd.hasOption("help")) {
                formatter.printHelp("chord-simulator", options);
                System.exit(0);
            }

            if (cmd.getOptionValue("nodes") != null)
                nNodes = Integer.parseInt(cmd.getOptionValue("nodes"));

            if (cmd.getOptionValue("bits") != null)
                nBits = Integer.parseInt(cmd.getOptionValue("bits"));
        } catch (NumberFormatException e) {
            System.err.println("Arguments must be integers.");
            System.exit(1);
        } catch (ParseException e) {
            System.err.println("Parsing Error: " + e.getMessage());
            formatter.printHelp("chord-simulator", options);
            System.exit(1);
        }

        if (nBits < 1 || nNodes < 1) {
            System.err.println("Arguments must be greater than 0.");
            System.exit(1);
        }

        if (nNodes > (1 << nBits)) {
            System.err.println("Number of nodes must be 0 < NODES < 2^BITS.");
            System.exit(1);
        }

        JSONObject log = new JSONObject();
        DirectedPseudograph<Node, DefaultEdge> model = buildNetwork(nBits, nNodes);
        Set<Node> network = model.vertexSet();
        String logPath = "log.json";

        if (cmd.getOptionValue("sif") != null)
            writeSIFFile(cmd.getOptionValue("sif"), network);

        if (cmd.getOptionValue("dot") != null)
            writeDOTFile(cmd.getOptionValue("dot"), network);

        if (cmd.getOptionValue("log") != null) {
            try(Reader reader = new BufferedReader(new InputStreamReader(
                    new FileInputStream(cmd.getOptionValue("log")), "utf-8"))) {
                logPath = cmd.getOptionValue("log");
                log = new JSONObject(new JSONTokener(reader));
            } catch (IOException e) {
                System.err.println("I/O Exception: " + e.getMessage());
            }
        }

        /* **************************************** START SIMULATION ************************************************ */

        System.out.print("\nRunning simulations: 0 of " + nNodes + ".\r");
        Random random = new Random();
        byte[] bytes = new byte[32];
        ArrayList<Integer> keys = new ArrayList<>();
        ArrayList<Integer> pathLength = new ArrayList<>();
        ArrayList<Integer> inDegree = new ArrayList<>();
        ArrayList<Integer> shortestPathLength = new ArrayList<>();
        HashMap<Node, Integer> queries = new HashMap<>();
        DijkstraShortestPath<Node, DefaultEdge> dijkstraAlg = new DijkstraShortestPath<>(model);
//        GraphMeasurer<Node, DefaultEdge> measurer = new GraphMeasurer<>(model);
        int i = 0;

        for (Node node: network) {
            random.nextBytes(bytes);
            Identifier id = new Identifier(bytes);
            Identifier gap = node.getId().subtract(node.getPredecessor().getId());
            keys.add(gap.getValue());
            List<Node> path = node.getPathTo(id);
            pathLength.add(path.size() - 1);
            inDegree.add(model.inDegreeOf(node));
            shortestPathLength.add(dijkstraAlg.getPath(node, path.get(path.size() - 1)).getLength());
            path.forEach(n -> queries.compute(n, (k, v) -> { if (v == null) return 1; else return v + 1; }));
            System.out.print("Running simulations: " + ++i + " of " + network.size() + ".\r");
        }

        System.out.print("\nWriting statistics... ");

        HashMap<String, Object> results = new HashMap<>();
        results.put("bits", nBits);
        results.put("nodes", nNodes);
//        results.put("radius", measurer.getRadius());
//        results.put("diameter", measurer.getDiameter());
        results.put("indegrees", inDegree);
        results.put("gaps", keys);
        results.put("pathLengths", pathLength);
        results.put("shortestPathLengths", shortestPathLength);
        results.put("queries", network.stream().map(queries::get).collect(Collectors.toList()));

        log.append("experiments", new JSONObject(results));

        try (Writer writer = new BufferedWriter(new OutputStreamWriter(
                new FileOutputStream(logPath), "utf-8"))) {
            log.write(writer, 3,0);
        } catch (IOException e) {
            System.err.println("I/O Exception: " + e.getMessage());
        }

        System.out.println("Done.");
    }
}
