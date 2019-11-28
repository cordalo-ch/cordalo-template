#!/bin/bash
BASE_DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" >/dev/null 2>&1 && pwd )"
. "$BASE_DIR"/env.sh

ps -ef | grep ".webserver.Starter" | grep java | grep -v grep | grep -v IntelliJ | awk '{print "kill -9 " $2 ";" }'
