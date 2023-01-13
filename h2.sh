#!/bin/sh

if [ -x jdk/bin/java ]; then
    JAVA=./jdk/bin/java
else
    JAVA=java
fi

COMMAND="${JAVA} -cp lib/h2*.jar org.h2.tools.Shell -url jdbc:h2:./rcc_db/rcc -user sa -password sa"

if [ "$#" -gt 0 ]; then
    ${COMMAND} -sql "$1"
else
    ${COMMAND}
fi

