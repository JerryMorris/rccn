#!/bin/bash
VERSION=$1
if [ -x ${VERSION} ];
then
	echo VERSION not defined
	exit 1
fi
PACKAGE=rcc-client-${VERSION}
echo PACKAGE="${PACKAGE}"
CHANGELOG=rcc-client-${VERSION}.changelog.txt
OBFUSCATE=$2
MACVERSION=$3
if [ -x ${MACVERSION} ];
then
MACVERSION=${VERSION}
fi
echo MACVERSION="${MACVERSION}"

# Force Java 8 even if a newer one is the default. Or else the installer will be broken is
export JAVA_HOME=`/usr/libexec/java_home -v 1.8`

export JAVA_TOOL_OPTIONS="$JAVA_TOOL_OPTIONS -Dfile.encoding=UTF8"

FILES="changelogs conf html lib resource contrib"
FILES="${FILES} rcc.exe rccservice.exe"
FILES="${FILES} 3RD-PARTY-LICENSES.txt AUTHORS.txt LICENSE.txt"
FILES="${FILES} DEVELOPERS-GUIDE.md OPERATORS-GUIDE.md README.md README.txt USERS-GUIDE.md"
FILES="${FILES} mint.bat mint.sh run.bat run.sh run-tor.sh run-desktop.sh start.sh stop.sh compact.sh compact.bat sign.sh sign.bat passphraseRecovery.sh passphraseRecovery.bat pem.to.pkcs12.keystore.certbot.hook.sh"
FILES="${FILES} rcc.policy rccdesktop.policy rcc_Wallet.url Dockerfile"

echo compile
./compile.sh
rm -rf html/doc/*
rm -rf rcc
rm -rf ${PACKAGE}.jar
rm -rf ${PACKAGE}.exe
rm -rf ${PACKAGE}.zip
mkdir -p rcc/
mkdir -p rcc/logs
mkdir -p rcc/addons/src

if [ "${OBFUSCATE}" = "obfuscate" ]; 
then
echo obfuscate
~/proguard/proguard5.3.3/bin/proguard.sh @rcc.pro
mv ../rcc.map ../rcc.map.${VERSION}
else
FILES="${FILES} classes src JPL-NRS.pdf"
FILES="${FILES} compile.sh javadoc.sh jar.sh package.sh"
echo javadoc
./javadoc.sh
fi
echo copy resources
cp installer/lib/JavaExe.exe rcc.exe
cp installer/lib/JavaExe.exe rccservice.exe
cp -a ${FILES} rcc
cp -a logs/placeholder.txt rcc/logs
echo gzip
for f in `find rcc/html -name *.gz`
do
	rm -f "$f"
done
for f in `find rcc/html -name *.html -o -name *.js -o -name *.css -o -name *.json  -o -name *.ttf -o -name *.svg -o -name *.otf`
do
	gzip -9c "$f" > "$f".gz
done
cd rcc
echo generate jar files
../jar.sh
echo package installer Jar
../installer/build-installer.sh ../${PACKAGE}
cd -
rm -rf rcc

echo bundle a dmg file	
$JAVA_HOME/bin/javapackager -deploy -outdir . -outfile rcc-client -name rcc-installer -width 34 -height 43 -native dmg -srcfiles ${PACKAGE}.jar -appclass com.izforge.izpack.installer.bootstrap.Installer -v -Bmac.category=Business -Bmac.CFBundleIdentifier=org.rcc.client.installer -Bmac.CFBundleName=rcc-Installer -Bmac.CFBundleVersion=${MACVERSION} -BappVersion=${MACVERSION} -Bicon=installer/AppIcon.icns > installer/javapackager.log 2>&1

mv bundles/ardor-installer-${MACVERSION}.dmg bundles/ardor-installer-${VERSION}.dmg