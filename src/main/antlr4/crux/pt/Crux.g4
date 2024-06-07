grammar Crux;
program
 : declarationList EOF
 ;

literal
 : INTEGER
 | TRUE
 | FALSE
 ;

designator
 : IDENTIFIER (OPEN_BRACKET expression0 CLOSE_BRACKET)?
 ;

type
 : IDENTIFIER
 ;

op0
 : GREATER_EQUAL|LESSER_EQUAL|NOT_EQUAL|EQUAL|GREATER_THAN|LESS_THAN
 ;

op1
 : ADD|SUB|OR
 ;

op2
 : MUL|DIV|AND
 ;

expression0
 : expression1 (op0 expression1)?
 ;

expression1
 : expression2
 | expression1 op1 expression2
 ;

expression2
 : expression3
 | expression2 op2 expression3
 ;

expression3
 : NOT expression3
 | OPEN_PAREN expression0 CLOSE_PAREN
 | designator
 | callExpression
 | literal
 ;

callExpression
 : IDENTIFIER OPEN_PAREN expressionList CLOSE_PAREN
 ;

expressionList
 : (expression0 (COMMA expression0)* )?
 ;

parameter
 : type IDENTIFIER
 ;

parameterList
 : (parameter (COMMA parameter)*)?
 ;

variableDeclaration
 : type IDENTIFIER SEMICOLON
 ;

arrayDeclaration
 : type IDENTIFIER OPEN_BRACKET INTEGER CLOSE_BRACKET SEMICOLON
 ;

functionDefinition
 : type IDENTIFIER OPEN_PAREN parameterList CLOSE_PAREN statementBlock
 ;

declaration
 : variableDeclaration
 | arrayDeclaration
 | functionDefinition
 ;

declarationList
 : declaration*
 ;

assignmentStatement
 : designator ASSIGN expression0 SEMICOLON
 ;

callStatement
 : callExpression SEMICOLON
 ;

ifStatement
 : IF expression0 statementBlock (ELSE statementBlock)?
 ;

loopStatement
 : LOOP statementBlock
 ;

breakStatement
 : BREAK SEMICOLON
 ;

continueStatement
 : CONTINUE SEMICOLON
 ;

returnStatement
 : RETURN expression0 SEMICOLON
 ;

statement
 : variableDeclaration
 | callStatement
 | assignmentStatement
 | ifStatement
 | loopStatement
 | breakStatement
 | continueStatement
 | returnStatement
 ;

statementList
 : statement*
 ;

statementBlock
 : OPEN_BRACE statementList CLOSE_BRACE
 ;

INTEGER
 : '0'
 | [1-9] [0-9]*
 ;

// void, bool, and int are reserved types but are recognized as IDENTIFIER tokens

// do not need to worry about reserved types here

// the following words are reserved keywords
AND
 : '&&'
 ;

OR
 : '||'
 ;

NOT
 : '!'
 ;

IF
 : 'if'
 ;

ELSE
 : 'else'
 ;

LOOP
 : 'loop'
 ;

CONTINUE
 : 'continue'
 ;

BREAK
 : 'break'
 ;

RETURN
 : 'return'
 ;


TRUE
 : 'true'
 ;

FALSE
 : 'false'
 ;

// the following character sequences have special meaning

OPEN_PAREN
 : '('
 ;

CLOSE_PAREN
 : ')'
 ;

OPEN_BRACE
 : '{'
 ;

CLOSE_BRACE
 : '}'
 ;

OPEN_BRACKET
 : '['
 ;

CLOSE_BRACKET
 : ']'
 ;

ADD
 : '+'
 ;

SUB
 : '-'
 ;

MUL
 : '*'
 ;

DIV
 : '/'
 ;

GREATER_EQUAL
 : '>='
 ;

LESSER_EQUAL
 : '<='
 ;

NOT_EQUAL
 : '!='
 ;

EQUAL
 : '=='
 ;

GREATER_THAN
 : '>'
 ;

LESS_THAN
 : '<'
 ;

ASSIGN
 : '='
 ;

COMMA
 : ','
 ;

SEMICOLON
 : ';'
 ;

IDENTIFIER
 : [a-zA-Z_] [a-zA-Z0-9_]*
 ;

// whitespace should be ignored, as it does not constitute a lexeme
WHITESPACES
 : [ \t\r\n]+ -> skip
 ;

// comments should begin with a double forward slash and continue until the end of line
// comments should be ignored by the scanner because they do not constitute a lexeme
COMMENT
 : '//' ~[\r\n]* -> skip
 ;

