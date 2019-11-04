#!/bin/bash

export CORDA_HOME="$( cd "$( dirname "${BASH_SOURCE[0]}" )" >/dev/null 2>&1 && pwd )"/..

export NodeNames=`grep ".*O=.*L=.*C=.*" $CORDA_HOME/build.gradle | awk -F "," '{print $1}' | awk -F "=" '{print $2}' | awk -F " " '{print $1}'`
export NodeNof=`grep ".*O=.*L=.*C=.*" $CORDA_HOME/build.gradle | awk -F "," '{print $1}' | awk -F "=" '{print $2}' | awk -F " " '{print $1}' | wc -l`

