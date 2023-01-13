#!/bin/sh
echo "***********************************************************************"
echo "* Use this shell script to search for a lost passphrase.              *"
echo "*                                                                     *"
echo "* When using desktop mode, invoke this script as:                     *"
echo "* ./passphraseRecovery.sh -Drcc.runtime.mode=desktop                  *"
echo "***********************************************************************"
if [ -x jdk/bin/java ]; then
    JAVA=./jdk/bin/java
else
    JAVA=java
fi
${JAVA} -Xmx1024m -cp "classes:lib/*:conf" $@ rcc.tools.PassphraseRecovery
exit $?
