#!/bin/sh

DIR=`dirname "$0"`
cd "${DIR}"

# setenv.sh can be locally used to provide environment variables values
if [ -r ./setenv.sh ]; then
  . ./setenv.sh
fi

if [ -z "${rcc_PID_FILE}" ]; then
    rcc_PID_FILE=~/.rcc/rcc.pid
fi

if [ -e ${rcc_PID_FILE} ]; then
    PID=`cat ${rcc_PID_FILE}`
    ps -p $PID > /dev/null
    STATUS=$?
    echo "stopping"
    while [ $STATUS -eq 0 ]; do
        kill `cat ${rcc_PID_FILE}` > /dev/null
        sleep 5
        ps -p $PID > /dev/null
        STATUS=$?
    done
    rm -f ${rcc_PID_FILE}
    echo "rcc server stopped"
fi

cd - > /dev/null
