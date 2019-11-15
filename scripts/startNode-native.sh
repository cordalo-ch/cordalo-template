#!/bin/bash
BASEDIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" >/dev/null 2>&1 && pwd )"

die () {
    echo >&2 "$@"
    exit 1
}

[ "$#" -eq 3 ] || die "missing arguments: usage: startNode-native.sh node-name debug-port jolokia-port "

## 5005 ++
DEBUG_PORT=$2
## 7006 ++
JOLOKIA_PORT=$3
##
NODE_NAME=$1
NODE_DIR=${CORDA_HOME}/build/nodes
cd ${NODE_DIR}/${NODE_NAME}
echo java -Xmx1500m -Dcapsule.jvm.args="-agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=${DEBUG_PORT} -javaagent:drivers/jolokia-jvm-1.6.0-agent.jar=port=${JOLOKIA_PORT},logHandlerClass=net.corda.node.JolokiaSlf4jAdapter" -Dname=${NODE_NAME} -jar ${NODE_DIR}/${NODE_NAME}/corda.jar
java -Xmx1500m -Dcapsule.jvm.args="-agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=${DEBUG_PORT} -javaagent:drivers/jolokia-jvm-1.6.0-agent.jar=port=${JOLOKIA_PORT},logHandlerClass=net.corda.node.JolokiaSlf4jAdapter" -Dname=${NODE_NAME} -jar ${NODE_DIR}/${NODE_NAME}/corda.jar >> ${NODE_DIR}/${NODE_NAME}/logs/node.log &

