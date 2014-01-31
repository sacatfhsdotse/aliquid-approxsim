#       $Id: Makefile,v 1.2 2006/09/01 07:44:07 dah Exp $

TOPDIR ?= ..
BUILDDIR ?=  $(TOPDIR)/classes
TARGETDIR =$(BUILDDIR)/StratmasClient/treeview

all:
	mkdir -p $(TARGETDIR)/images
	find images -type f -not -path "*/CVS/*" -exec cp {} $(TARGETDIR)/images ";"

clean:
