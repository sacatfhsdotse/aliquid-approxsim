#       $Id: Makefile,v 1.5 2005/03/30 23:47:20 dah Exp $

JAVAFILES = $(wildcard *.java)
JAVADIRS =
JAVADOCEDFILES = $(JAVAFILES)
JOGLPATH=/afs/pdc.kth.se/projects/stratmas/common/jars
JOGLJARS=$(JOGLPATH)/jogl.jar:$(JOGLPATH)/jogl-natives-linux.jar:$(JOGLPATH)/jogl-natives-macosx.jar
JAVAC = javac
JAVA = java
JAVADOC = javadoc
DOCDIR = $(TOPDIR)/doc
CLASSPATH= .:$(JOGLJARS)
JAVACFLAGS=-classpath $(CLASSPATH)
JAVAFLAGS=-classpath $(CLASSPATH)

TOPDIR ?= ..
BUILDDIR ?=  $(TOPDIR)/classes
TARGETDIR =$(BUILDDIR)/StratmasClient/icons

%.class: %.java
	$(JAVAC) $(JAVACFLAGS) $(JAVAFILES)

all:
	mkdir -p $(BUILDDIR)/StratmasClient/map/images
	cp images/*.* $(BUILDDIR)/StratmasClient/map/images

clean:

doc: $(JAVADOCEDFILES)
	$(JAVADOC) -d $(DOCDIR) $^