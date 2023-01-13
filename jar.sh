#!/bin/sh
if [ -x jdk/bin/java ]; then
    JAVA=./jdk/bin/java
    JAR=./jdk/bin/jar
elif [ -x ../jdk/bin/java ]; then
    JAVA=../jdk/bin/java
    JAR=../jdk/bin/jar
else
    JAVA=java
    JAR=jar
fi
${JAVA} -cp classes rcc.tools.ManifestGenerator
/bin/rm -f rcc.jar
${JAR} cfm rcc.jar resource/rcc.manifest.mf -C classes . || exit 1
/bin/rm -f rccservice.jar
${JAR} cfm rccservice.jar resource/rccservice.manifest.mf -C classes . || exit 1

echo "jar files generated successfully"