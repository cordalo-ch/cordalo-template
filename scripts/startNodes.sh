#!/bin/bash
BASE_DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" >/dev/null 2>&1 && pwd )"
. "$BASE_DIR"/env.sh

cd "$BASE_DIR" || exit

(( max_nof = NodeNof ))
max_time=80
echo "Starting max $max_nof CORDA servers"
echo "-----------------------------------"

get_sshd(){
	retval=$(netstat -an | egrep "${NodeSSHPorts}" | grep LISTEN | wc -l)
}

wait_until_first_started(){
	get_sshd
	nof=$retval
	x=0
	echo "searching for 1st node to be started ... stop ctrl-c any time - $nof"
	while [ "$nof" -eq 0 ]
	do
		get_sshd
		nof=$retval
		x=$(( $x + 10 ))
		echo "slept $x s until first started...."
		x=$(( $x + 10 ))
		sleep 10s
	done
	retval=$nof
}
wait_until_all_started() {
	wait_until_first_started
	nof=$retval
	x=0
	while [ "$nof" -lt $max_nof ] && [ "$x" -lt $max_time ]
	do
		get_sshd
		nof=$retval
		x=$(( $x + 10 ))
		echo "slept $x s to see if all started... $nof"
		sleep 10s
	done
	echo "$nof started"
	retval=$nof
}

cd $CORDA_HOME
get_sshd
if [ "$retval" -eq $max_nof ]; then
	echo "all nodes started"
	exit 0
fi

#$CORDA_HOME/build/nodes/runnodes
"$BASE_DIR"/startNodes-native.sh

get_sshd
if [ "$retval" -eq 0 ]; then
	#first node to come up needs a lot of time, so keep 2nd .... low
	max_time=20
fi

echo "Wait 40s to spin up first log files"
sleep 40s
wait_until_all_started
if [ "$retval" -lt $max_nof ]; then
	echo "kill unstarted nodes"
	cd "$BASE_DIR" || exit
	"$BASE_DIR"/_killUnstartedNodes.sh
	echo "start unstarted nodes"
	cd "$BASE_DIR" || exit
	"$BASE_DIR"/startNodes.sh
fi

