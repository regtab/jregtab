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
tablePattern : settings? subtablePattern+ ;

// Optional settings prefix <NORM,ANCH(n),SPLIT("s")>
settings     : LANGLE setting (COMMA setting)* RANGLE ;
setting      : normSetting | anchSetting | splitSetting ;
normSetting  : 'NORM' ;
anchSetting  : 'ANCH' LPAREN INT RPAREN ;
splitSetting : 'SPLIT' LPAREN STRING RPAREN ;

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
cellPattern : LSQUARE cellPatternBody? RSQUARE quantifier? ;
cellPatternBody : cellMatchCond QUESTION (actSpecs? contSpec)?
               | actSpecs? contSpec
               ;

// Content specification: atomic, delimited, compound, conditional
contSpec : atomContSpec | delimContSpec | compContSpec | condContSpec ;

// Atomic content specification
atomContSpec : itemDerivDir tags? (ASSIGN strExtr)? (COLON actSpecs)? ;

// Item derivation directive
itemDerivDir  : ATTRIBUTE | VALUE | AUXILIARY | SKIPPED ;
ATTRIBUTE : 'ATTR' ;
VALUE     : 'VAL'  ;
AUXILIARY : 'AUX'  ;
SKIPPED   : 'SKIP' | '_' ;

// User-defined tags
tags : TAG+ ;

// Item string extractor (supports chains: =REPL("x","").TRIM)
strExtr     : strExtrStep ('.' strExtrStep)* ;
strExtrStep : substr | replace | norm | upperCase | lowerCase | trim ;

// String processing
substr    : 'SUBSTR' LPAREN INT COMMA INT RPAREN ;
replace   : 'REPL'   LPAREN STRING COMMA STRING RPAREN ;
norm      : 'NORM' ;
upperCase : 'UC' ;
lowerCase : 'LC' ;
trim      : 'TRIM' ;

// Interpretation action specifications
actSpecs : actSpec (COMMA actSpec)* ;

// Interpretation action specification
actSpec : provSpecs RIGHT_ARROW op ;
op : fillOp | prefixOp | suffixOp | AVP | recOp | CONCAT ;
fillOp   : FILL   (LPAREN STRING RPAREN)? ;
prefixOp : PREFIX (LPAREN STRING RPAREN)? ;
suffixOp : SUFFIX (LPAREN STRING RPAREN)? ;
recOp    : REC    (LPAREN (INT | STRING) RPAREN)? ;
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
cellMatchConstr : regex | blank | contains ;

// Item provider specification
provSpec : tblProvSpec | ctxProvSpec ;

// Cell derived item provider specification
// Single bare form: spatConstr only (avoids ambiguity with ctxProvSpec STRING).
// Multiple or content constraints require parentheses.
tblProvSpec : traversalOrderMark? (spatConstr | LPAREN constraints RPAREN) cardinality? ;

// Traversal order mark (absence = ROW_MAJOR)
traversalOrderMark : reverseRowMajor | columnMajor | reverseColumnMajor ;
reverseRowMajor    : MINUS ;
columnMajor        : CARET ;
reverseColumnMajor : MINUS CARET ;

// Context derived item provider specification
ctxProvSpec : STRING ;

// Cardinality k: {n} = at most n; * = UNBOUNDED (0..*); absent = at most 1 (default)
cardinality : LCURLY INT RCURLY | MULT ;

// Constraints (| has lower precedence than &; parentheses for explicit grouping)
constraints : orGroup (VBAR orGroup)* ;
orGroup     : baseConstr (AMP baseConstr)* ;
baseConstr  : constr | LPAREN constraints RPAREN ;
constr      : spatConstr | contConstr ;

// Spatial constraints
spatConstr : LEFT_OF | RIGHT_OF | ABOVE | BELOW | ROW | COLUMN
           | SUBROW | SUBCOLUMN | SUBTABLE | TABLE | CELL
           | col | row | pos ;

LEFT_OF   : 'LT'  ;   // sameSubrow(a) && col < col(a)
RIGHT_OF  : 'RT'  ;   // sameSubrow(a) && col > col(a)
ABOVE     : 'AV'  ;   // sameSubcol(a) && row < row(a)
BELOW     : 'BW'  ;   // sameSubcol(a) && row > row(a)
ROW       : 'ROW' ;   // sameRow(a) && !sameCell(a)
COLUMN    : 'COL' ;   // sameCol(a) && !sameCell(a)
SUBROW    : 'SR'  ;   // sameSubrow(a) && !sameCell(a)
SUBCOLUMN : 'SC'  ;   // sameSubcol(a) && !sameCell(a)
SUBTABLE  : 'ST'  ;   // sameSubtable(a) && !sameCell(a)
TABLE     : 'TAB' ;   // !sameCell(a)
CELL      : 'CL'  ;   // sameCell(a)

// Positional constraints
row : 'R' (range | offset | INT) ;
col : 'C' (range | offset | INT) ;
pos : 'P' (range | offset | INT) ;

range : start DOUBLE_PERIOD end? ;
start : offset | INT ;
end   : offset | INT ;

offset : (MINUS INT) | (PLUS INT) ;

// Content constraints
contConstr : regex | blank | tag | sameStr | contains ;

// Contains constraint
contains : EXCLAMATION? 'CONTAINS' STRING ;

tag : 'TAG' TAG+ ;

sameStr : STR ;
STR : 'STR' ;

regex : EXCLAMATION? STRING ;

blank : EXCLAMATION? 'BLANK' ;

PLUS  : '+' ;
MINUS : '-' ;
CARET : '^' ;
MULT  : '*' ;
AMP   : '&' ;

LPAREN  : '(' ;
RPAREN  : ')' ;
LCURLY  : '{' ;
RCURLY  : '}' ;
LSQUARE : '[' ;
RSQUARE : ']' ;
LANGLE  : '<' ;
RANGLE  : '>' ;

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
