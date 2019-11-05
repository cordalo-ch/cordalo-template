#!/bin/bash
BASEDIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" >/dev/null 2>&1 && pwd )"
. $BASEDIR/env.sh


i=${NodeSSHPortStart}
nodes=( $NodeNames )
for n in "${nodes[@]}"
do
  echo "check State $n"
  sshpass -p test ssh user1@localhost -p $i -oStrictHostKeyChecking=accept-new -oUserKnownHostsFile=/dev/null run gracefulShutdown
  (( i=i+3 ))
done
