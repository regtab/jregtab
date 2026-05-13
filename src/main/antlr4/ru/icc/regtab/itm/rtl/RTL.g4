// Regular Table Language (RTL) Grammar
grammar RTL ;

options { caseInsensitive = true ; }

// Quantifier
quantifier : zeroOrOne | zeroOrMore | oneOrMore | exactly ;

zeroOrOne  : QUESTION ;
zeroOrMore : MULT ;
oneOrMore  : PLUS ;
exactly    : LCURLY INT RCURLY ;

// Table pattern
tablePattern : subtablePattern+ ;

// Subtable pattern: implicit or explicit
subtablePattern : implSubtablePattern | explSubtablePattern ;

// Implicit subtable pattern
implSubtablePattern : rowPattern+ ;

// Explicit subtable pattern
explSubtablePattern : LCURLY subtablePatternBody RCURLY quantifier? ;
subtablePatternBody : (cellMatchCond QUESTION)? (actSpecs)? rowPattern+ ;

// Row pattern
rowPattern : LSQUARE rowPatternBody RSQUARE quantifier? ;
rowPatternBody : (cellMatchCond QUESTION)? (actSpecs)? subrowPattern+ ;

// Subrow pattern: implicit or explicit
subrowPattern : implSubrowPattern | explSubrowPattern ;

// Implicit subrow pattern
implSubrowPattern : cellPattern+ ;

// Explicit subrow pattern
explSubrowPattern : LCURLY subrowPatternBody RCURLY quantifier? ;
subrowPatternBody : (cellMatchCond QUESTION)? (actSpecs)? cellPattern+ ;

// Cell pattern
cellPattern : LSQUARE cellPatternBody RSQUARE quantifier? ;
cellPatternBody : (cellMatchCond QUESTION)? actSpecs? contSpec;

// Content specification: atomic, delimited, compound, conditional
contSpec : atomContSpec | delimContSpec | compContSpec | condContSpec ;

// Atomic content specification
atomContSpec : itemDerivDir tags? (ASSIGN strExtr)? (COLON actSpecs)? ;

// Item derivation directive
itemDerivDir  : ATTRIBUTE | VALUE | AUXILIARY | SKIPPED ;
ATTRIBUTE : 'ATTR' ;
VALUE     : 'VAL'  ;
AUXILIARY : 'AUX'  ;
SKIPPED   : 'SKIP' ;

// User-defined tags
tags : TAG+ ;

// Item string extractor
strExtr : substr | replace | upperCase | lowerCase ;

// String processing
substr    : 'SUBSTR' LPAREN INT COMMA INT RPAREN ;
replace   : 'REPL'   LPAREN STRING COMMA STRING RPAREN ;
upperCase : 'UC' ;
lowerCase : 'LC' ;

// Interpretation action specifications
actSpecs : actSpec (COMMA actSpec)* ;

// Interpretation action specification
actSpec : provSpecs RIGHT_ARROW op ;
op : fillOp | prefixOp | suffixOp | AVP | REC | CONCAT ;
fillOp   : FILL   (LPAREN STRING RPAREN)? ;
prefixOp : PREFIX (LPAREN STRING RPAREN)? ;
suffixOp : SUFFIX (LPAREN STRING RPAREN)? ;
FILL   : 'FILL'   ;
PREFIX : 'PREFIX' ;
SUFFIX : 'SUFFIX' ;
AVP    : 'AVP'    ;
REC    : 'REC'    ;
CONCAT : 'CONCAT' ;

provSpecs : provSpec | (LPAREN provSpec (COMMA provSpec)* RPAREN) ;

// Delimited content specification
delimContSpec : LPAREN atomContSpec RPAREN LCURLY separator RCURLY;

separator  : STRING ;  // Separator

// Compound content specification
compContSpec : openDelim? atomContSpec (separator atomContSpec)* closeDelim? ;

openDelim  : STRING ;  // Opening delimiter
closeDelim : STRING ;  // Closing delimiter

// Conditional content specification
condContSpec : LPAREN cellMatchCond QUESTION (xContSpec VBAR xContSpec) RPAREN;
xContSpec    : atomContSpec | delimContSpec | compContSpec ;

// Cell match condition
cellMatchCond : cellMatchConstr ;
cellMatchConstr : regex | blank ;

// Item provider specification
provSpec : tblProvSpec | ctxProvSpec ;

// Cell derived item provider specification
tblProvSpec : provTemplate cardinality? constraints? ;

// Context derived item provider specification
ctxProvSpec : STRING ;

// Provider templates
provTemplate : LEFTWARD | RIGHTWARD | UPWARD | DOWNWARD | ROW_MAJOR | COLUMN_MAJOR | CELL ;

LEFTWARD     : 'LW' ;
RIGHTWARD    : 'RW' ;
UPWARD       : 'UW' ;
DOWNWARD     : 'DW' ;
ROW_MAJOR    : 'RM' ;
COLUMN_MAJOR : 'CM' ;
CELL         : 'CL' ;

// Cardinality k
cardinality : LCURLY INT RCURLY ;

// Constraints
constraints : LPAREN constr ((COMMA constr)*)? RPAREN ;
constr      : spatConstr | contConstr ;

// Spatial constraints
spatConstr : col | row | pos ;

// Bug fix: 'COL' keyword is always required; alternatives are grouped under it
row : 'ROW' (range | offset | INT) ;
col : 'COL' (range | offset | INT) ;
pos : 'POS' (range | offset | INT) ;

range : start DOUBLE_PERIOD end ;
start : offset | INT ;
end   : offset | INT ;

offset : (MINUS INT) | (PLUS INT) ;

// Content constraints
contConstr : regex | blank | tag ;

tag : 'TAG' TAG+ ;

regex : EXCLAMATION? STRING ;

blank : EXCLAMATION? 'BLANK' ;

PLUS  : '+' ;
MINUS : '-' ;
MULT  : '*' ;

LPAREN  : '(' ;
RPAREN  : ')' ;
LCURLY  : '{' ;
RCURLY  : '}' ;
LSQUARE : '[' ;
RSQUARE : ']' ;

COLON       : ':' ;
COMMA       : ',' ;
QUESTION    : '?' ;
VBAR        : '|' ;
EXCLAMATION : '!' ;

DOUBLE_PERIOD : '..' ;
ASSIGN        : '=' ;

RIGHT_ARROW : '->' ;

TAG : '#' [a-z_] [a-z_0-9]* ;

INT : [0-9]+ ;

STRING
    : '"'      (ESC | '""'   | ~["])* '"'
    | '\''     (ESC | '\'\'' | ~['])* '\''
    | '“' (ESC | .)*? ('”' | '″')   // smart quotes
    ;

fragment ESC
    : '`\''    // backtick single-quote
    | '`"'     // backtick double-quote
    ;

WS : [ \r\t\n]+ -> channel(HIDDEN) ;

ZWNBSP : [﻿]+ -> channel(HIDDEN) ; // Remove UTF8 BOM character

LineComment
    : '//' ~[\r\n]* -> channel(HIDDEN) ;
