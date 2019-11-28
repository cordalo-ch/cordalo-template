#!/bin/bash
BASE_DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" >/dev/null 2>&1 && pwd )"
. "$BASE_DIR"/env.sh

cd "$BASE_DIR" || exit
"$BASE_DIR"/stopAll.sh
"$BASE_DIR"/checkoutAndDeployNode.sh
"$BASE_DIR"/startAll.sh
"$BASE_DIR"/status.sh

