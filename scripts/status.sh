#!/bin/bash
BASE_DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" >/dev/null 2>&1 && pwd )"
. "$BASE_DIR"/env.sh

echo "---------------------------------------------------------------------------------------------"
echo "Currently $(netstat -an | egrep "${NodeSSHPorts}" | grep LISTEN | wc -l) CORDA nodes running"
echo "Currently $(netstat -an | egrep "${NodeWebPorts}" | grep LISTEN | wc -l) Webservers running"
