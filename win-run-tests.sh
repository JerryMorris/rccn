#!/bin/sh
CP="conf/;classes/;lib/*;testlib/*"
SP="src/java/;test/java/"
TESTS="rcc.crypto.Curve25519Test rcc.crypto.ReedSolomonTest"

/bin/rm -f rcc.jar
/bin/rm -rf classes
/bin/mkdir -p classes/

javac -encoding utf8 -sourcepath $SP -classpath $CP -d classes/ src/java/rcc/*.java src/java/rcc/*/*.java test/java/rcc/*/*.java || exit 1

java -classpath $CP org.junit.runner.JUnitCore $TESTS

