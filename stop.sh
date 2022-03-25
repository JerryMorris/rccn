#!/bin/sh

DIR=`dirname "$0"`
cd "${DIR}"

# setenv.sh can be locally used to provide environment variables values
if [ -r ./setenv.sh ]; then
  . ./setenv.sh
fi

if [ -z "${NXT_PID_FILE}" ]; then
    NXT_PID_FILE=~/.nxt/nxt.pid
fi

if [ -e ${NXT_PID_FILE} ]; then
    PID=`cat ${NXT_PID_FILE}`
    ps -p $PID > /dev/null
    STATUS=$?
    echo "stopping"
    while [ $STATUS -eq 0 ]; do
        kill `cat ${NXT_PID_FILE}` > /dev/null
        sleep 5
        ps -p $PID > /dev/null
        STATUS=$?
    done
    rm -f ${NXT_PID_FILE}
    echo "Nxt server stopped"
fi

cd - > /dev/null
