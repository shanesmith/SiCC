
import java.util.*;
import java.io.*;

public class Parser {

	ASTCompilerNode compiler = new ASTCompilerNode();
	
	Hashtable<String, Hashtable<String, ASTTokenNode>> tokens = new Hashtable<String, Hashtable<String, ASTTokenNode>> ();

	Hashtable<String, ASTGrammarNode>  grammar = new Hashtable<String, ASTGrammarNode>();
	
	StreamTokenizer in;
	
	public ASTCompilerNode parse(StreamTokenizer input) {
		
		in = input;
		
		//parse tokens
		//compiler.setTokens(parseTokens());
		
		//parse grammar
		//compiler.setGrammar(parseGrammar());
		
		
		return compiler;
	}
	
	@SuppressWarnings("unused")
	private Hashtable<String, Hashtable<String, ASTTokenNode>> parseTokens() {
		/*
		 * iterate through token definition
		 * and create multi-dimension hashtable like
		 * 
		 * {
		 * 	'public' => {
		 * 	  'tokenName1' => ASTTokenNode,
		 * 	  'tokenName2' => ASTTokenNode
		 *  },
		 *  'private' => {
		 *    'tokenName3' => ASTTokenNode
		 *  }
		 * }
		 * 
		 * where 'private' for tokens such as :internal:
		 * 
		 */
		return new Hashtable<String, Hashtable<String, ASTTokenNode>>();
	}
	
	@SuppressWarnings("unused")
	private Hashtable<String, ASTGrammarNode> parseGrammar() {
		/*
		 * iterate through grammar definition
		 * and create hashtable
		 * 
		 * { 'name' => ASTGrammarNode, ... }
		 * 
		 */
		return new Hashtable<String, ASTGrammarNode>();
	}
	
}

/*

<tokens>
	# end of line character
	eol: ;
	
	# block delimiters
	bb: {
	be: }
	
	# subscript operators
	lsub: \[
	rsub: \]
	
	# parentheses
	lparen: \(
	rparen: \)
	
	# list separator
	listsep: ,
</tokens>	

<grammar>
	program -> functionDefinition* 
	
	FunctionDefinition -> Type Identifier lparen ParameterList Block
	
	ParameterList -> (Parameter (listsep Parameter)*)?
	
	Parameteter -> Type Identifier
	
	Block -> bb ExpressionList be
	
	ExpressionList -> Statement*
	
	Statement -> Declaration | ReturnStatement | WhileLoop \
	            | DoLoop| IfStatement| SimpleStatement
	
	Declaration -> Type Identifier (listsep Identifier)* eol
	
	Type -> number_t | string_t | boolean_t 
	
	Identifier -> id
	
	ReturnStatement -> return Logical eol
	
	WhileLoop -> while Condition Block
	
	DoLoop -> do Block while Condition eol
	
	IfStatement -> if Condition Block \
			(elsif Condition Block)* \
	                (else Block)?
</grammar>
*/