#!/bin/bash
BASEDIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" >/dev/null 2>&1 && pwd )"
. $BASEDIR/env.sh

cd $CORDA_HOME
get_sshd(){
	retval=$(netstat -an | egrep "${NodeSSHPorts}" | grep LISTEN | wc -l)
}
get_webd(){
        retval=$(netstat -an | egrep "${NodeWebPorts}" | grep LISTEN | wc -l)
}

get_sshd
nof=$retval

get_webd
nof2=$retval
echo "Currently $nof CORDA nodes running"
echo "Currently $nof2 Webservers  running"
