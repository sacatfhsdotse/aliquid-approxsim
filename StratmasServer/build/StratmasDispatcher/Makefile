#       $Id: Makefile,v 1.3 2005/10/07 12:57:21 dah Exp $

JAVAFILES = $(wildcard *.java)
JAVADOCEDFILES = $(JAVAFILES)
export TOPDIR := $(CURDIR)
export BUILDDIR ?= $(TOPDIR)/classes
XERCESPATH = $(TOPDIR)/externaljars
XERCESJARS = $(XERCESPATH)/xercesImpl.jar:$(XERCESPATH)/xml-apis.jar:$(XERCESPATH)/resolver.jar
JAVAC = javac
JAVA = java
JAVADOC = javadoc
DOCDIR = $(BUILDDIR)/doc
CLASSPATH = $(BUILDDIR):$(XERCESJARS)
JAVACFLAGS = -deprecation -source 1.4 -classpath $(CLASSPATH) -d $(BUILDDIR) -sourcepath $(TOPDIR)/..
JAVAFLAGS = -Xmx300m -Xdebug  -Xrunjdwp:transport=dt_socket,address=8001,server=y,suspend=n -classpath $(CLASSPATH)
COMMTESTFLAGS = -Xmx300m -classpath $(CLASSPATH) 
JAVADOCFLAGS = -classpath $(CLASSPATH) -d $(DOCDIR) -sourcepath $(TOPDIR)/.. -link http://java.sun.com/j2se/1.4/docs/api -exclude StratmasDispatcher.buildutils -subpackages

$(BUILDDIR)/StratmasDispatcher/StratmasDispatcher.class: $(JAVAFILES)
	mkdir -p $(BUILDDIR)
	make -C schemas
	$(JAVAC) $(JAVACFLAGS) $^

StratmasDispatcher: $(BUILDDIR)/StratmasDispatcher/StratmasDispatcher.class
	(cd $(BUILDDIR) && $(JAVA) $(JAVAFLAGS) StratmasDispatcher/StratmasDispatcher)

clean:
	rm -rf core *~ $(BUILDDIR)
doc:
	$(JAVADOC) $(JAVADOCFLAGS) StratmasDispatcher

test: StratmasDispatcher

jar: StratmasDispatcher.jar

StratmasDispatcher.jar:	 $(BUILDDIR)/StratmasDispatcher/StratmasDispatcher.class
	./buildutils/buildjar.sh "$(BUILDDIR)" "$(CLASSPATH)" $@
