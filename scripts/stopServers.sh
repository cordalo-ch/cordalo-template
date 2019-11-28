#!/bin/bash
BASE_DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" >/dev/null 2>&1 && pwd )"
. "$BASE_DIR"/env.sh

cd "$BASE_DIR" || exit
nof=`$BASE_DIR/killServers.sh | wc -l`
if [ "$nof" -gt 0 ]; then
	echo "servers killed: $nof (sleep 5s) $(eval `$BASE_DIR/killServers.sh`)"
	sleep 5s
fi
cd "$BASE_DIR" || exit
