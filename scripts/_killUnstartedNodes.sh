#!/bin/bash
BASEDIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" >/dev/null 2>&1 && pwd )"
. $BASEDIR/env.sh

stopped=
checkPorts(){
	port=$1
	name=$2
	nof=$(netstat -an | grep $port.*LISTEN | wc -l)
	if [ $nof -eq 0 ];  then
		echo "kill Java Nodes for $name"
		cd $BASEDIR
		echo "$(eval `./_killNode.sh $name`)"
		stopped="${stopped}${name}"
	fi
}

i=10103
nodes=( $NodeNames )
for n in "${nodes[@]}"
do
  checkPorts $i $n
  (( i=i+3 ))
done

retval=${stopped}

