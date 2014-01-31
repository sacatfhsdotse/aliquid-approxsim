#!/bin/bash

CMD=`basename $0`
DIR="$1"
CLASSPATH="$2"

if [[ ! -d "$DIR" ]]; then
    echo "$CMD: Error: BUILDDIR \"$DIR\" is not directory." 2>&1
    exit 1;
fi

cd "$DIR" || exit 1;

OLDIFS="$IFS"
IFS=":"
for JAR in $CLASSPATH; do 
    IFS="OLDIFS"
    if [[ -f "$JAR" ]]; then
	echo Processing "$JAR"
	jar xf "$JAR"
    fi
done
IFS="$OLDIFS"

# Recreate META-INF
rm -rf META-INF
echo Packaging StratmasClient.jar
jar cfm StratmasClient.jar ../Manifest *
echo "Creating index in" StratmasClient.jar
jar -i StratmasClient.jar
