#!/bin/bash
BASE_DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" >/dev/null 2>&1 && pwd )"
. "$BASE_DIR"/env.sh

cd "$BASE_DIR" || exit
"$BASE_DIR"/startNodes.sh
"$BASE_DIR"/startServers.sh
"$BASE_DIR"/status.sh

