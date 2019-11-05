#!/bin/bash
BASEDIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" >/dev/null 2>&1 && pwd )"
. $BASEDIR/env.sh

ps -ef | grep corda | grep -v ".webserver.Starter" | grep java | grep -v grep | grep -v IntelliJ | awk '{print "kill -9 " $2 ";"  }'
