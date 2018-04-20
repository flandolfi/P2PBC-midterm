#!/bin/bash

cd $(dirname $(readlink -f $0))

NODES=2
ITERS=16
BITS=16
GRAPH_DIR="../data/graphs/"
LOG_DIR="../data/logs/"

mkdir -p ${GRAPH_DIR}
mkdir -p ${LOG_DIR}

for i in `seq 1 ${BITS}`; do
    echo -e "\n --- Starting simulations with $NODES nodes ---"
    java -jar P2PBC-midterm.jar \
            -n ${NODES} \
            -l ${ITERS} \
            -b ${BITS} \
            -s ${GRAPH_DIR}/graph_${NODES}_nodes.sif \
            -o ${LOG_DIR}/log.json
    NODES=$((NODES*2))
done
