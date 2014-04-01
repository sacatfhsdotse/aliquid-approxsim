#!/bin/sh

# Go to the directory where the Stratmas client executable is located.
cd "`dirname "$0"`"

# Find out the which platform we run on and set the LIBPATH to point
# to the lib directory for that platform.
UNAME=`uname`
if [ $UNAME = Darwin ]; then    
    LIBPATH="StratmasClient/lib/Mac OS X-ppc"
elif [ $UNAME = Linux ]; then    
    LIBPATH=StratmasClient/lib/Linux-i386
elif echo "$UNAME" | grep -q -i "cygwin"; then
    LIBPATH="StratmasClient/lib/Windows XP-x86"
else
    echo Stratmas does not support the \'$UNAME\' operating system through this script.
    exit 1
fi

# Start the client.
java -Xmx300m "-Djava.library.path=$LIBPATH" -cp StratmasClient StratmasClient.Client -noJoglResolve $*
