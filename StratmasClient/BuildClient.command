#!/bin/sh

# Make sure CD is where this file is.
pushd "`dirname "$0"`"

# Run ant
ant clean 
ant -DinstallPrefix=install install

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
popd
