#!/bin/bash
BASEDIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" >/dev/null 2>&1 && pwd )"
. $BASEDIR/env.sh

cd $BASEDIR

i=${NodeDebugPortStart}
j=${NodeJolokiaPortStart}
nodes=( $NodeNames )
for n in "${nodes[@]}"
do
  echo "start Node $n $i $j"
  $BASEDIR/startNode-native.sh $n $i $j
  (( i=i+1 ))
  (( j=j+1 ))
done


