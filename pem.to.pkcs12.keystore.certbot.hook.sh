#!/bin/sh

#######################################################################
## This script takes the pem files from a Let's Encrypt / Certbot
## directory and bundles them together for use by the current NRS
## installation, reading the corresponding properties from the
## rcc.properties file.
##
## It is designed to be run from the --deploy-hook Certbot option,
## meaning that it expects the RENEWED_LINEAGE environment variable
## to point to a directory with the PEM encoded files.
#######################################################################

PROPERTIES_PATH="conf/rcc.properties"

if [ -z $RENEWED_LINEAGE ]; then
	echo "RENEWED_LINEAGE environment variable not found, running from certbot --deploy-hook ?"
	exit
fi

OLD_DIR="$(pwd)"
SCRIPTPATH="$( cd "$(dirname "$0")" ; pwd -P )"
cd $SCRIPTPATH

if [ ! -r $PROPERTIES_PATH ]; then
	echo "rcc.properties file not found"
	exit
fi

KEYSTORE=$(grep "^rcc.keyStorePath=" $PROPERTIES_PATH | cut -d'=' -f2)

if [ -z $KEYSTORE ]; then
	echo "You need to define rcc.keyStorePath on rcc.properties"
	exit
fi

KEYSTORE_PASS=$(grep "^rcc.keyStorePassword=" $PROPERTIES_PATH | cut -d'=' -f2)

if [ -z $KEYSTORE_PASS ]; then
	echo "You need to define rcc.keyStorePassword on rcc.properties"
	exit
fi

KEYSTORE_TYPE=$(grep "^rcc.keyStoreType=" $PROPERTIES_PATH | cut -d'=' -f2)

if [ -z $KEYSTORE_TYPE ] || [ $KEYSTORE_TYPE != "PKCS12" ]; then
	echo "You need to define the keystore type as PKCS12. Add \"rcc.keyStoreType=PKCS12\" to your rcc.properties file "
	exit
fi

openssl pkcs12 -export -in $RENEWED_LINEAGE/fullchain.pem -inkey $RENEWED_LINEAGE/privkey.pem -out $KEYSTORE -name nrs -passout pass:$KEYSTORE_PASS
chmod a+r $KEYSTORE

cd $OLD_DIR
