#!/bin/sh

echo "************************************************"
echo "** DEPRECATED: Use 'run.sh --desktop' instead **"
echo "************************************************"
sleep 1

if [ -x jdk/bin/java ]; then
    JAVA=./jdk/bin/java
else
    JAVA=java
fi
${JAVA} -cp classes:lib/*:conf:addons/classes:addons/lib/*:javafx-sdk/lib/* -Drcc.runtime.mode=desktop -Drcc.runtime.dirProvider=rcc.env.DefaultDirProvider rcc.rcc
