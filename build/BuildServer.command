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
		UNAME2=`uname -m`
		if [ $UNAME2 = x86_64 ]; then
			PLATFORM=x86_64
		else
			PLATFORM=i386
		fi
    elif echo "$UNAME" | grep -q -i "cygwin"; then
	PLATFORM=cygwin
    else
	echo Stratmas does not support the \'$UNAME\' operating system through this script.
	exit 1
    fi
fi

# Setup dependencies
# Xerces
#XERCESVERSION=2.8.0
#tar zxfC \
#    "$DEPDIR/xerces/$XERCESVERSION/xerces-$XERCESVERSION-$PLATFORM.tar.gz" \
#    "$DEPDIR/xerces/$XERCESVERSION"
#XERCESCROOT="$DEPDIR/xerces/$XERCESVERSION/$PLATFORM"
# Boost
#BOOSTVERSION=1.55.0
#tar zxfC \
#    "$DEPDIR/boost/$BOOSTVERSION/boost-$BOOSTVERSION-$PLATFORM.tar.gz" \
#    "$DEPDIR/boost/$BOOSTVERSION"
#BOOSTROOT="$DEPDIR/boost/$BOOSTVERSION/$PLATFORM"

# Run make
#make -f GNUmakefile "BOOSTROOT=$BOOSTROOT" "XERCESCROOT=$XERCESCROOT" \
#    DEBUG=0 clean
#make -f GNUmakefile "BOOSTROOT=$BOOSTROOT" "XERCESCROOT=$XERCESCROOT" \
#    DEBUG=0 depend
#make -f GNUmakefile "BOOSTROOT=$BOOSTROOT" "XERCESCROOT=$XERCESCROOT" \
#    DEBUG=0 all
make -f GNUmakefile     DEBUG=0 clean
make -f GNUmakefile    DEBUG=0 depend
make -j4 -f GNUmakefile    DEBUG=0 all

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
