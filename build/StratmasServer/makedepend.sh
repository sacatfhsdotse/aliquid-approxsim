#!/bin/bash

# $Id: makedepend.sh,v 1.6 2006/07/17 09:01:02 dah Exp $

# BUGS 
# * '#' may not appear in filenames
# * -e  prefixes with spaces in them may not work very well

CMD="`basename $0`"

# Defaults
CCMD=g++
VERBOSE=0
EXCLUDELINES=""
LISTMODE=""
OUTFILE="/dev/stdout"

while getopts c:e:o:lv ARG; do
    case "$ARG" in
        c)
	    CCMD="$OPTARG"
            ;;
        e)
	    if [[ -z "$LISTMODE" ]]; then
	    #Note that last '"' is supposed to be on the next line.
		EXCLUDELINES="$EXCLUDELINES\\#$OPTARG# s#$OPTARG[^[:space:]]*##g
"
	    elif [[ -n "$EXCLUDELINES" ]]; then
		echo "$CMD: only one -e argument accepted with -l" 1>&2
		exit 1
	    else
		EXCLUDELINES="$OPTARG"
	    fi
            ;;
        o)
	    OUTFILE="$OPTARG"
	    ;;
	l)
	    if [[ -n "$EXCLUDELINES" ]]; then
		echo "$CMD: -l must appear before any -e argument" 1>&2
		exit 1
	    else 
		LISTMODE="1"
	    fi
	    ;;
	v)  
	    # Verbose, to be implemented
	    ;;
        *)
	    echo "$CMD [-c ccmd] [-o outfile] [-l] [-e excludepath1 -e " \
		 " excludepath2 ...] [-v] files..." 1>&2
            exit 1
            ;;
    esac
done
shift $((${OPTIND} - 1))

# Temp file for gcc output.
TMPFILE=$(mktemp "/tmp/$CMD.XXXXXXXXXX") || exit 1

if [[ -z "$LISTMODE" ]]; then
# Sed line to ensure that the last line does not contain /\$/
    FIXEND="\$ s/\\\\$//"

# Add rule removing empty lines
    EXCLUDELINES="$EXCLUDELINES\\#^[[:space:]]*\\\[[:space:]]*\$# D
/^[[:space:]]*$/ D"

    FIRST=1
    for file in $@; do
	$CCMD -MM -MF "$TMPFILE" $file || exit 1
	if [[ "$FIRST" == 1 ]]; then  
	    sed "$EXCLUDELINES" "$TMPFILE" | sed "$FIXEND" >"$OUTFILE" || \
		exit 1
	    FIRST=0
	else
	    sed "$EXCLUDELINES" "$TMPFILE" | sed "$FIXEND" >>"$OUTFILE" || \
		exit 1
	fi
    done
else
    TMPOUT=$(mktemp "/tmp/$CMD.XXXXXXXXXX") || exit 1
    touch "$TMPOUT"
    for file in $@; do
	$CCMD -MM -MF "$TMPFILE" $file || exit 1
	grep -o "$EXCLUDELINES[^[:space:]]*" "$TMPFILE" >> "$TMPOUT"
    done
    sort "$TMPOUT" |uniq > "$OUTFILE"
    rm -f "$TMPOUT"
fi

rm -f $TMPFILE || exit 1
