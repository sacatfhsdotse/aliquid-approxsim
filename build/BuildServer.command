#!/bin/bash
# Go to the Stratmas server source code directory.
cd $(dirname "$0")
cd StratmasServer

DEPDIR="../../dependencies"

# If not defined, find out the which platform we run on.
if [[ -z "$PLATFORM" ]]; then
    UNAME=`uname`
    if [ $UNAME = Darwin ]; then    
	PLATFORM=ppc_macosx
    elif [ $UNAME = Linux ]; then    
	PLATFORM=i386_rh9
    elif echo "$UNAME" | grep -q -i "cygwin"; then
	PLATFORM=cygwin
    else
	echo Stratmas does not support the \'$UNAME\' operating system through this script.
	exit 1
    fi
fi

# Setup dependencies
# Xerces
XERCESVERSION=2.7.0
tar zxfC \
    "$DEPDIR/xerces/$XERCESVERSION/xerces-$XERCESVERSION-$PLATFORM.tar.gz" \
    "$DEPDIR/xerces/$XERCESVERSION"
XERCESCROOT="$DEPDIR/xerces/$XERCESVERSION/$PLATFORM"
# Boost
BOOSTVERSION=1.33.1
tar zxfC \
    "$DEPDIR/boost/$BOOSTVERSION/boost-$BOOSTVERSION-$PLATFORM.tar.gz" \
    "$DEPDIR/boost/$BOOSTVERSION"
BOOSTROOT="$DEPDIR/boost/$BOOSTVERSION/$PLATFORM"

# Run make
make -f GNUmakefile "BOOSTROOT=$BOOSTROOT" "XERCESCROOT=$XERCESCROOT" \
    DEBUG=0 clean
make -f GNUmakefile "BOOSTROOT=$BOOSTROOT" "XERCESCROOT=$XERCESCROOT" \
    DEBUG=0 depend
make -f GNUmakefile "BOOSTROOT=$BOOSTROOT" "XERCESCROOT=$XERCESCROOT" \
    DEBUG=0 all

# Copy the executable to the correct directory if make succeeded (if
# the retrun status was 0, that is), else print error message.
if [ $? -eq 0 ]; then 
    echo =============================================================
    echo Build completed. Copying executable...
    cp stratmas ../../stratmas_$PLATFORM
    echo Done!
    echo =============================================================
else
    echo =============================================================
    echo The build was incomplete. No new executable has been created.
    echo =============================================================
fi
