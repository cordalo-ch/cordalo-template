#!/bin/bash
BASE_DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" >/dev/null 2>&1 && pwd )"
. "$BASE_DIR"/env.sh

list=$(jps | awk '{print $1}')
if [ -z "$list" ]
then
  echo "There is no jvm running"
else
  while read -r line; do
      echo "stopping jvm with pid $line"
      kill -9 $line
  done <<< "$list"
fi