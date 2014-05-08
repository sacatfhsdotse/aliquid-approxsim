#!/bin/sh

HOMEDIR="`dirname \"$0\"`"
JARFILE="$HOMEDIR/ApproxsimDispatcher.jar"

# Any command line arguments to the dispatcher goes here. For example:
# java -jar ApproxsimDispatcher.jar -p 4711
java -jar "$JARFILE"
