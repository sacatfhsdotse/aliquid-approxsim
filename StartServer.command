#!/bin/sh

# Go to the directory where the Stratmas server executable is located.
cd "`dirname "$0"`"

# Find out the which platform we run on.
UNAME=`uname`
if [ $UNAME = Darwin ]; then    
    CURRENT_PLATFORM=ppc_macosx
elif [ $UNAME = Linux ]; then    
    CURRENT_PLATFORM=i386_rh9
elif echo "$UNAME" | grep -q -i "cygwin"; then
    CURRENT_PLATFORM=win
else
    echo Stratmas does not support the \'$UNAME\' operating system through this script.
    exit 1
fi

# Set path to dynamically linked libraries.
export DYLD_LIBRARY_PATH=dependencies/xerces/2.7.0/$CURRENT_PLATFORM/lib
export LD_LIBRARY_PATH=dependencies/xerces/2.7.0/$CURRENT_PLATFORM/lib

# Any command line arguments to the server goes here. For example:
# stratmas_$CURRENT_PLATFORM -p 29000
./stratmas_$CURRENT_PLATFORM $*
