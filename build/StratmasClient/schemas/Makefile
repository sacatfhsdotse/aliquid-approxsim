#       $Id: Makefile,v 1.3 2005/03/30 23:47:20 dah Exp $

XSDFILES = $(wildcard *.xsd)
TOPDIR ?= ..
BUILDDIR ?=  $(TOPDIR)/classes
TARGETDIR = $(BUILDDIR)/StratmasClient/schemas

all:
	mkdir -p "$(TARGETDIR)"
	cp $(XSDFILES) "$(TARGETDIR)"

clean:
	rm -rf "$(TARGETDIR)"
