#!/bin/bash
BASEDIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" >/dev/null 2>&1 && pwd )"
. $BASEDIR/env.sh

cd $CORDA_HOME
rm -rf ./build ./out
git pull
./gradlew clean deployNodes
cd ~
