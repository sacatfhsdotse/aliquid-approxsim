#       $Id: Makefile,v 1.1 2005/10/07 12:57:27 dah Exp $

XSDFILES = $(wildcard *.xsd)
TOPDIR ?= ..
BUILDDIR ?=  $(TOPDIR)/classes
TARGETDIR = $(BUILDDIR)/StratmasDispatcher/schemas

all:
	mkdir -p "$(TARGETDIR)"
	cp $(XSDFILES) "$(TARGETDIR)"

clean:
	rm -rf "$(TARGETDIR)"
