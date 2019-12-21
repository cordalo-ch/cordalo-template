#!/bin/bash

export JAVA_HOME=/usr/lib/jvm/java-8-openjdk-amd64

export NodeDebugPortStart=5005
export NodeJolokiaPortStart=7005
#export NodeMemory="-Xms1G -Xmx1G -XX:+UseG1GC"
export NodeMemory="-Xms512m -Xmx512m -XX:+UseG1GC"

export WAIT_TIME_TILL_ALL_WEBSERVER_STARTED=250

# do not change below this line -----------------------
export CORDA_HOME="$( cd "$( dirname "${BASH_SOURCE[0]}" )" >/dev/null 2>&1 && pwd )"/..

export NodeNames=`grep ".*O=.*L=.*C=.*" $CORDA_HOME/build.gradle | awk -F "," '{print $1}' | awk -F "=" '{print $2}' | awk -F " " '{print $1}'`
export NodeNof=`grep ".*O=.*L=.*C=.*" $CORDA_HOME/build.gradle | awk -F "," '{print $1}' | awk -F "=" '{print $2}' | awk -F " " '{print $1}' | wc -l`

export NodeSSHPorts=`grep ssh $CORDA_HOME/build.gradle | awk '{print $2}'`
export NodeSSHPorts=`echo $NodeSSHPorts | awk '{gsub(" ","|"); print}'`

export NodeWebPorts=`grep server.port $CORDA_HOME/clients/build.gradle | awk -F "'" '{print $2}' | awk -F "=" '{print $2}'`
export NodeWebPorts=`echo $NodeWebPorts | awk '{gsub(" ","|"); print}'`

export NodeSSHPortStart=`echo $NodeSSHPorts | awk -F "|" '{print $1}'`
