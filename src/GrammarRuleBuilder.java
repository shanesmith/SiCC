import java.util.Hashtable;
import java.util.Stack;
import java.util.Vector;

/**
 * Builds a rule with its accompanying sub-rules 
 */
public class GrammarRuleBuilder {

	/**
	 * Used for the naming of sub-rules
	 */
	private static int nameCounter = 1;
	
	/**
	 * Used for processing
	 */
	private static final char opConcat = 2;
	
	/**
	 * The name of the rule being built
	 */
	private String name;
	
	/**
	 * A reference to the grammar definition object
	 */
	private GrammarDefinition grammardef;
	
	/**
	 * Whether this (main) rule has the multi-child flag set
	 */
	private boolean multi_child = false;
	
	/**
	 * The set of built rules, including main and sub-rules
	 */
	private Hashtable<String, Vector<GrammarRule>> rules = new Hashtable<String, Vector<GrammarRule>>();
	
	/**
	 * Stacks used for processing
	 */
	private Stack<StateGraph<GrammarState>> operandStack = new Stack<StateGraph<GrammarState>>();
	private Stack<Character> operatorStack = new Stack<Character>();
	private Stack<String> nameStack = new Stack<String>();	
	
	/**
	 * A bunch of getters
	 */
	public String getName() { return name; }
	public Hashtable<String, Vector<GrammarRule>> getRules() { return rules; }
	public Vector<GrammarRule> getRules(String rulename) { return rules.get(rulename); }
	
	/**
	 * Constructor.
	 */
	public GrammarRuleBuilder (String name, GrammarTokenizer tokenizer, GrammarDefinition grammardef) throws GrammarDefinitionException, TokenizerException {
		this.name = name;
		this.grammardef = grammardef;
		
		try {
			parse(tokenizer);
		}
		catch (NoSuchTokenException ex) {
			throw new GrammarDefinitionException("Invalid: " + ex.getValue(), tokenizer.getLineNumber());
		}
		catch (GrammarDefinitionException ex) {
			throw new GrammarDefinitionException(ex.getMessage(), tokenizer.getLineNumber());
		}
	}
	
	/**
	 * Parse definition using the given tokenizer (assumed to be positioned at the beginning of the RHS).  
	 */
	private void parse(GrammarTokenizer tokenizer) throws GrammarDefinitionException, TokenizerException {
		
		Token tok;
		
		// include main rule name in the name stack
		nameStack.push(name);
		
	tokenloop:
		while ( true ) {
			
			tok = tokenizer.nextToken();
			
			switch(tok.type) {
				case GrammarTokenizer.EOF_TOKEN: 
				case GrammarTokenizer.EOL_TOKEN:
					break tokenloop;
			
				case GrammarTokenizer.OP_TOKEN:
					pushOperator(tok.value.charAt(0));
					break;
					
				case GrammarTokenizer.LPAREN_TOKEN:
					pushNewName();
					pushOperator(tok.value.charAt(0), false);
					operandStack.push(null); // mark it
					break;
					
				case GrammarTokenizer.RPAREN_TOKEN:
					if (operandStack.peek() == null) {
						// empty subpattern
						char op;
						if ((op = operatorStack.pop()) != '(') {
							throw new GrammarDefinitionException("Missing operand for " + op + " operation");
						}
						nameStack.pop();
						operandStack.pop();
						continue;
					}
					
					while ( true ) {
						
						if (operatorStack.empty()) {
							throw new GrammarDefinitionException("Unbalanced sub-expression, could not find beginning (");
						}
						
						if (operatorStack.peek() == '(') {
							operatorStack.pop();
							break;
						}
					
						evaluate();
					}
					
					String subrulename = nameStack.pop();
					
					addSubrule(subrulename, operandStack.pop());
					
					operandStack.pop(); //pop off null marker
					
					pushOperand(new GrammarState(subrulename, GrammarState.RULE));
					
					break;
					
				case GrammarTokenizer.ID_TOKEN:
					pushOperand(new GrammarState(tok.value, GrammarState.UNKNOWN));
					break;
					
				case GrammarTokenizer.EPSILON_TOKEN:
					pushOperand(new GrammarState(tok.value, GrammarState.EPSILON));
					break;
					
				case GrammarTokenizer.MULTI_CHILD_TOKEN:
					multi_child = true;
					
					tok = tokenizer.nextToken();
					
					if (tok.type != GrammarTokenizer.EOL_TOKEN && tok.type != GrammarTokenizer.EOF_TOKEN) {
						throw new GrammarDefinitionException("Expected eol after multi-child token");
					}
					
					break tokenloop;
				default:
					throw new GrammarDefinitionException("Unexpected " + tok.name + " (" + tok.value + ")");
			}
			
			// TODO remove peekToken?
			detectImplicitConcat(tok, peekToken(tokenizer));
			
		}
		
		if (operandStack.isEmpty()) {
			throw new GrammarDefinitionException("Empty rule definition");
		}
		
		while (!operatorStack.empty()) {
			if (operatorStack.peek() == '(') {
				throw new GrammarDefinitionException("Unbalanced sub-expression, could not find end )");
			}
			
			evaluate();
		}
		
		addRule(nameStack.pop(), operandStack.pop());
		
		for (GrammarRule rule : rules.get(name)) {
			
			// implicit EOF token for the first rule of the grammar
			if (name.equals(grammardef.getStartRuleName())) {
				rule.getGraph().addLast(new GrammarState("eof", GrammarState.TOKEN));
			}
			
			rule.setMultiChild(multi_child);
			
		}
		
	}
	
	/**
	 * Push a new sub-rule name onto the name stack
	 */
	private void pushNewName() {
		nameStack.push(getNewName());
	}
	
	/**
	 * Creates a different sub-rule name on every call (based on the current rule's name)  
	 */
	private String getNewName() {
		return name + "{" + (nameCounter++) + "}";
	}
	
	/**
	 * Push an operand onto the operand stack
	 */
	private void pushOperand(GrammarState state) {
		StateGraph<GrammarState> sg = new StateGraph<GrammarState>();
		
		sg.add(state);
		
		operandStack.push(sg);
	}
	
	/**
	 * Push an operator onto the operator stack
	 */
	private void pushOperator(Character c) throws GrammarDefinitionException { pushOperator(c, true); }
	private void pushOperator(Character c, boolean eval) throws GrammarDefinitionException {
		if (eval) {
			// evaluate while there are operators and they have precedence over the one pushed
			while (!operatorStack.empty() && precedence(c, operatorStack.peek().charValue())) {
				evaluate();
			}
		}
		
		// push the operator
		operatorStack.push(c);
	}
	

	/**
	 * Add a sub-rule to the rule set 
	 */
	private void addSubrule(String rulename, StateGraph<GrammarState> rulegraph) {
		addRule(new GrammarRule(rulename, rulegraph, false, grammardef, true));
	}
	
	/**
	 * Add a regular rule to the rule set
	 */
	private void addRule(String rulename, StateGraph<GrammarState> rulegraph) {
		addRule(new GrammarRule(rulename, rulegraph, false, grammardef));
	}

	/**
	 * Add the given rule to the rule set
	 */
	private void addRule(GrammarRule rule) {
		if (!rules.containsKey(rule.getName())) rules.put(rule.getName(), new Vector<GrammarRule>());
		
		rules.get(rule.getName()).add(rule);
	}
	
	/**
	 * Evaluate the operator on top of the operator stack
	 */
	private void evaluate() throws GrammarDefinitionException {
		char op = operatorStack.pop().charValue();
		
		switch (op) {
			case opConcat:
				evalConcat();
				break;
			case '|':
				evalAlternative();
				break;
			case '*':
				evalZeroPlus();
				break;
			case '?':
				evalOptional(); 
				break;
			default:
				throw new RuntimeException("Unrecognized operator: " + op);
		}
	}
	
	/**
	 * Evaluate a concatenation
	 */
	private void evalConcat() throws GrammarDefinitionException {
		StateGraph<GrammarState> a, b;
		
		if (operandStack.size() < 2) {
			throw new GrammarDefinitionException("Missing operand for CONCAT operation");
		}
		
		b = operandStack.pop();
		a = operandStack.pop();
		
		if (a == null || b == null) {
			throw new GrammarDefinitionException("Missing operand for CONCAT operation");
		}
		
		a.addAll(b);
		
		operandStack.push(a);
	}
	
	/**
	 * Evaluate an alternative (OR)
	 */
	private void evalAlternative() throws GrammarDefinitionException {
		if (operandStack.size() < 2 || operandStack.peek() == null) {
			throw new GrammarDefinitionException("Missing operand for ALTERN (|) operation");
		}
		
		if (nameStack.size() == 1) {
			addRule(nameStack.peek(), operandStack.pop());
		} else {
			addSubrule(nameStack.peek(), operandStack.pop());
		}
		
		if (operandStack.peek() == null) {
			throw new GrammarDefinitionException("Missing operand for ALTERN (|) operation");
		}
	}
	
	/**
	 * Evaluate a zero-plus (*)
	 */
	private void evalZeroPlus() throws GrammarDefinitionException {
		
		//TO-DO {E} ? Exception on epsilon graph ?
		
		if (operandStack.size() < 1 || operandStack.peek() == null) {
			throw new GrammarDefinitionException("Missing operand for ZERO-PLUS (*) operation");
		}
		
		String subrulename = getNewName();
		
		StateGraph<GrammarState> subrulegraph = operandStack.pop();
		subrulegraph.addLast(new GrammarState(subrulename, GrammarState.RULE)); // repeat
		
		StateGraph<GrammarState> epsilongraph = new StateGraph<GrammarState>();
		epsilongraph.add(new GrammarState("\\0", GrammarState.EPSILON));
		
		addSubrule(subrulename, subrulegraph);
		addSubrule(subrulename, epsilongraph);
		
		pushOperand(new GrammarState(subrulename, GrammarState.RULE));
		
	}
	
	/**
	 * Evaluate an optional (?)
	 */
	private void evalOptional() throws GrammarDefinitionException {
		
		//TO-DO {E} ? Exception on epsilon graph ?
		
		if (operandStack.size() < 1 || operandStack.peek() == null) {
			throw new GrammarDefinitionException("Missing operant for ZERO-PLUS (*) operation");
		}
		
		String subrulename = getNewName();
		
		StateGraph<GrammarState> subrulegraph = operandStack.pop();
		
		StateGraph<GrammarState> epsilongraph = new StateGraph<GrammarState>();
		epsilongraph.add(new GrammarState("\\0", GrammarState.EPSILON));
		
		addSubrule(subrulename, subrulegraph);
		addSubrule(subrulename, epsilongraph);
		
		pushOperand(new GrammarState(subrulename, GrammarState.RULE));
		
	}
	
	/**
	 * Push a concat operation if appropriate 
	 */
	private void detectImplicitConcat(Token tokLeft, Token tokRight) throws GrammarDefinitionException {
		int[] leftOK = { GrammarTokenizer.ID_TOKEN, GrammarTokenizer.EPSILON_TOKEN, GrammarTokenizer.RPAREN_TOKEN, GrammarTokenizer.OP_TOKEN };
		int[] rightOK = { GrammarTokenizer.ID_TOKEN, GrammarTokenizer.EPSILON_TOKEN, GrammarTokenizer.LPAREN_TOKEN };
		
		if (Utils.in_array(tokLeft.type, leftOK) && !tokLeft.value.equals("|") && Utils.in_array(tokRight.type, rightOK)) {
			pushOperator(opConcat);
		}
	}
	
	/**
	 * Returns whether the precedence of opLeft <= opRight 
	 */
	private static boolean precedence (char opLeft, char opRight) {
		if(opLeft == opRight) return true;

		// define high precedence ops
		char[] highops = { '*', '?'};
		
		if(Utils.in_array(opLeft, highops)) return false;
		if(Utils.in_array(opRight, highops)) return true;

		// define medium precedence ops
		char[] medops = { opConcat };
		
		if(Utils.in_array(opLeft, medops)) return false;
		if(Utils.in_array(opRight, medops)) return true;

		// define low precedence ops
		char[] lowops = { '|' };
		
		if(Utils.in_array(opLeft, lowops)) return false;
		
		return true;
	}
	
	/**
	 * Returns the next token without actually popping it off
	 */
	private static Token peekToken(GrammarTokenizer tokenizer) throws TokenizerException {
		Token tok = tokenizer.nextToken();
		
		tokenizer.pushToken();
		
		return tok;
	}
	
}
