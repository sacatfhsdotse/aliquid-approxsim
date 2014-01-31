#       $Id: Makefile,v 1.2 2006/09/01 07:43:44 dah Exp $

TOPDIR ?= ..
BUILDDIR ?=  $(TOPDIR)/classes
TARGETDIR =$(BUILDDIR)/StratmasClient/dispatcher

all:
	mkdir -p $(TARGETDIR)/images
	find images -type f -not -path "*/CVS/*" -exec cp {} $(TARGETDIR)/images ";"

clean:
