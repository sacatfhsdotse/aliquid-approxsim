#       $Id: Makefile,v 1.7 2006/04/24 08:06:47 dah Exp $

JAVAFILES = java_cup/runtime/Scanner.java				\
java_cup/runtime/lr_parser.java java_cup/runtime/Symbol.java		\
java_cup/runtime/virtual_parse_stack.java				\
CollectedErrorsException.java ParsedImport.java Declaration.java	\
ParsedInteger.java ESRIImportHandler.java ParsedObject.java		\
IdConflictException.java ParsedPrimitive.java ImportException.java	\
ParsedReference.java ImportHandler.java ParsedString.java		\
ImportHandlerException.java SemanticException.java			\
NoImportHandlerException.java SourcePosition.java			\
ParsedDeclaration.java TaclanV2ImportHandler.java			\
ParsedDeclarationList.java Type.java ParsedFloat.java			\
TypeAttribute.java ParsedIdentifier.java TypeInformation.java		\
TypeErrorException.java TypeDefinition.java DocumentDefinition.java	\
ParsedList.java ESRIShapefile.java UnresolvedReferenceException.java	\
SyntaxException.java MissingDeclarationException.java ParsedBoolean.java

TOPDIR ?= ..
BUILDDIR ?= $(TOPDIR)/classes
GENERATEDJAVA = Sym.java Parser.java Lexer.java
JAVADOCEDFILES = $(JAVAFILES) java_cup/runtime/*.java
JFLEX = java -jar $(TOPDIR)/buildutils/JFlex.jar
JCUP  = java -jar $(TOPDIR)/buildutils/JCup.jar
JAVAC = javac
JAVA = java
JAVADOC = javadoc
DOCDIR = ./doc
CLASSPATH= $(XERCESJARS)
JAVACFLAGS=-classpath "../../:"$(CLASSPATH) -d ../classes
JAVAFLAGS=-classpath $(CURDIR)"/../classes:"$(CLASSPATH)

%.class: %.java
	$(JAVAC) $(JAVACFLAGS) $^

Parser.class: $(GENERATEDJAVA) $(JAVAFILES)

Parser.java Sym.java: Parser.cup
	$(JCUP) -package StratmasClient.TaclanV2 -parser Parser -symbols Sym -expect 0 < $^
	echo -e ',s/\([^.]\)java_cup.runtime/\\1StratmasClient.TaclanV2.java_cup.runtime/g\nw\nq' | ed Parser.java
	echo -e ',s/throws java.lang.Exception/throws SemanticException, SyntaxException/g\nw\nq' | ed Parser.java
	echo -e ',s/throw new Exception/throw new AssertionError/g\nw\nq' | ed Parser.java

ParserDebug: Parser.cup
	$(JCUP) -dump_states -dump_grammar -parser Parser -symbols Sym -expect 0 < $^

Lexer.java: Lexer.lex
	$(JFLEX) $^
	echo -e ',s/\([^.]\)java_cup.runtime/\\1StratmasClient.TaclanV2.java_cup.runtime/g\nw\nq' | ed Lexer.java

generated: $(GENERATEDJAVA)

clean:
	rm -f core *~ *.class $(GENERATEDJAVA) $(DOCDIR)/*.html $(DOCDIR)/*.css $(DOCDIR)/*.html $(DOCDIR)/package-list symboltable

$(DOCDIR)/index.html: $(JAVADOCEDFILES)
	$(JAVADOC) -d $(DOCDIR) $^

doc: $(DOCDIR)/index.html

TaclanV2.jar: Parser.class Manifest
	@jar cmf Manifest $@ `find . -name "*.class"`

jar: TaclanV2.jar

symboltable: Sym.java
	@awk '$$2 == "static" {print $$7 "\t" $$5}' $^ |sort -nk1 | tr -d ';' > $@
