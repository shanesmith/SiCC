import java.util.EmptyStackException;
import java.util.Stack;
import java.util.Vector;


public class GrammarRuleBuilder {

	private String name;
	
	private GrammarDefinition grammardef;
	
	private boolean multi_child = false;
	
	private Stack<StateGraph<GrammarState>> operandStack = new Stack<StateGraph<GrammarState>>();
	private Stack<Character> operatorStack = new Stack<Character>();
	
	private Vector<StateGraph<GrammarState>> graphs = new Vector<StateGraph<GrammarState>>();
	
	private Vector<GrammarRule> rules = new Vector<GrammarRule>();
	
	static final char opConcat = 2;
	
	public String getName() { return name; }
	public Vector<GrammarRule> getRules() { return rules; }
	
	public GrammarRuleBuilder (GrammarTokenizer tokenizer, Vector<String> tokenNames, GrammarDefinition grammardef) throws Exception {
		this.grammardef = grammardef;
		parse(tokenizer, tokenNames);
	}
	
	private void parse(GrammarTokenizer tokenizer, Vector<String> tokenNames) throws Exception {
		
		Token tok;
		
		// Get Name
		tok = getToken(tokenizer);
		
		if (!tok.is("id")) throw new Exception("(" + tok.line + ") Rule must start with a valid ID!");
		
		name = tok.value;
		
		// Get Seperator ->
		tok = getToken(tokenizer);
		
		if (!tok.is("sep")) throw new Exception("(" + tok.line + ") Rule seperator -> not found after rule name!");
		
		// Parse the definition
		parseDefinition(tokenizer, tokenNames);
		
		for (StateGraph<GrammarState> g : graphs) {
			rules.add(new GrammarRule(name, g, multi_child, grammardef));
		}
		
		/*StateGraph<GrammarState> graph = parseDefinition(tokenizer, tokenNames);
		
		Stack<GrammarState> process = new Stack<GrammarState>();
		
		process.push(graph.firstElement());
		
		while (!process.isEmpty()) {
			
			GrammarState processState = process.pop();
			
			if (processState.getType() == GrammarState.EMPTY) {
				process.addAll(processState.getNext());
			} else {
				StateGraph<GrammarState> copy = graph.copy();
				
			}
			
		}
		*/
		
	}
	
	private void parseDefinition(GrammarTokenizer tokenizer, Vector<String> tokenNames) throws Exception {
		
		Token tok;
		
		while ( !(tok=getToken(tokenizer)).is("eol") ) {
			
			if (tok.is("op")) {
				pushOperator(tok.value.charAt(0));
			}
			else if (tok.is("lparen")) {
				pushOperator(tok.value.charAt(0), false);
			}
			else if (tok.is("rparen")) {
				while ( true ) {
					
					if (operatorStack.empty()) throw new Exception("Could not find beggining (");
					
					if (operatorStack.peek() == '(') {
						operatorStack.pop();
						break;
					}
				
					evaluate();
				}
			}
			else if (tok.is("id")) {
				int type = tokenNames.contains(tok.value) ? GrammarState.TOKEN_TYPE : GrammarState.RULE_TYPE;
				pushOperand(new GrammarState(tok.value, type));
			}
			else if (tok.is("multi_child")) {
				multi_child = true;
				if (!getToken(tokenizer).is("eol")) throw new Exception("Expected eol after multi child token!");
				break;
			}
			else {
				throw new Exception("(" + tok.line + ") Invalid token type: " + tok.name);
			}
			
			detectImplicitConcat(tok, peekToken(tokenizer));
			
		}
		
		while (!operatorStack.empty()) {
			char c = operatorStack.peek();
			
			if (c == '(') throw new Exception("Could not find end subpattern ')'");
			if (c == '[') throw new Exception("Could not find end character class ']");
			
			evaluate();
		}
		
		includeGraph(operandStack.pop());
		
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
	
	// Make sure we have an empty start and empty end
	private void includeGraph(StateGraph<GrammarState> graph) {
		GrammarState start = new GrammarState();
		GrammarState end = new GrammarState();
		
		start.addNext(graph.start());
		graph.addFirst(start);
		
		graph.end().addNext(end);
		graph.addLast(end);
		
		graphs.add(graph);
	}
	
	private void evaluate() throws Exception{
		char op = operatorStack.pop().charValue();
		
		try {
			switch (op) {
				case opConcat:
					evalConcat();
					break;
				case '|':
					if (operatorStack.size() == 0) {
						// top level alternative
						includeGraph(operandStack.pop());
					} else {
						evalAlternative();
					}
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
	
	private void evalConcat() {
		StateGraph<GrammarState> a, b;
		
		b = operandStack.pop();
		a = operandStack.pop();
		
		a.lastElement().addNext(b.firstElement());
		
		a.addAll(b);
		
		operandStack.push(a);
	}
	
	private void evalAlternative() {
		StateGraph<GrammarState> a, b;
		
		b = operandStack.pop();
		a = operandStack.pop();
		
		GrammarState start = new GrammarState();
		GrammarState end = new GrammarState();
		
		start.addNext(a.firstElement());
		start.addNext(b.firstElement());

		a.lastElement().addNext(end);
		b.lastElement().addNext(end);
				
		b.addLast(end);
		
		a.addFirst(start);
		a.addAll(b);
		
		operandStack.push(a);
	}
	
	private void evalZeroPlus() {
		StateGraph<GrammarState> g = operandStack.pop();
		
		GrammarState start = new GrammarState();
		GrammarState end = new GrammarState();
		
		start.addNext(end);
		start.addNext(g.firstElement());
		
		g.lastElement().addNext(end);
		g.lastElement().addNext(g.firstElement());
		
		g.addFirst(start);
		g.addLast(end);
		
		operandStack.push(g);
	}
	
	private void evalOptional() {
		StateGraph<GrammarState> g = operandStack.pop();
		
		GrammarState start = new GrammarState();
		GrammarState end = new GrammarState();
		
		start.addNext(end);
		start.addNext(g.firstElement());
		
		g.lastElement().addNext(end);
		
		g.addFirst(start);
		g.addLast(end);
		
		operandStack.push(g);
	}
	
	private void detectImplicitConcat(Token tokLeft, Token tokRight) throws Exception {
		if (tokLeft == null || tokRight == null) return;
		
		if (tokLeft.is("id") || tokLeft.is("rparen") || (tokLeft.is("op") && !tokLeft.value.equals("|"))) {
			if (tokRight.is("id") || tokRight.is("lparen")) {
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
