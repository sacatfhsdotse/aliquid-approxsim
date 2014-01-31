#       $Id: Makefile,v 1.65 2006/09/04 10:34:53 alexius Exp $

JAVAFILES = $(wildcard *.java)
SUBPACKAGES = map TaclanV2 communication
JAVADOCEDFILES = $(JAVAFILES)
export TOPDIR := $(CURDIR)
export BUILDDIR ?= $(TOPDIR)/classes
XERCESPATH = $(TOPDIR)/externaljars
XERCESJARS = $(XERCESPATH)/xercesImpl.jar:$(XERCESPATH)/xml-apis.jar:$(XERCESPATH)/resolver.jar
JOGLPATH = $(TOPDIR)/externaljars
JOGLJARS = $(JOGLPATH)/jogl.jar
JDBFPATH = $(TOPDIR)/externaljars
JDBFJARS = $(JDBFPATH)/javadbf.jar
APP6APATH = $(TOPDIR)/externaljars
APP6AJARS = $(JDBFPATH)/App6A.jar
PORTFLAG = -DPORT=28444
#DISPFLAG = -DDISPATCHER=localhost
#JCONSOLE = -Dcom.sun.management.jmxremote
#JPROFILE = -Xrunhprof:file=dump.hprof,format=b
#JDBDEBUG = -Xdebug -Xrunjdwp:transport=dt_socket,address=8000,server=y,suspend=n
STRATMASDEBUG = -DStratmasClientDebug
#STRATMASDEBUG =
LIBDIR=$(BUILDDIR)/lib/Linux-i386:$(BUILDDIR)/lib/Mac OS X-ppc
JAVAC = javac
JAVA = java
JAVADOC = javadoc
DOCDIR = $(BUILDDIR)/doc
CLASSPATH = $(BUILDDIR):$(XERCESJARS):$(JOGLJARS):$(JDBFJARS):$(APP6AJARS)
JAVACFLAGS = -deprecation -source 1.4 -classpath $(CLASSPATH) \
	-d $(BUILDDIR) -sourcepath $(TOPDIR)/..
JAVAFLAGS = -Xmx100m $(JDBDEBUG) $(JCONSOLE) $(STRATMASDEBUG) \
	-classpath $(CLASSPATH) $(PORTFLAG) $(DISPFLAG)
JAVADOCFLAGS = -classpath $(CLASSPATH) -d $(DOCDIR) \
	-sourcepath $(TOPDIR)/.. -link http://java.sun.com/j2se/1.4/docs/api \
	-exclude StratmasClient.buildutils -subpackages

$(BUILDDIR)/StratmasClient/Client.class: $(JAVAFILES)
	mkdir -p $(BUILDDIR)
	make -C TaclanV2 generated
	make -C map
	make -C schemas
	make -C icons
	make -C treeview
	make -C communication
	make -C symbolloader
	make -C evolver
	make -C dispatcher
	$(JAVAC) $(JAVACFLAGS) $^
#$(patsubst %.java,StratmasClient/%.java,$^))

$(BUILDDIR)/StratmasClient/CommTest.class: $(JAVAFILES)
	mkdir -p $(BUILDDIR)
	make -C TaclanV2 generated
	make -C map
	make -C schemas
	make -C icons
	make -C treeview
	make -C communication
	make -C symbolloader
	(cd $(TOPDIR)/..; $(JAVAC) $(JAVACFLAGS) \
	 $(patsubst %.java,StratmasClient/%.java,$^))

$(BUILDDIR)/StratmasClient/XMLImporter.class: $(JAVAFILES)
	mkdir -p $(BUILDDIR)
	make -C TaclanV2 generated
	make -C map
	make -C schemas
	make -C icons
	make -C treeview
	make -C communication
	make -C symbolloader
	(cd $(TOPDIR)/..; $(JAVAC) $(JAVACFLAGS) \
	 $(patsubst %.java,StratmasClient/%.java,$^))

Client: $(BUILDDIR)/StratmasClient/Client.class
	make -C externaljars extractlibs
#	make -C externaljars extractsymbols
	make -C samples
	(cd $(BUILDDIR)/samples && env LD_LIBRARY_PATH="$(LIBDIR)" \
	 DYLD_LIBRARY_PATH="$(LIBDIR)" $(JAVA) $(JAVAFLAGS) \
	 StratmasClient/Client -noJoglResolve)

%.scn: $(BUILDDIR)/StratmasClient/Client.class
	make -C externaljars extractlibs
	make -C samples
	(cd $(BUILDDIR)/samples && env LD_LIBRARY_PATH="$(LIBDIR)" \
	 DYLD_LIBRARY_PATH="$(LIBDIR)" $(JAVA) $(JAVAFLAGS) \
	 StratmasClient/Client -noJoglResolve $$(find . -name "$*.scn" -print))


BatchClient: $(BUILDDIR)/StratmasClient/Client.class
	make -C externaljars extractlibs
	make -C externaljars extractsymbols
	make -C samples
	(cd $(BUILDDIR)/samples && env LD_LIBRARY_PATH="$(LIBDIR)" \
	 DYLD_LIBRARY_PATH="$(LIBDIR)" $(JAVA) $(JAVAFLAGS) \
	 StratmasClient/Client -noJoglResolve -batch=?,365d,fido.out \
	 teds.scn)

BatchClientLH: $(BUILDDIR)/StratmasClient/Client.class
	make -C externaljars extractlibs
	make -C externaljars extractsymbols
	make -C samples
	(cd $(BUILDDIR)/samples && env LD_LIBRARY_PATH="$(LIBDIR)" \
	 DYLD_LIBRARY_PATH="$(LIBDIR)" $(JAVA) $(JAVAFLAGS) \
	 StratmasClient/Client -noJoglResolve -batch=localhost,365d,fido.out \
	 teds.scn)

DefaultClient: $(BUILDDIR)/StratmasClient/Client.class
	make -C externaljars extractlibs
	make -C externaljars extractsymbols
	make -C samples
	(cd $(BUILDDIR)/samples && env LD_LIBRARY_PATH="$(LIBDIR)" \
	 DYLD_LIBRARY_PATH="$(LIBDIR)" $(JAVA) $(JAVAFLAGS) \
	 StratmasClient/Client -noJoglResolve -batch=localhost,0s,fido.out \
	 teds.scn)

CommTest: $(BUILDDIR)/StratmasClient/CommTest.class
	make -C samples
	(cd $(BUILDDIR)/samples ; env LD_LIBRARY_PATH="$(LIBDIR)" \
	 DYLD_LIBRARY_PATH="$(LIBDIR)" $(JAVA) $(JAVAFLAGS) \
	 StratmasClient/CommTest)

XMLImporter: $(BUILDDIR)/StratmasClient/XMLImporter.class
	make -C samples
	(cd $(BUILDDIR)/samples ; env LD_LIBRARY_PATH="$(LIBDIR)" \
	 DYLD_LIBRARY_PATH="$(LIBDIR)" $(JAVA) $(JAVAFLAGS) \
	 StratmasClient/XMLImporter $(CURDIR)/instance2.xml)

clean:
	make -C TaclanV2 clean
	make -C map clean
	make -C schemas clean
	make -C icons clean
	make -C treeview clean
	make -C symbolloader clean
	rm -rf core *~ $(BUILDDIR)
doc:
	make -C TaclanV2 generated
	$(JAVADOC) $(JAVADOCFLAGS) StratmasClient

test: Client

StratmasClient.jar: $(BUILDDIR)/StratmasClient/Client.class Manifest
	(cd $(BUILDDIR); jar cmf ../Manifest $@ StratmasClient)

jar: distjar

distjar: $(BUILDDIR)/StratmasClient/Client.class
	make -C externaljars extractlibs
	./buildutils/builddistjar.sh "$(BUILDDIR)" "$(CLASSPATH)"
