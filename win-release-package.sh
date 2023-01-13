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

FILES="changelogs conf html lib resource contrib logs"
FILES="${FILES} rcc.exe rccservice.exe"
FILES="${FILES} 3RD-PARTY-LICENSES.txt AUTHORS.txt LICENSE.txt"
FILES="${FILES} DEVELOPERS-GUIDE.md OPERATORS-GUIDE.md README.md README.txt USERS-GUIDE.md"
FILES="${FILES} mint.bat mint.sh run.bat run.sh run-tor.sh run-desktop.sh start.sh stop.sh compact.sh compact.bat sign.sh sign.bat passphraseRecovery.sh passphraseRecovery.bat pem.to.pkcs12.keystore.certbot.hook.sh"
FILES="${FILES} rcc.policy rccdesktop.policy rcc_Wallet.url Dockerfile"

# unix2dos *.bat
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

if [ "${OBFUSCATE}" == "obfuscate" ];
then
echo obfuscate
proguard.bat @rcc.pro
mv ../rcc.map ../rcc.map.${VERSION}
mkdir -p rcc/src/
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
echo create installer exe
../installer/build-exe.bat ${PACKAGE}
echo create installer zip
cd -
zip -q -X -r ${PACKAGE}.zip rcc -x \*/.idea/\* \*/.gitignore \*/.git/\* \*.iml rcc/conf/rcc.properties rcc/conf/logging.properties rcc/conf/localstorage/\*
rm -rf rcc

echo creating change log ${CHANGELOG}
echo -e "Release $1\n" > ${CHANGELOG}
echo -e "https://www.jelurida.com/\n" >> ${CHANGELOG}
echo -e "sha256 checksums:\n" >> ${CHANGELOG}
sha256sum ${PACKAGE}.exe >> ${CHANGELOG}

sha256sum ${PACKAGE}.jar >> ${CHANGELOG}

if [ "${OBFUSCATE}" == "obfuscate" ];
then
echo -e "\n\nThis is an experimental release for testing only. Source code is not provided." >> ${CHANGELOG}
fi
echo -e "\n\nChange log:\n" >> ${CHANGELOG}

cat changelogs/${CHANGELOG} >> ${CHANGELOG}
echo >> ${CHANGELOG}
