#!/bin/bash

i=10103
nodes=( $NodeNames )
for n in "${nodes[@]}"
do
  echo "check Node $n"
  sshpass -p test ssh user1@localhost -p $i -oStrictHostKeyChecking=accept-new -oUserKnownHostsFile=/dev/null
  (( i=i+3 ))
done
