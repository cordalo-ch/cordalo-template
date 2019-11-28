#!/bin/bash
BASE_DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" >/dev/null 2>&1 && pwd )"
. "$BASE_DIR"/env.sh

i=${NodeSSHPortStart}
nodes=( $NodeNames )
for n in "${nodes[@]}"
do
  echo "check Node $n"
  # TODO sshpass do not exist in cycgwin, in macos, in windows -> build form source required! get rid of that dependencies
  sshpass -p test ssh user1@localhost -p $i -oStrictHostKeyChecking=accept-new -oUserKnownHostsFile=/dev/null
  (( i=i+3 ))
done
