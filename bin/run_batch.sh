#!/bin/bash

NODES=2
ITERS=1024
BITS=16
DIR_PATH=$1

if [ -z ${DIR_PATH} ]; then
    DIR_PATH="../data/"
fi

if [ ! -d ${DIR_PATH} ]; then
    echo -e"\"$1\"Not a directory. Usage:\n\t${0##*/} [LOG DIRECTORY PATH]"
    exit 1
fi

mkdir -p "${DIR_PATH}/graphs/"
mkdir -p "${DIR_PATH}/logs/"

for i in `seq 1 ${BITS}`; do
    echo " --- Starting simulations with $NODES nodes ---"
    java -jar P2PBC-midterm.jar \
            -n ${NODES} \
            -i ${ITERS} \
            -b ${BITS} \
            -s ${DIR_PATH}/graphs/graph_${NODES}_nodes.sif \
            -l ${DIR_PATH}/logs/log_${ITERS}_simulations.json
    NODES=$((NODES*2))
done
