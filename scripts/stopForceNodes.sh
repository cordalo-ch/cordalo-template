#!/bin/bash
BASE_DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" >/dev/null 2>&1 && pwd )"
. "$BASE_DIR"/env.sh

cd "$BASE_DIR" || exit
nof=`$BASE_DIR/killNodes.sh | wc -l`
if [ "$nof" -gt 0 ]; then
	echo "Nodes killed: $nof (wait 5s) $(eval `./killNodes.sh`)"
	sleep 5s
fi
cd "$BASE_DIR" || exit
