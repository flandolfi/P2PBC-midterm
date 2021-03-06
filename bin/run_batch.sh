#!/bin/bash

cd $(dirname $(readlink -f $0))

NODES=2
ITERS=32768
BITS=16
GRAPH_DIR="../data/graphs/"
LOG_DIR="../data/logs/"
LOG_FILE="${LOG_DIR}/log.json"

mkdir -p ${GRAPH_DIR}
mkdir -p ${LOG_DIR}
> ${LOG_FILE}

for i in `seq 1 ${BITS}`; do
    echo -e "\n --- Executing ${ITERS} simulations on ${NODES} nodes ---"
    java -jar P2PBC-midterm.jar \
            -n ${NODES} \
            -l ${ITERS} \
            -b ${BITS} \
            -s ${GRAPH_DIR}/graph_${NODES}_nodes.sif \
            -o ${LOG_FILE}
    NODES=$((NODES*2))
    ITERS=$((ITERS/2))
done
