Program -> FunctionDefinition*

FunctionDefinition -> Type Identifier lparen ParameterList rparen Block

ParameterList -> (Parameter (listsep Parameter)*)?

Parameter -> Type Identifier

Block -> bb ExpressionList be

ExpressionList -> Statement*

Statement -> Declaration | ReturnStatement | WhileLoop | DoLoop | IfStatement | SimpleStatement

Declaration -> Type Identifier (listsep Identifier)* eol

Type -> number_t | string_t | boolean_t 

Identifier -> id

ReturnStatement -> return Logical eol

WhileLoop -> while Condition Block

DoLoop -> do Block while Condition eol

IfStatement -> if Condition Block (elsif Condition Block)* (else Block)?

Condition -> lparen Assignment rparen

SimpleStatement -> Assignment eol

Assignment -> Logical (gets Logical)?

Logical -> Comparison ((and|or) Comparison)*  [>1]
Comparison -> Sum ((lt|gt|eq|le|ge) Sum)*  [>1]
Sum -> Term ((plus|minus) Term)*  [>1]
Term -> Exp ((multiply|divide|mod) Exp)* [>1]  
Exp -> Unary (exp Unary)*  [>1] 
Unary -> PrefixUnary | Element [>1] 
PrefixUnary -> PrefixUnaryOp Element
PrefixUnaryOp -> inc|dec|minus
Element -> Constant | lparen Logical rparen | ElementPlus
ElementPlus -> id (PostfixUnaryOp | ArgumentList)?
PostfixUnaryOp -> inc|dec

Constant -> StringConstant | BooleanConstant | NumberConstant

StringConstant -> string
BooleanConstant -> true | false
NumberConstant -> number

ArgumentList -> lparen (Assignment (listsep Assignment)*)? rparen

