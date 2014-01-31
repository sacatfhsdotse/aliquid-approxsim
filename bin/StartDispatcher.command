#!/bin/sh

HOMEDIR="`dirname \"$0\"`"
JARFILE="$HOMEDIR/StratmasDispatcher.jar"

# Any command line arguments to the dispatcher goes here. For example:
# java -jar StratmasDispatcher.jar -p 4711
java -jar "$JARFILE"
