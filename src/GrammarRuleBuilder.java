import java.util.EmptyStackException;
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
	public GrammarRuleBuilder (String name, GrammarTokenizer tokenizer, GrammarDefinition grammardef, boolean first) throws Exception {
		this.name = name;
		this.grammardef = grammardef;
		parse(tokenizer, first);
	}
	
	/**
	 * Parse definition using the given tokenizer (assumed to be positioned at the beginning of the RHS).  
	 */
	private void parse(GrammarTokenizer tokenizer, boolean first) throws Exception {
		
		Token tok;
		
		// include main rule name in the name stack
		nameStack.push(name);
		
	tokenloop:
		while ( true ) {
			
			tok = getToken(tokenizer);
			
			switch(tok.type) {
				case GrammarTokenizer.EOL_TOKEN:
					break tokenloop;
			
				case GrammarTokenizer.OP_TOKEN:
					pushOperator(tok.value.charAt(0));
					break;
					
				case GrammarTokenizer.LPAREN_TOKEN:
					pushNewName();
					pushOperator(tok.value.charAt(0), false);
					break;
					
				case GrammarTokenizer.RPAREN_TOKEN:
					while ( true ) {
						
						if (operatorStack.empty()) throw new Exception("Could not find beggining (");
						
						if (operatorStack.peek() == '(') {
							operatorStack.pop();
							break;
						}
					
						evaluate();
					}
					
					String subrulename = nameStack.pop();
					
					addSubrule(subrulename, operandStack.pop());
					
					pushOperand(new GrammarState(subrulename, GrammarState.RULE));
					break;
					
				case GrammarTokenizer.ID_TOKEN:
					pushOperand(new GrammarState(tok.value, GrammarState.UNKNOWN));
					break;
					
				case GrammarTokenizer.MULTI_CHILD_TOKEN:
					multi_child = true;
					
					if (getToken(tokenizer).type != GrammarTokenizer.EOL_TOKEN) throw new Exception("Expected eol after multi child token!");
					
					break tokenloop;
					
				default:
					throw new Exception("(" + tok.line + ") Invalid token type: " + tok.name);
			}
			
			// TODO remove peekToken?
			detectImplicitConcat(tok, peekToken(tokenizer));
			
		}
		
		if (first) {
			pushOperator(opConcat);
			pushOperand(new GrammarState("eof", GrammarState.TOKEN));
		}
		
		while (!operatorStack.empty()) {
			char c = operatorStack.peek();
			
			if (c == '(') throw new Exception("Could not find end subpattern ')'");
			
			evaluate();
		}
		
		addRule(nameStack.pop(), operandStack.pop());
		
		for (GrammarRule rule : rules.get(this.name)) {
			rule.setMultiChild(multi_child);
		}
		
	}
	
	private void pushNewName() {
		nameStack.push(getNewName());
	}
	
	private String getNewName() {
		return name + "{" + nameCounter++ + "}";
	}
	
	private void pushOperand(GrammarState state) {
		StateGraph<GrammarState> sg = new StateGraph<GrammarState>();
		
		sg.add(state);
		
		operandStack.push(sg);
	}
	
	/*
	 *  push operator
	 */
	private void pushOperator(Character c) throws Exception { pushOperator(c, true); }
	private void pushOperator(Character c, boolean eval) throws Exception {
		if (eval) {
			// evaluate while there are operators and they have precedence over the one pushed
			while (!operatorStack.empty() && precedence(c, operatorStack.peek().charValue())) {
				evaluate();
			}
		}
		
		// push the operator
		operatorStack.push(c);
	}
	
	private void addSubrule(String rulename, StateGraph<GrammarState> rulegraph) {
		GrammarRule rule = new GrammarRule(rulename, rulegraph, false, grammardef, true);
		
		addRule(rule);
	}
	
	private void addRule(String rulename, StateGraph<GrammarState> rulegraph) {
		GrammarRule rule = new GrammarRule(rulename, rulegraph, false, grammardef);
		
		addRule(rule);
	}
	
	private void addRule(GrammarRule rule) {
		if (!rules.containsKey(rule.getName())) rules.put(rule.getName(), new Vector<GrammarRule>());
		
		rules.get(rule.getName()).add(rule);
	}
	
	private void evaluate() throws Exception{
		char op = operatorStack.pop().charValue();
		
		try {
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
					throw new Exception("Operator error on " + op);
			}
		}
		catch (EmptyStackException e) {
			throw new Exception("Not enough operands for operation '" + op + "'");
		}
	}
	
	private void evalConcat() throws Exception {
		StateGraph<GrammarState> a, b;
		
		b = operandStack.pop();
		a = operandStack.pop();
		
		a.addAll(b);
		
		operandStack.push(a);
	}
	
	private void evalAlternative() {
		if (nameStack.size() == 1) {
			addRule(nameStack.peek(), operandStack.pop());
		} else {
			addSubrule(nameStack.peek(), operandStack.pop());
		}
	}
	
	private void evalZeroPlus() throws Exception {
		
		String subrulename = getNewName();
		
		StateGraph<GrammarState> subrulegraph = operandStack.pop();
	
		GrammarState repeat = new GrammarState(subrulename, GrammarState.RULE);
		
		subrulegraph.addLast(repeat);
		
		addSubrule(subrulename, subrulegraph);
		addSubrule(subrulename, null);
		
		pushOperand(new GrammarState(subrulename, GrammarState.RULE));
		
	}
	
	private void evalOptional() throws Exception {
		
		String subrulename = getNewName();
		
		StateGraph<GrammarState> subrulegraph = operandStack.pop();
		
		addSubrule(subrulename, subrulegraph);
		addSubrule(subrulename, null);
		
		pushOperand(new GrammarState(subrulename, GrammarState.RULE));
		
	}
	
	private void detectImplicitConcat(Token tokLeft, Token tokRight) throws Exception {
		if (tokLeft == null || tokRight == null) return;
		
		if (tokLeft.type == GrammarTokenizer.ID_TOKEN || tokLeft.type == GrammarTokenizer.RPAREN_TOKEN || (tokLeft.type == GrammarTokenizer.OP_TOKEN && !tokLeft.value.equals("|"))) {
			if (tokRight.type == GrammarTokenizer.ID_TOKEN || tokRight.type == GrammarTokenizer.LPAREN_TOKEN) {
				pushOperator(opConcat);
			}
		}
	}
	
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
	
	private static Token getToken(GrammarTokenizer tokenizer) throws Exception {
		Token tok = tokenizer.nextToken();
		
		if (tok == null) throw new Exception("Unexpected end of file!");
		
		return tok;
	}
	
	private static Token peekToken(GrammarTokenizer tokenizer) throws Exception {
		Token tok = tokenizer.nextToken();
		
		tokenizer.pushToken();
		
		return tok;
	}
	
}
