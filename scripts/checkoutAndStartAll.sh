#!/bin/bash
BASEDIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" >/dev/null 2>&1 && pwd )"
. $BASEDIR/env.sh

cd $BASEDIR
$BASEDIR/checkoutAndDeployNodes.sh
$BASEDIR/startAll.sh
