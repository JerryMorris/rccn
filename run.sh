#!/bin/sh

desktop=0
authbind=0
daemon=0

help()
{
    echo "Parameters:"
    echo
    echo "  --desktop : Force desktop mode in the current directory."
    echo "  --daemon  : Start in daemon mode (background). Use stop.sh to stop the node."
    echo "  --authbind: Use authbind (installed separately) to allow binding of privileged ports."
    exit
}

while [ "$1" != "" ]; do
    case $1 in
        --desktop )    desktop=1
                       ;;
        --authbind )   authbind=1
                       ;;
        --daemon )     daemon=1
                       ;;
        * )            help
                       ;;
    esac
    shift
done

if [ $desktop -eq 1 ] && [ $daemon -eq 1 ]; then
    echo "You can't start in desktop and daemon mode at the same time. Pick one."
    exit 1
fi

DIR=`dirname "$0"`
cd "${DIR}"

# setenv.sh can be locally used to provide environment variables values
if [ -r ./setenv.sh ]; then
  . ./setenv.sh
fi

if [ -x jdk/bin/java ]; then
    JAVACMD=./jdk/bin/java
else
    JAVACMD=java
fi

if [ $authbind -eq 1 ]; then
    JAVACMD="authbind ${JAVACMD}"
fi

JVM_OPTS=-Xms256M
if [ -n "${rcc_JVM_OPTS}" ]; then
    echo "JVM options: ${rcc_JVM_OPTS}"
    JVM_OPTS=${rcc_JVM_OPTS}
fi

if [ -z "${rcc_PID_FILE}" ]; then
    rcc_PID_FILE=~/.rcc/rcc.pid
fi

if [ $desktop -eq 1 ]; then
    echo "Starting desktop mode in current directory"
    ${JAVACMD} ${JVM_OPTS} -cp classes:lib/*:conf:addons/classes:addons/lib/*:javafx-sdk/lib/* -Drcc.runtime.mode=desktop -Drcc.runtime.dirProvider=rcc.env.DefaultDirProvider rcc.rcc
elif [ $daemon -eq 1 ]; then
    echo "Starting daemon mode"
    if [ -e ${rcc_PID_FILE} ]; then
        PID=`cat ${rcc_PID_FILE}`
        ps -p $PID > /dev/null
        STATUS=$?
        if [ $STATUS -eq 0 ]; then
            echo "rcc server already running"
            exit 1
        fi
    fi
    mkdir -p "$(dirname "${rcc_PID_FILE}")"
    nohup ${JAVACMD} ${JVM_OPTS} -cp classes:lib/*:conf:addons/classes:addons/lib/*:javafx-sdk/lib/* rcc.rcc > /dev/null 2>&1 &
    echo $! > ${rcc_PID_FILE}
else
    echo "Starting default mode"
    ${JAVACMD} ${JVM_OPTS} -cp classes:lib/*:conf:addons/classes:addons/lib/*:javafx-sdk/lib/* rcc.rcc
fi

cd - > /dev/null
