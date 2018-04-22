# Analysis and Simulation of the Chord Lookup Protocol ##

The goal of this project is to create a model of a Chord network and simulate
the lookup protocol. This application was developed as a midterm of the Peer to
Peer and Block Chain (P2PBC) course at University of Pisa, taught by Prof. Laura
Ricci.

## Execution ##

The program can be executed with the command

    java -jar ./bin/P2PBC-midterm.jar

executed in the root folder. This will start a simulation on a Chord ring of 2¹⁶
possible keys initialized with 2¹⁰ nodes, creating a JSON file `./log.json` with
the routing statistics, such as an histogram of the number of keys of each node
or the length of the path computed by Chord. As example:

    {
      "experiments": [
        {
          "endNodes": {
            "0": 505,
            "1": 268,
            "2": 125,
            ...
          },
          "nodes": 1024,
          "bits": 16,
          "pathLengths": {
            "0": 2,
            "1": 1,
            "2": 10,
            ...
          },
          "gaps": {
            "110": 2,
            "111": 2,
            "232": 1,
            ...
          },
            "queries": {
            "22": 6,
            "23": 4,
            "24": 1,
            "25": 1,
            ...
          },
          "iterations": 1
        }
      ]
    }

The program have also the following optional parameters, that can be printed
using the `-h` or `--help` option:

    $ java -jar ./bin/P2PBC-midterm.jar --help
    usage: chord-simulator
    -b,--bits <arg>      Number of bits (default: 16)
    -d,--dot <arg>       Export graph to DOT file
    -h,--help            Show this help text and exit
    -l,--lookups <arg>   Number of lookup tests per node (default: 1)
    -n,--nodes <arg>     Number of nodes (default: 1024)
    -o,--out <arg>       Store log statistics to JSON file. If it exists,
                         append the results (default: "./log.json")
    -s,--sif <arg>       Export graph to SIF file

## Batch simulations ##

A suite of simulation can be executed running the command

    ./bin/run_batch.sh

in the root folder. This will run a set of experiments with the following
parameters:

| Iteration     | Nodes     | Lookups   | SIF File                                  | JSON File                 |
|---------------|-----------|-----------|-------------------------------------------|---------------------------|
| 1             | 2         | 32768     | `./data/graphs/graph_2_nodes.sif`         | `./data/logs/log.json`    |
| 2             | 4         | 16384     | `./data/graphs/graph_4_nodes.sif`         | `./data/logs/log.json`    |
| 3             | 8         | 8192      | `./data/graphs/graph_8_nodes.sif`         | `./data/logs/log.json`    |
| 4             | 16        | 4096      | `./data/graphs/graph_16_nodes.sif`        | `./data/logs/log.json`    |
| 5             | 32        | 2048      | `./data/graphs/graph_32_nodes.sif`        | `./data/logs/log.json`    |
| 6             | 64        | 1024      | `./data/graphs/graph_64_nodes.sif`        | `./data/logs/log.json`    |
| 7             | 128       | 512       | `./data/graphs/graph_128_nodes.sif`       | `./data/logs/log.json`    |
| 8             | 256       | 256       | `./data/graphs/graph_256_nodes.sif`       | `./data/logs/log.json`    |
| 9             | 512       | 128       | `./data/graphs/graph_512_nodes.sif`       | `./data/logs/log.json`    |
| 10            | 1024      | 64        | `./data/graphs/graph_1024_nodes.sif`      | `./data/logs/log.json`    |
| 11            | 2048      | 32        | `./data/graphs/graph_2048_nodes.sif`      | `./data/logs/log.json`    |
| 12            | 4096      | 16        | `./data/graphs/graph_4096_nodes.sif`      | `./data/logs/log.json`    |
| 13            | 8192      | 8         | `./data/graphs/graph_8192_nodes.sif`      | `./data/logs/log.json`    |
| 14            | 16384     | 4         | `./data/graphs/graph_16384_nodes.sif`     | `./data/logs/log.json`    |
| 15            | 32768     | 2         | `./data/graphs/graph_32768_nodes.sif`     | `./data/logs/log.json`    |
| 16            | 65536     | 1         | `./data/graphs/graph_65536_nodes.sif`     | `./data/logs/log.json`    |

## Dependences ##

This program depends on the following libraries:

 - [JSON-Java](https://github.com/stleary/JSON-java), used to create and modify
   the JSON log file;
 - [Apache Commons CLI](https://commons.apache.org/proper/commons-cli/), used to
   parse the optional parameters.
