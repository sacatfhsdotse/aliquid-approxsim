#
# A simple Makefile, mainly to get a grip on distribution archive building.
#

#       $Id: Makefile,v 1.3 2005/03/30 23:47:21 dah Exp $

JAVAFILES = $(wildcard *.java)
TXTFILES = $(wildcard *.txt)
JAVADIRS =
JAVADOCEDFILES = $(JAVAFILES)
JAVAC = javac
JAVA = java
JAVADOC = javadoc
TOPDIR ?= $(CURDIR)	
DOCDIR = $(TOPDIR)/doc
CLASSPATH ?= .
JAVACFLAGS=-classpath $(CLASSPATH)
JAVAFLAGS=-classpath $(CLASSPATH)

%.class: %.java
	$(JAVAC) $(JAVACFLAGS) $(JAVAFILES)

all: $(JAVAFILES) $(TXTFILES)
	mkdir -p $(BUILDDIR)/StratmasClient/symbolloader
	cp $(TXTFILES) $(BUILDDIR)/StratmasClient/symbolloader
#javac -d $(TOPDIR)/$(BUILDDIR) $(JAVAFILES)

doc: $(JAVADOCEDFILES)
	$(JAVADOC) -d $(DOCDIR) $^

clean: