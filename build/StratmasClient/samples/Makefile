#       $Id: Makefile,v 1.6 2006/03/30 12:11:04 alexius Exp $

BUILDDIR ?= $(TOPDIR)/classes
TARGETDIR = $(BUILDDIR)/samples

all:
	mkdir -p "$(TARGETDIR)"
	tar c . | tar x -C "$(TARGETDIR)"
	find "$(TARGETDIR)"/ -name CVS | xargs rm -rf 
	find "$(TARGETDIR)"/ -name Makefile | xargs rm
clean:
