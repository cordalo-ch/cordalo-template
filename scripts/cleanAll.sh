#!/bin/bash
BASE_DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" >/dev/null 2>&1 && pwd )"
. "$BASE_DIR"/env.sh

cd "$BASE_DIR" || exit
"$BASE_DIR"/stopForceAll.sh
cd $CORDA_HOME
rm -Rf `find . -type d -name build -o -name out -o -name logs`

cd "$BASE_DIR" || exit
"$BASE_DIR"/checkoutCleanAndDeployNode.sh
"$BASE_DIR"/startAll.sh
"$BASE_DIR"/status.sh

