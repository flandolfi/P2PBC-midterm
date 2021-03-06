#!/bin/bash

cd $(dirname $(readlink -f $0))/../data/cytoscape/

OUT_DIR="./summary"
OUT_FILE="${OUT_DIR}/stats.csv"

mkdir -p ${OUT_DIR}
echo -e "cc\tdiameter\tradius\tnodes" > ${OUT_FILE}

grep -hwr ./netstats/multi_* --exclude=./netstats/multi_2.netstats \
        -e "nodeCount Integer" -e "cc Double" \
        -e "diameter Integer" -e "radius Integer" \
    | grep -Eo "[0-9.]+" \
    | tr '\n' '\t' \
    | grep -Po "[0-9]+\.[0-9]+\t[0-9]+\t[0-9]+\t[0-9]+" >> ${OUT_FILE}
