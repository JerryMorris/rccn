#!/bin/sh

echo "***********************************************"
echo "** DEPRECATED: Use 'run.sh --desktop' instead **"
echo "***********************************************"
sleep 1

if [ -e ~/.rcc/rcc.pid ]; then
    PID=`cat ~/.rcc/rcc.pid`
    ps -p $PID > /dev/null
    STATUS=$?
    if [ $STATUS -eq 0 ]; then
        echo "rcc server already running"
        exit 1
    fi
fi
mkdir -p ~/.rcc/
DIR=`dirname "$0"`
cd "${DIR}"
if [ -x jdk/bin/java ]; then
    JAVA=./jdk/bin/java
else
    JAVA=java
fi
nohup ${JAVA} -cp classes:lib/*:conf:addons/classes:addons/lib/*:javafx-sdk/lib/* -Drcc.runtime.mode=desktop rcc.rcc > /dev/null 2>&1 &
echo $! > ~/.rcc/rcc.pid
cd - > /dev/null
