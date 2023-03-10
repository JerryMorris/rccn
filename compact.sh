#!/bin/sh
echo "***********************************************************************"
echo "* This shell script will compact and reorganize the rcc NRS database. *"
echo "* This process can take a long time.  Do not interrupt the script     *"
echo "* or shutdown the computer until it finishes.                         *"
echo "*                                                                     *"
echo "* To compact the database used while in a desktop mode, i.e. located  *"
echo "* under ~/.rcc/ , invoke this script as:                              *"
echo "* ./compact.sh -Drcc.runtime.mode=desktop                             *"
echo "***********************************************************************"

if [ -x jdk/bin/java ]; then
    JAVA=./jdk/bin/java
else
    JAVA=java
fi

${JAVA} -Xmx1024m -cp "classes:lib/*:conf" $@ rcc.tools.CompactDatabase
exit $?
