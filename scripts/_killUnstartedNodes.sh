#!/bin/bash
BASE_DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" >/dev/null 2>&1 && pwd )"
. "$BASE_DIR"/env.sh

stopped=
checkPorts(){
	port=$1
	name=$2
	nof=$(netstat -an | grep $port.*LISTEN | wc -l)
	if [ $nof -eq 0 ];  then
		echo "kill Java Nodes for $name"
		cd "$BASE_DIR" || exit
		echo "$(eval `./_killNode.sh $name`)"
		stopped="${stopped}${name}"
	fi
}

i=${NodeSSHPortStart}
nodes=( $NodeNames )
for n in "${nodes[@]}"
do
  checkPorts $i $n
  (( i=i+3 ))
done

retval=${stopped}

