package P2PBC;

import P2PBC.Chord.*;

import org.apache.commons.cli.*;

import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.io.*;
import java.util.*;

public class Coordinator {
    public static void main(String[] args) {
        Integer nBits = 16, nNodes = 1024, nIters = 1;
        Options options =  new Options();
        CommandLineParser parser = new DefaultParser();
        HelpFormatter formatter = new HelpFormatter();
        CommandLine cmd = null;

        Option nodesOpt = new Option("n", "nodes", true, "Number of nodes (default: 1024)");
        Option bitsOpt = new Option("b", "bits", true, "Number of bits (default: 16)");
        Option SIFOpt = new Option("s", "sif", true, "Export graph to SIF file");
        Option DOTOpt = new Option("d", "dot", true, "Export graph to DOT file");
        Option logOpt = new Option("o", "out", true,
                "Store log statistics to JSON file. If it exists, append the results (default: \"./log.json\")");
        Option helpOpt = new Option("h", "help", false, "Show this help text and exit");
        Option itOpt = new Option("l", "lookups", true,
                "Number of lookup tests per node (default: 1)");

        options.addOption(nodesOpt).addOption(bitsOpt).addOption(SIFOpt).addOption(DOTOpt)
                .addOption(logOpt).addOption(helpOpt).addOption(itOpt);

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

            if (cmd.getOptionValue("lookups") != null)
                nIters = Integer.parseInt(cmd.getOptionValue("lookups"));
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
        String logPath = "log.json";

        if (cmd.getOptionValue("out") != null) {
            logPath = cmd.getOptionValue("out");

            try(Reader reader = new BufferedReader(new InputStreamReader(
                    new FileInputStream(cmd.getOptionValue("out")), "utf-8"))) {
                log = new JSONObject(new JSONTokener(reader));
            } catch (JSONException e) {
                log = new JSONObject();
            } catch (FileNotFoundException ignore) {} catch (IOException e) {
                System.err.println("I/O Exception: " + e.getMessage());
                System.exit(1);
            }
        }

        /* **************************************** GENERATE NETWORK ************************************************ */

        System.out.print("Building network... ");
        ArrayList<Node> network = new ArrayList<>(Node.buildNetwork(nBits, nNodes));
        System.out.println("Done.");

        if (cmd.getOptionValue("sif") != null)
            try (Writer writer = new BufferedWriter(new OutputStreamWriter(
                    new FileOutputStream(cmd.getOptionValue("sif")), "utf-8"))) {
                System.out.print("Writing SIF file... ");
                Node.writeSIFFile(writer, network);
                System.out.println("Done.");
            } catch (IOException e) {
                System.err.println("I/O Exception: " + e.getMessage());
            }

        if (cmd.getOptionValue("dot") != null)
            try (Writer writer = new BufferedWriter(new OutputStreamWriter(
                    new FileOutputStream(cmd.getOptionValue("dot")), "utf-8"))) {
                System.out.print("Writing DOT file... ");
                Node.writeDOTFile(writer, network);
                System.out.println("Done.");
            } catch (IOException e) {
                System.err.println("I/O Exception: " + e.getMessage());
            }

        /* **************************************** START SIMULATION ************************************************ */

        System.out.print("Running simulations: 0 of " + nIters*nNodes + ".\r");
        Random random = new Random();
        byte[] bytes = new byte[32];
        HashMap<Integer, Integer> gapHist = new HashMap<>();
        HashMap<Integer, Integer> pathLengthHist = new HashMap<>();
        HashMap<Node, Integer> queries = new HashMap<>();
        HashMap<Node, Integer> endNodes = new HashMap<>();
        int iters = 0;

        for (Node node: network) {
            Identifier gap = node.getId().subtract(node.getPredecessor().getId());
            gapHist.compute(gap.getValue(), (k, v) -> v == null ? 1 : v + 1);
            queries.putIfAbsent(node, 0);
            endNodes.putIfAbsent(node, 0);

            for (int i = 0; i < nIters; i++) {
                random.nextBytes(bytes);
                Identifier id = new Identifier(bytes);
                List<Node> path = node.getPathTo(id);
                pathLengthHist.compute(path.size() - 1, (k, v) -> v == null ? 1 : v + 1);
                path.forEach(n -> queries.compute(n, (k, v) -> v == null ? 1 : v + 1));
                endNodes.compute(path.get(path.size() - 1), (k, v) -> v == null ? 1 : v + 1);
                System.out.print("Running simulations: " + ++iters + " of " + nIters*nNodes + ".\r");
            }
        }

        /* **************************************** WRITE STATISTICS ************************************************ */

        System.out.print("\nWriting statistics... ");
        HashMap<String, Object> results = new HashMap<>();
        HashMap<Integer, Integer> queryHist = new HashMap<>();
        HashMap<Integer, Integer> endNodeHist = new HashMap<>();
        queries.values().forEach(q -> queryHist.compute(q, (k, v) -> v == null ? 1 : v + 1));
        endNodes.values().forEach(n -> endNodeHist.compute(n, (k, v) -> v == null ? 1 : v + 1));

        results.put("bits", nBits);
        results.put("nodes", nNodes);
        results.put("iterations", nIters);
        results.put("gaps", gapHist);
        results.put("pathLengths", pathLengthHist);
        results.put("queries", queryHist);
        results.put("endNodes", endNodeHist);

        log.append("experiments", new JSONObject(results));

        File logFile = new File(logPath);
        logFile.getParentFile().mkdirs();

        try (Writer writer = new BufferedWriter(new OutputStreamWriter(
                new FileOutputStream(logFile), "utf-8"))) {
            log.write(writer, 3,0);
        } catch (IOException e) {
            System.err.println("I/O Exception: " + e.getMessage());
        }

        System.out.println("Done.");
    }
}
