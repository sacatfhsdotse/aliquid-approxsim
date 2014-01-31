#       $Id: Makefile,v 1.2 2005/03/30 23:47:20 dah Exp $

ICONS = $(wildcard *.png)
TOPDIR ?= ..
BUILDDIR ?=  $(TOPDIR)/classes
TARGETDIR = $(BUILDDIR)/StratmasClient/icons

all:
	mkdir -p "$(TARGETDIR)"
	cp $(ICONS) "$(TARGETDIR)"

clean:
	rm -rf "$(TARGETDIR)"
