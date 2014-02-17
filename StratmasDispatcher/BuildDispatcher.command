#!/bin/sh

# Go to the Dispatcher source code directory.
pushd "`dirname \"$0\"`"

# Run make
make clean jar

# Copy the jar to the correct directory if make succeeded (if
# the retrun status was 0, that is), else print error message.
if [ $? -eq 0 ]; then 
    echo =============================================================
    echo Build completed. Copying jar...
    cp classes/StratmasDispatcher.jar ../StratmasServer/bin/
    echo Done!
    echo =============================================================
else
    echo =============================================================
    echo The build was incomplete. No new jar has been created.
    echo =============================================================
fi

popd
