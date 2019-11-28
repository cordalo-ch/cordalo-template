#!/bin/bash
BASE_DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" >/dev/null 2>&1 && pwd )"
. "$BASE_DIR"/env.sh

i=${NodeSSHPortStart}
nodes=( $NodeNames )
for n in "${nodes[@]}"
do
  echo "check State $n"
  sshpass -p test ssh user1@localhost -p $i -oStrictHostKeyChecking=accept-new -oUserKnownHostsFile=/dev/null run vaultQuery contractStateType: ch.cordalo.template.states.ServiceState
  (( i=i+3 ))
done
