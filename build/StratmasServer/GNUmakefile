# $Id: GNUmakefile,v 1.15 2006/08/29 17:18:42 dah Exp $
# Makefile for Posix-like plattforms

# Uncomment to use Intel compiler.
#CXX=icc

# Use GNU compiler collection by default.
ifndef CXX
CXX		= g++
endif

ifeq ($(CXX),icc)
CC		= icc
CXX		= icc
LD		= icc
# Use gcc compiled Boost libraries for now.
BOOSTSUFFIX 	?= -gcc-mt
endif
ifeq ($(CXX),g++)
CC		= gcc
CXX		= g++
LD		= g++
endif

MAKEDEPEND	= ./makedepend.sh
OPTFLAGS	= -O3
OS		?= $(shell uname -s)

BOOSTROOT	?= /afs/pdc.kth.se/projects/stratmas/common/boost/1.33.1
XERCESCROOT     ?= /afs/pdc.kth.se/projects/stratmas/common/xerces/2.7.0

# Boost uses compiler- and threadsuffix on most platforms, but not on
# darwin also ability to switch between dynamic and static linking
# seems to be missing from darwin. Hence we need to construct somewhat
# complicated LDFLAGS lines. To add a boost library, just name it on
# the line below.
BOOSTLIBS	= thread filesystem program_options date_time
ifeq ($(OS),Darwin)
BOOSTSUFFIX	=
BOOSTLDFLAGS	= $(patsubst %,$(BOOSTROOT)/lib/libboost_%$(BOOSTSUFFIX).a,\
		  $(BOOSTLIBS))
else
COMMA		:=,
BOOSTSUFFIX 	?= -$(CC)-mt
BOOSTLDFLAGS	= -L$(BOOSTROOT)/lib -Wl,-Bstatic			\
		  $(patsubst %,-Wl$(COMMA)-lboost_%$(BOOSTSUFFIX),	\
		    $(BOOSTLIBS))					\
		  -Wl,-Bdynamic
endif
CXXFLAGS	+= -Wall -I$(XERCESCROOT)/include -I$(BOOSTROOT)/include
LDFLAGS		+= -L$(XERCESCROOT)/lib -lxerces-c $(BOOSTLDFLAGS)
SERVERNAME	= stratmas

# Debug by default
DEBUG		?= 1

# Redefines for production builds
ifeq ($(DEBUG), 0)
CXXFLAGS	+= $(OPTFLAGS)
else
CXXFLAGS 	+= -ggdb -DDEBUG
# No rpath availiable (or needed) on darwin.
ifneq ($(OS),Darwin)
LDFLAGS		+= -Wl,-rpath -Wl,$(XERCESCROOT)/lib
endif
endif

# Add more libs on solaris
ifeq ($(OS),SunOS)
LDFLAGS		+= -lsocket -lnsl
endif

# -pthread needed on some linux platforms
ifeq ($(OS),Linux)
LDFLAGS		+= -pthread
CXXFLAGS	+= -pthread
endif

all: $(SERVERNAME)

# Add platform specific sourcefiles
PLATFORMSRCS=PosixSocket.cpp SyslogSink.cpp optionsPosix.cpp

# Get definition of SERVERSRCS from Makefile.base
include Makefile.base
SERVEROBJS = $(SERVERSRCS:.cpp=.o)

# If dependencies availiable, use them
ifeq (.depend, $(wildcard .depend))
include .depend
endif

clean:
	rm -rf $(SERVERNAME) $(SERVEROBJS) .depend *~ *.tmp doc/html doc/latex\
	  boostheaders.tar.gz file2h schemas/schemas.h schemas/schemas.cpp

test: $(SERVERNAME)
	./$(SERVERNAME) -h localhost

dTest: $(SERVERNAME)
	./$(SERVERNAME) -h localhost -d localhost

doc/html/index.html: doc/Doxyfile $(wildcard *.h) $(wildcard *.cpp)
	doxygen doc/Doxyfile

doc: doc/html/index.html

schemas/schemas.cpp schemas/schemas.h: file2h $(XMLSCHEMAS)
	(cd schemas; ../file2h schemas.cpp schemas.h $(notdir $(XMLSCHEMAS)))

file2h: file2h.c
	$(CC) -Wall -o file2h $^

%.cpp.d: %.cpp schemas/schemas.h
	$(MAKEDEPEND) -c "$(CXX) $(CXXFLAGS)" -o $@ -e "$(BOOSTROOT)" \
	-e "$(XERCESCROOT)" $<

.o: %.cpp
	$(CXX) $(CXXFLAGS) -c $<

SERVERDEPENDS = $(SERVERSRCS:.cpp=.cpp.d)
.INTERMEDIATE: $(SERVERDEPENDS)

depend: $(SERVERDEPENDS)
	cat $^ > .depend

boostheaders.tar.gz: $(SERVERSRCS)
	$(MAKEDEPEND) -l -c "$(CXX) $(CXXFLAGS)" -o boostheaderlist \
	-e "$(BOOSTROOT)" $^
	sed 's#$(BOOSTROOT)/##' boostheaderlist > boostheaderlist.unprefixed
	tar zcCfT "$(BOOSTROOT)" $@ boostheaderlist.unprefixed
	rm boostheaderlist boostheaderlist.unprefixed

$(SERVERNAME): $(SERVEROBJS)
	$(LD) -o $(SERVERNAME) $(SERVEROBJS) $(LDFLAGS)
