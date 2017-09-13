/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

grammar Expression;

options {
    language = Java;
}

@members {
}

parse
    : expression
    ;

expression
    : cast expression                           #castExpression
    | LParen expression RParen                  #parenthesisExpression
    | Minus expression                          #unaryMinusExpression
    | Neg expression                            #notExpression
    | expression Pow expression                 #powerExpression
    | expression Div expression                 #divideExpression
    | expression Mult expression                #multiplyExpression
    | expression Mod expression                 #modulusExpression
    | expression Plus expression                #addExpression
    | expression Minus expression               #subtractExpression
    | expression GTEquals expression            #greaterThanOrEqualsExpression
    | expression LTEquals expression            #lessThanOrEqualsExpression
    | expression GT expression                  #greaterThanExpression
    | expression LT expression                  #lessThanExpression
    | expression Like expression                #likeExpression
    | expression (Equals|DoubleEquals) expression   #equalsExpression
    | expression NotEquals expression            #notEqualsExpression
    | expression And expression            #andExpression
    | expression Or expression            #orExpression
    | expression In expression            #inExpression
    | String                                    #stringExpression
    | Float                                     #floatExpression
    | Double                                    #doubleExpression
    | Byte                                      #byteExpression
    | Short                                     #shortExpression
    | Long                                      #longExpression
    | Int                                       #intExpression
    | Bool                                      #boolExpression
    | Null                                      #nullExpression
    | colFunctionCall                           #colFunctionCallExpression
    | functionCall                              #functionCallExpression
    | list                                      #listExpression
    | Identifier                                    #columnExpression
    ;

cast : LParen CastType RParen
    ;

colFunctionCall : ColFunction LParen expression RParen
    ;

functionCall : Identifier LParen expressionList? RParen
    ;

expressionList : expression (ListSep expression)*
         ;

list : LBracket expressionList RBracket
     ;

fragment Letter : ('a'..'z'|'A'..'Z');
fragment Digit : '0'..'9';
LParen  : '(';
RParen  : ')';
CastType : 'Byte' | 'byte' | 'Short' | 'short' | 'Int' | 'int' | 'Long' | 'long' | 'Float' | 'float' | 'Double' | 'double';
LBracket: '[';
RBracket: ']';
ListSep : ',';
In      : ('in'|'IN');
Or      : ('||'|'OR');
And     : ('&&'|'AND');
Like    : ('like'|'LIKE');
DoubleEquals: '==';
Equals      : '=';
NotEquals   : '!=';
GTEquals: '>=';
LTEquals: '<=';
GT      : '>';
LT      : '<';
Plus    : '+';
Minus   : '-';
Mult    : '*';
Div     : '/';
Mod     : '%';
Pow     : '^';
Neg     : '!';
Null    : ('null'|'NULL');
String  : '"' (~["]|'\\\\'|'\\"')* '"';
Bool    : ('true'|'TRUE'|'false'|'FALSE');
ColFunction: 'col';
Identifier: Letter (Letter|Digit|'_')*;
Float   : Digit* '.' Digit+ ('f'|'F');
Double  : Digit* '.' Digit+ ('d'|'D')?;
Byte    : Int ('b'|'B');
Short   : Int ('h'|'H');
Long    : Int ('l'|'L');
Int     : '0' | (('1'..'9') Digit*) ('i'|'I')?;
Space   : [ \t\r\n] -> skip;
