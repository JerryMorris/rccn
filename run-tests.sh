#!/bin/sh
PATHSEP=":"
if [ "$OSTYPE" = "cygwin" ] ; then
PATHSEP=";"
fi

CP=conf/${PATHSEP}classes/${PATHSEP}lib/*${PATHSEP}testlib/*
SP=src/java/${PATHSEP}test/java/

if [ $# -eq 0 ]; then
TESTS="rcc.crypto.Curve25519Test rcc.crypto.ReedSolomonTest rcc.peer.HallmarkTest rcc.TokenTest rcc.FakeForgingTest
rcc.FastForgingTest rcc.ManualForgingTest"
else
TESTS=$@
fi

/bin/rm -f rcc.jar
/bin/rm -rf classes
/bin/mkdir -p classes/

javac -encoding utf8 -sourcepath ${SP} -classpath ${CP} -d classes/ src/java/rcc/*.java src/java/rcc/*/*.java test/java/rcc/*.java test/java/rcc/*/*.java || exit 1

for TEST in ${TESTS} ; do
java -classpath ${CP} org.junit.runner.JUnitCore ${TEST} ;
done



