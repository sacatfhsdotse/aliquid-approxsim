// $Id: Lexer.lex,v 1.6 2005/06/29 18:08:16 dah Exp $

package ApproxsimClient.TaclanV2;
import ApproxsimClient.TaclanV2.java_cup.runtime.Symbol;

%%

%eofval{
  return new Symbol(Sym.EOF, yyline, yycolumn, new Symbol(Sym.EOF, yyline, yycolumn, "end of file"));
%eofval}

%line
%column
%cup
%unicode
%type Symbol
%class Lexer

NEWLINE = \n
NOTNEWLINE = [^\n]
/* Generous whitespacedef */
WHITE_SPACE_CHAR=[\n\r\t\b ]
DIGIT=[0-9]
/* Protected string text (within double quotes) */
STRING_TEXT=(((\\)|(\\\")|(\\n))|[^\n\"])*
/* Protected id text (within quotes) */
ID_PTEXT=(((\\)|(\\\')|(\\n))|[^\n\'])*
/* UnProtected id text (without quotes) */
ID_UPTEXT=[A-Za-z_][A-Za-z_0-9]*

%%
/* Comments */
"//"{NOTNEWLINE}*{NEWLINE}? {/* line comment skip */}
"/*" [^*] ~"*/" | "/*" "*"+ "/"  {/* block comment skip */}

/* Operators */
"{" { return new Symbol(Sym.OPENBLOCK, yyline, yycolumn, 
                        new Symbol(Sym.OPENBLOCK, yyline, yycolumn, "{"));}
"}" { return new Symbol(Sym.CLOSEBLOCK, yyline, yycolumn, 
                        new Symbol(Sym.CLOSEBLOCK, yyline, yycolumn, "}"));}
":" { return new Symbol(Sym.SCOPE, yyline, yycolumn, 
                        new Symbol(Sym.SCOPE, yyline, yycolumn, ":"));}
"=" { return new Symbol(Sym.EQUIV, yyline, yycolumn, 
                        new Symbol(Sym.EQUIV, yyline, yycolumn, "="));}


/* Keywords */
"as" { return new Symbol(Sym.AS, yyline, yycolumn, 
                         new Symbol(Sym.AS, yyline, yycolumn, "as")); } 
"from" { return new Symbol(Sym.FROM, yyline, yycolumn, 
                            new Symbol(Sym.FROM, yyline, yycolumn, "from")); }
"import" { return new Symbol(Sym.IMPORT, yyline, yycolumn, 
                              new Symbol(Sym.IMPORT, yyline, yycolumn, "import")); }
"true" { return new Symbol(Sym.TRUE, yyline, yycolumn, 
                              new Symbol(Sym.TRUE, yyline, yycolumn, "true")); }
"false" { return new Symbol(Sym.FALSE, yyline, yycolumn, 
                              new Symbol(Sym.FALSE, yyline, yycolumn, "false")); }

/* Unprotected identifier e. g. car */
{ID_UPTEXT} {String str = new String(yytext()); 
 return new Symbol(Sym.ID, yyline, yycolumn, 
                   new Symbol(Sym.ID, 
                              yyline, yycolumn, str));}

/* Protected identifier e. g. 'My car' */
"'"{ID_PTEXT}"'" {String str = new String(yytext()); 
 return new Symbol(Sym.ID, yyline, yycolumn, 
                   new Symbol(Sym.ID, yyline, 
                              yycolumn, str.substring(1, str.length() - 1)));}

/* String */
"\""{STRING_TEXT}"\"" {String str = new String(yytext()); 
 return new Symbol(Sym.STRING, yyline, yycolumn, 
                   new Symbol(Sym.STRING, yyline, 
                              yycolumn, str.substring(1, str.length() - 1)));}

/* Integer */ 
-?{DIGIT}+ {return new Symbol(Sym.INTEGER, yyline, 
                              yycolumn, new Symbol(Sym.INTEGER, 
                                                   yyline, yycolumn, 
                                                   new String(yytext())));}

/* Floating point. */
-?{DIGIT}+"."{DIGIT}+(([eE](-?)({DIGIT}+))?) {return new Symbol(Sym.FLOAT, yyline, 
                                         yycolumn, new Symbol(Sym.FLOAT, 
                                                              yyline, yycolumn, 
                                                              new String(yytext())));}

-?{DIGIT}+[eE](-?)({DIGIT}+) {return new Symbol(Sym.FLOAT, yyline, 
                                         yycolumn, new Symbol(Sym.FLOAT, 
                                                              yyline, yycolumn, 
                                                              new String(yytext())));}

/* Whitespaces. */
{WHITE_SPACE_CHAR}          { /* Skip whitespaces  */}

// Anything else is an error
. { return new Symbol(Sym.error, 
                      yyline, yycolumn, 
                      new Symbol(Sym.error, yyline, yycolumn,
                                 new String(yytext())));}
