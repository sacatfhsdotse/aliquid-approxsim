#!/bin/sh

# Go to the Stratmas server source code directory.
cd "`dirname "$0"`"
cd StratmasClient

# Run ant
ant clean 
ant -DinstallPrefix=../../ install

# Copy the executable to the correct directory if make succeeded (if
# the retrun status was 0, that is), else print error message.
if [ $? -eq 0 ]; then 
    echo =============================================================
    echo Build completed.
    echo =============================================================
else
    echo =============================================================
    echo The build was incomplete.
    echo =============================================================
fi
