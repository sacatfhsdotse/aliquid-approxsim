#       $Id: Makefile,v 1.2 2006/09/01 07:43:53 dah Exp $

TOPDIR ?= ..
BUILDDIR ?=  $(TOPDIR)/classes
TARGETDIR =$(BUILDDIR)/StratmasClient/evolver

all:
	mkdir -p $(TARGETDIR)/icons
	find icons -type f -not -path "*/CVS/*" -exec cp {} $(TARGETDIR)/icons ";"

clean:
