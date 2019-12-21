#!/bin/bash
BASE_DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" >/dev/null 2>&1 && pwd )"
. "$BASE_DIR"/env.sh

cd "$CORDA_HOME" || exit
git pull
"$CORDA_HOME"/gradlew deployNodes
cd ~
