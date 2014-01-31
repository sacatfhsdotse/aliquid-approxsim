#!/bin/sh

# Go to the Dispatcher source code directory.
cd "`dirname \"$0\"`/StratmasDispatcher"

# Run make
make clean jar

# Copy the jar to the correct directory if make succeeded (if
# the retrun status was 0, that is), else print error message.
if [ $? -eq 0 ]; then 
    echo =============================================================
    echo Build completed. Copying jar...
    cp classes/StratmasDispatcher.jar ../../
    echo Done!
    echo =============================================================
else
    echo =============================================================
    echo The build was incomplete. No new jar has been created.
    echo =============================================================
fi
