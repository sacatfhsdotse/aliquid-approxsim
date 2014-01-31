#       $Id: Makefile,v 1.4 2006/09/01 14:28:48 dah Exp $

TOPDIR ?= ..
JOGLPATH ?= $(CURDIR)
JOGLLINUXJAR ?= $(JOGLPATH)/jogl-natives-linux-i586.jar 
JOGLSOLSPARCJAR ?= $(JOGLPATH)/jogl-natives-solaris-sparc.jar 
JOGLWIN32JAR ?= $(JOGLPATH)/jogl-natives-windows-i586.jar 
JOGLMACOSXJAR ?= $(JOGLPATH)/jogl-natives-macosx-universal.jar
JOGLNATIVEJARS = $(JOGLLINUXJAR) $(JOGLSOLSPARCJAR) $(JOGLWIN32JAR) $(JOGLMACOSXJAR)
APP6APATH ?= $(CURDIR)
APP6AJAR ?= $(APP6APATH)/App6A.jar

BUILDDIR ?= $(TOPDIR)/classes
TARGETDIR = $(BUILDDIR)/lib

extractlibs: $(JOGLNATIVEJARS)
	mkdir -p $(BUILDDIR)/lib 
	mkdir -p $(BUILDDIR)/lib/Linux-i386
	(cd $(BUILDDIR)/lib/Linux-i386 && \
	 jar xf $(JOGLLINUXJAR) && rm -rf META-INF);

	mkdir -p $(BUILDDIR)/lib/SunOS-sparc
	(cd $(BUILDDIR)/lib/SunOS-sparc && \
	 jar xf $(JOGLSOLSPARCJAR) && rm -rf META-INF)
	mkdir -p $(BUILDDIR)/lib/Windows-x86
	(cd $(BUILDDIR)/lib/Windows-x86 && \
	 jar xf $(JOGLWIN32JAR) && rm -rf META-INF)
	mkdir -p "$(BUILDDIR)/lib/Mac OS X-ppc"
	(cd "$(BUILDDIR)/lib/Mac OS X-ppc" && \
	 jar xf $(JOGLMACOSXJAR) && rm -rf META-INF)
	mkdir -p "$(BUILDDIR)/lib/Mac OS X-i386"
	(cd "$(BUILDDIR)/lib/Mac OS X-i386" && \
	  jar xf $(JOGLMACOSXJAR) && rm -rf META-INF)

extractsymbols: $(APP6AJAR)
	(cd $(BUILDDIR); jar xf $(APP6AJAR));

clean:


