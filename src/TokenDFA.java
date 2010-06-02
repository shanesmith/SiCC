
/*
 * A DFA based on a token definitions
 */

import java.util.*;

public class TokenDFA {

	private static int nextPos = 1;
	
	private int position = nextPos++;
	
	/*
	 * A reference back to the Tokenizer that holds this DFA, needed for embedding tokens
	 */
	TokenizerDefinition tokendef;	
	
	String name, regexp;
	
	boolean internal = false;
	
	/*
	 * Stacks used to build the NFA
	 */
	Stack<StateGraph<TokenizerNFAState>> operandStack = new Stack<StateGraph<TokenizerNFAState>>();
	Stack<Character> operatorStack = new Stack<Character>();
	
	/*
	 * Graphs
	 */
	StateGraph<TokenizerNFAState> NFA; // intermediate NFA
	StateGraph<TokenizerDFAState> DFA; // resulting DFA
	
	/*
	 * Character code used to define concatenation of characters
	 */
	static final char opConcat = 2;
	
	/*
	 * Useful character lists 
	 */
	static final char[] ignoreList = { 32/*space*/, 9/*tab*/ };
	static final char[] operatorlist = { '?', '*', ':', '(', ')', '+', '^', '|', '[', ']', '#' };
	
	/*
	 * Constructors
	 */
	public TokenDFA(String name, StateGraph<TokenizerNFAState> NFA) throws Exception {

		this.name = name;
		this.NFA = NFA;
		
		if (NFA == null) throw new TokenizerException("Passed token NFA cannot be null!", this);
		
		convertToDFA();
	}
	
	public TokenDFA(String name, String regexp, TokenizerDefinition tokendef) throws Exception {
		this(name, regexp, tokendef, false);
	}
	
	// TODO token name validation?
	public TokenDFA(String name, String regexp, TokenizerDefinition tokendef, boolean internal) throws Exception {
		
		this.name = name;
		this.regexp = regexp;
		this.tokendef = tokendef;
		this.internal = internal;
		
		if (regexp == null) throw new TokenizerException("Token regexp cannot be null!", this);
		if (regexp.length() == 0) throw new TokenizerException("Token regexp cannot be empty!", this);
		if (tokendef == null) throw new TokenizerException("Passed tokenizer cannot be null!", this);
		
		// remove characters to ignore from the regexp
		for (char c : ignoreList) {
			this.regexp = this.regexp.replace(String.valueOf(c), "");
		}
		
		// create the intermediate NFA
		createNFA();
		
		// convert the NFA to a DFA
		convertToDFA();
	}

	public void setInternal(boolean set) { internal = set; }
	public boolean isInternal() { return internal; }
	
	public String getRegexp() { return regexp; }
	
	public TokenizerDFAState getStartState() { return DFA.start(); }
	
	public int getPosition() { return position; }
	
	/*
	 * Returns whether or not this DFA matches the given string
	 */
	public boolean matches(String str) {
		
		TokenizerDFAState curState = DFA.firstElement();
		
		for (char c : str.toCharArray()) {
			
			curState = curState.doTransition(c);
			
			if (curState == null) return false;
			
		}
		
		return curState.isAccepting();
		
	}
	
	public void alternNFA(StateGraph<TokenizerNFAState> alt) throws Exception {
		TokenizerNFAState start = new TokenizerNFAState();
		TokenizerNFAState end = new TokenizerNFAState();
		
		start.addTransition(null, NFA.firstElement());
		start.addTransition(null, alt.firstElement());
		
		NFA.lastElement().addTransition(null, end);
		alt.lastElement().addTransition(null, end);
		
		NFA.addFirst(start);
		NFA.addAll(alt);
		NFA.addLast(end);
		
		convertToDFA();
	}
	
	/*
	 * Create an intermediate NFA based on the regexp
	 */
	private void createNFA() throws Exception {
		
		char c;
		
		boolean charclass = false;
		
		// iterate over the regexp's characters
		for (int pos = 0; pos < regexp.length(); pos++) {
			
			c = regexp.charAt(pos);
			
			if (isOperator(c)) {
				// operator				
				
				if (c == ':') {
					// embed token
					
					String name = "";
					
					while ( true ) {
						
						if (pos + 1 == regexp.length()) throw new TokenizerException("Could not find end ':' of embed token.", this);
						
						c = regexp.charAt(++pos);
						
						if (c == ':') break;
						
						name += String.valueOf(c);
					}
					
					TokenDFA embedToken = tokendef.getTokenDFA(name);
					
					if (embedToken == null) throw new TokenizerException("Cannot find token \"" + name + "\" for embedding!", this);
					
					// push a _copy_ of the token's graph
					pushOperand(copyGraph(embedToken.NFA));
				}
				else if (c == ')') {
					// end of a subpattern, evaluate inside

					while ( true ) {
						
						if (operatorStack.empty()) throw new TokenizerException("Could not find beggining (", this);
						
						if (operatorStack.peek() == '(') {
							operatorStack.pop();
							break;
						}
					
						evaluate();
					}
					
				}
				else if (c == ']') {					
					charclass = false;
					
					if (!operatorStack.isEmpty() && operatorStack.peek() == '^') {
						evaluate();
					}
				}
				else if (c == '[') {
					charclass = true;
					
					TokenizerNFAState first = new TokenizerNFAState();
					TokenizerNFAState last = new TokenizerNFAState();
					
					StateGraph<TokenizerNFAState> charclassGraph = new StateGraph<TokenizerNFAState>();
					
					charclassGraph.add(first);
					charclassGraph.add(last);
					
					operandStack.push(charclassGraph);					
				}
				else if (c == '(' || c == '^') {
					// push without evaluating
					pushOperator(c, false);
				}
				else {
					// default to simple push
					pushOperator(c);
					
				}
				
			} 
			else {
				// operand
				
				if (c == '\\') {
					// escaped character
					
					if (pos == regexp.length()-1) throw new TokenizerException("Nothing following the escape character!", this);
					
					c = regexp.charAt(++pos);
					
					switch (c) {
						case 's': c = 32; break; // a space
						case 'n': c = '\n'; break;
						case 'r': c = '\r'; break;
						case 't': c = '\t'; break;
						case '.': case '\\': break;
						default:
							if (!isOperator(c)) throw new TokenizerException("Invalid escaping of character " + c, this);
					}
					
				}
				else if (c == '.') {
					c = TokenizerState.wildcard;
				}
				
				if (charclass) {
					addToCharClass(c);
				} else {				
					pushOperand(c);
				}
				
			}
			
			if (!charclass && pos != regexp.length()-1) {
				// detect and push implicit concat operator
				detectImplicitConcat(regexp, pos);
			}
			
		}
		
		// done reading in regexp, finish operators
		while (!operatorStack.empty()) {
			c = operatorStack.peek();
			
			if (c == '(') throw new TokenizerException("Could not find end subpattern ')'", this);
			if (c == '[') throw new TokenizerException("Could not find end character class ']", this);
			
			evaluate();
		}
		
		if (operandStack.empty()) throw new TokenizerException("Empty NFA stack", this);
		
		// NFA is at the top of the operand
		NFA = operandStack.pop();
		
		if (NFA.isEmpty()) throw new TokenizerException("Empty NFA", this);
		
		// last state of NFA is accepting
		NFA.lastElement().setAccepting(true);
	}
	
	/*
	 * Convert the intermediate NFA to a DFA 
	 */
	private void convertToDFA() throws Exception {
		
		// make sure we have an NFA
		if (NFA == null) throw new TokenizerException("Empty NFA, cannot convert to DFA", this);
		
		// reset auto state id
		TokenizerState.resetNextID();
		
		// the state we are currently processing
		TokenizerDFAState processState;
		
		// a process stack
		Stack<TokenizerDFAState> process = new Stack<TokenizerDFAState>();
		
		// create a new graph for the DFA
		DFA = new StateGraph<TokenizerDFAState>();
		
		// define the start state of the DFA as the epsilon closure of NFA's start state		
		TokenizerDFAState DFAStartState = new TokenizerDFAState(epsilonClosure(NFA.start()));
		
		DFA.add(DFAStartState);
		process.push(DFAStartState);
		
		// iterate over process stack
		while ( !process.empty() ) {
			processState = process.pop();
			
			// for each possible character match of the state's NFA
			for (Character c : processState.getNFATransitionCharacters()) {
				
				// ignore epsilon transitions
				if (c == null) continue;
				
				// get all states attainable with the character match
				Vector<TokenizerNFAState> moveResult = move(processState, c);
				
				// get all states attainable by an epsilon transition from move's results
				Vector<TokenizerNFAState> epsilonResult = epsilonClosure(moveResult);
				
				// search through the current DFA to see if a similar state already exists
				boolean found = false;
				
				for (TokenizerDFAState s : DFA) {
					if (equals(s.getNFAStates(), epsilonResult)) {
						// similar state exists, no need to create a new one, simply add a transition
						processState.addTransition(c, s);
						if (s.isAccepting()) s.addOwner(this);
						found = true;
						break;
					}
				}
				
				if (!found) {
					// a similar state was not found, create one and add a transition
					TokenizerDFAState s = new TokenizerDFAState(epsilonResult);
					
					DFA.add(s);
					
					processState.addTransition(c, s);
					
					// also include the new state for processing
					process.push(s);
				}
				
			}
			
		}
		
		
	}
	
	/*
	 * Deep copy of the graph (both edges and vertices), and make (this) the owner
	 */
	public StateGraph<TokenizerNFAState> copyGraph(StateGraph<TokenizerNFAState> graph) {
		StateGraph<TokenizerNFAState> newgraph = new StateGraph<TokenizerNFAState>();
		Hashtable<TokenizerNFAState, TokenizerNFAState> copytable = new Hashtable<TokenizerNFAState, TokenizerNFAState>();
		
		graph.start().copyGraph(newgraph, copytable, this);
		
		// move to end.......
		newgraph.add( newgraph.remove( newgraph.indexOf( copytable.get(graph.lastElement()) ) ) );
		
		return newgraph;
	}
	
	public static Vector<TokenizerNFAState> epsilonClosure(TokenizerNFAState s) {
		Vector<TokenizerNFAState> states = new Vector<TokenizerNFAState>();
		states.add(s);
		return epsilonClosure(states);
	}
	
	/*
	 * Returns the epsilon closure (all states attainable by epsilon transitions only) of the given states
	 */
	public static Vector<TokenizerNFAState> epsilonClosure(Vector<TokenizerNFAState> states) {
		Vector<TokenizerNFAState> closure = new Vector<TokenizerNFAState>(states);
		
		Stack<TokenizerNFAState> process = new Stack<TokenizerNFAState>();
				
		// Initialise process stack with all given states
		for (TokenizerNFAState s : states) process.push(s);
		
		while( !process.empty() ) {
			Vector<TokenizerNFAState> epsilonStates = process.pop().getEpsilonTransitions();
			
			for (TokenizerNFAState s : epsilonStates) {
				if ( !closure.contains(s) ) {
					// add to closure and processing stack
					closure.add(s);
					process.push(s);
				}
			}
		}
		
		return closure;
	}
	
	public static Vector<TokenizerNFAState> move(TokenizerDFAState s, Character c) {
		Vector<TokenizerDFAState> states = new Vector<TokenizerDFAState>();
		states.add(s);
		return move(states, c);
	}
	
	/*
	 * Get all states attainable by a specific character match from the given states
	 */
	public static Vector<TokenizerNFAState> move(Vector<TokenizerDFAState> states, Character c) {
		Vector<TokenizerNFAState> result = new Vector<TokenizerNFAState>();
		
		for (TokenizerDFAState from : states) {
			for (TokenizerNFAState s : from.getNFATransitions(c)) { 
				if ( !result.contains(s) ) {
					result.add(s);
				}
			}
		}
		
		return result;
	}
	
	/*
	 * Test for an implicit concat between pos and pos+1
	 * push concat operator to the operatorStack if true
	 */
	private void detectImplicitConcat (String str, int pos) throws Exception {
		
		// operator characters for which a concat is possible
		char[] leftChars = { ')', ']', '*', '?', '+', '.', ':', '#' };
		char[] rightChars = { '(', '[', '.', ':', '#' };
		
		// left and right characters to test
		char left = str.charAt(pos);
		char right = str.charAt(pos+1);
		
		// test for regular input character (possibly escaped operator)
		boolean leftIsInput = !isOperator(left) || (pos!=0 && str.charAt(pos-1) == '\\');
		boolean rightIsInput = !isOperator(right) || str.charAt(pos) == '\\';
		
		// test if character is compatible for concat
		boolean leftCompatible = leftIsInput || Utils.in_array(left, leftChars);
		boolean rightCompatible = rightIsInput || Utils.in_array(right, rightChars);
		
		// if both left and right are concatable, push operator
		if ( leftCompatible && rightCompatible ) {
			pushOperator(opConcat);
		}
		
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
	
	private void addToCharClass(Character c) {
		StateGraph<TokenizerNFAState> charclass = operandStack.pop();
		
		charclass.firstElement().addTransition(c, charclass.lastElement());
		
		operandStack.push(charclass);
	}
	
	/*
	 * Create a new graph from the character match and push it to the stack
	 */
	private void pushOperand(Character c) {
		
		TokenizerNFAState s1 = new TokenizerNFAState(this);
		TokenizerNFAState s2 = new TokenizerNFAState(this);
		
		s1.addTransition(c, s2);
		
		StateGraph<TokenizerNFAState> graph = new StateGraph<TokenizerNFAState>();
		
		graph.add(s1);
		graph.add(s2);
		
		pushOperand(graph);
		
	}
	private void pushOperand(StateGraph<TokenizerNFAState> sg) { operandStack.push(sg); }
	
	/*
	 * Pop an operator from the stack and evaluate
	 */
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
				case '+':
					evalOnePlus();
					break;
				case '?':
					evalOptional(); 
					break;
				case '^':
					evalNegate();
					break;
				default:
					throw new TokenizerException("Operator error on " + op, this);
			}
		}
		catch (EmptyStackException e) {
			throw new TokenizerException("Not enough operands for operation '" + op + "'", this);
		}
	}
	
	/*
	 * Add a default transition to a new state, which is added at the end and becomes the new "accepting" state.
	 * 
	 * The old "accepting" state is left alone to become a dead end.
	 */
	private void evalNegate() {		
		StateGraph<TokenizerNFAState> g = operandStack.pop();
		
		TokenizerNFAState newAcceptingState = new TokenizerNFAState(this);
		
		g.firstElement().addTransition(TokenizerState.neg, newAcceptingState);
		
		g.add(newAcceptingState);
		
		operandStack.push(g);
	}
	
	/*
	 * Evaluate a concat of operands
	 */
	private void evalConcat() {
		StateGraph<TokenizerNFAState> a, b;
		
		b = operandStack.pop();
		a = operandStack.pop();
		
		a.lastElement().addTransition(null, b.firstElement());
		a.addAll(b);
		
		operandStack.push(a);
	}
	
	/*
	 * Evaluate an alternative operation
	 */
	private void evalAlternative() {
		StateGraph<TokenizerNFAState> a, b;
		
		b = operandStack.pop();
		a = operandStack.pop();
		
		TokenizerNFAState start = new TokenizerNFAState(this);
		TokenizerNFAState end = new TokenizerNFAState(this);
		
		start.addTransition(null, a.firstElement());
		start.addTransition(null, b.firstElement());
		
		a.lastElement().addTransition(null, end);
		b.lastElement().addTransition(null, end);
		
		b.addLast(end);
		
		a.addFirst(start);
		a.addAll(b);
		
		operandStack.push(a);
	}
	
	/*
	 * Evaluate a zero-plus operation
	 */
	private void evalZeroPlus() {
		StateGraph<TokenizerNFAState> g = operandStack.pop();
		
		TokenizerNFAState start = new TokenizerNFAState(this);
		TokenizerNFAState end = new TokenizerNFAState(this);
		
		start.addTransition(null, end);
		start.addTransition(null, g.firstElement());
		
		g.lastElement().addTransition(null, end);
		g.lastElement().addTransition(null, g.firstElement());
		
		g.addFirst(start);
		g.addLast(end);
		
		operandStack.push(g);
	}
	
	/*
	 * Evaluate a one-plus operation
	 * 
	 * Same as evalZeroPlus(), but don't create a transition from start to end,
	 * forcing at least one pass through the graph
	 */
	private void evalOnePlus() {
		StateGraph<TokenizerNFAState> g = operandStack.pop();
		
		TokenizerNFAState start = new TokenizerNFAState(this);
		TokenizerNFAState end = new TokenizerNFAState(this);
		
		start.addTransition(null, g.firstElement());
		
		g.lastElement().addTransition(null, end);
		g.lastElement().addTransition(null, g.firstElement());
		
		g.addFirst(start);
		g.addLast(end);
		
		operandStack.push(g);
	}
	
	/*
	 * Evaluate an optional operation
	 * 
	 * Same as evalZeroPlus(), but don't create a transition from lastElement to firstElement,
	 * which removes the loop
	 */
	private void evalOptional() {
		StateGraph<TokenizerNFAState> g = operandStack.pop();
		
		TokenizerNFAState start = new TokenizerNFAState(this);
		TokenizerNFAState end = new TokenizerNFAState(this);
		
		start.addTransition(null, end);
		start.addTransition(null, g.firstElement());
		
		g.lastElement().addTransition(null, end);
		
		g.addFirst(start);
		g.addLast(end);
		
		operandStack.push(g);
	}
	
	/*
	 * Returns true if precedence of opLeft <= opRight
	 */
	private boolean precedence (char opLeft, char opRight) {
		if(opLeft == opRight) return true;

		// define high precedence ops
		char[] highops = { '*', '+', '?', '^' };
		
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
	
	private static boolean isOperator(char c) { return Utils.in_array(c, operatorlist); }
	
	public static boolean equals(Vector<TokenizerNFAState> va, Vector<TokenizerNFAState> vb) {
		
		if (va.size() != vb.size()) return false;
		
		Iterator<TokenizerNFAState> ita = va.iterator();
		Iterator<TokenizerNFAState> itb = vb.iterator();
		
		while (ita.hasNext() && itb.hasNext()) {
			if (!equals(ita.next(), itb.next())) return false;
		}
		
		return true;
		
	}
	
	public static boolean equals(TokenizerState a, TokenizerState b) {
		return a.getID() == b.getID();
	}
	
	public String toString() {
		String str = "[" + name + "]";
		
		if (DFA != null) {
			return str + " (DFA)\n" + DFA.toString();
		}
		else if (NFA != null) {
			return str + " (NFA)\n" + NFA.toString();
		}
		else {
			return str + " (Neither DFA or NFA are set)";
		}
	}
	
}
