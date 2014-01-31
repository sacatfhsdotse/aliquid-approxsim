#!/bin/sh

cd "`dirname "$0"`"

# Find out the which platform we run on.
UNAME=`uname`
if [ $UNAME = Darwin ]; then    
    CURRENT_PLATFORM=ppc_macosx
elif [ $UNAME = Linux ]; then    
    CURRENT_PLATFORM=i386_rh9
else
    echo Stratmas does not support the \'$UNAME\' operating system through this script.
    exit 1
fi

# Start server
cd ../../../StratmasServer
./StartServer.command &

# Start client
cd ../StratmasClient

# If the first argument is non null it should be the name of the
# indata file to use.
if [ -z $1 ]; then
    ./StartClient.command
else
    ./StartClient.command -batch=localhost,0s,/dev/null "$1"
fi

# Stop the server
killall stratmas_$CURRENT_PLATFORM
