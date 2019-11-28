#!/bin/bash
BASE_DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" >/dev/null 2>&1 && pwd )"
. "$BASE_DIR"/env.sh

cd "$BASE_DIR" || exit

i=${NodeDebugPortStart}
j=${NodeJolokiaPortStart}
nodes=( $NodeNames )
for n in "${nodes[@]}"
do
  echo "start Node $n $i $j"
  "$BASE_DIR"/startNode-native.sh $n $i $j
  (( i=i+1 ))
  (( j=j+1 ))
done


