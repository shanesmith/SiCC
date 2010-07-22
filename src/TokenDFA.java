
import java.util.*;

/**
 * TokenDFA represents a token definition with a DFA
 */
public class TokenDFA {

	private static int nextPos = 1;
	
	/**
	 * Character code used to define concatenation of characters
	 */
	static final char opConcat = 2;
	
	/**
	 * Useful character lists 
	 */
	static final char[] ignoreList = { 32/*space*/, 9/*tab*/ };
	static final char[] operatorlist = { '?', '*', ':', '(', ')', '+', '^', '|', '[', ']', '#' };
	
	/**
	 * The position of this token in the definition file
	 */
	private int position = nextPos++;
	
	/**
	 * A reference back to the Tokenizer that holds this DFA, needed for embedding tokens
	 */
	TokenizerDefinition tokendef;	
	
	/**
	 * The token's name and regexp
	 */
	String name, regexp;
	
	/**
	 * Whether the token is defined as internal
	 */
	boolean internal;
	
	/**
	 * Stacks used to build the NFA
	 */
	Stack<StateGraph<TokenizerNFAState>> operandStack = new Stack<StateGraph<TokenizerNFAState>>();
	Stack<Character> operatorStack = new Stack<Character>();
	
	/**
	 * Graphs
	 */
	StateGraph<TokenizerNFAState> NFA; // intermediate NFA
	StateGraph<TokenizerDFAState> DFA; // resulting DFA
	
	/**
	 * Constructor. Simply takes a name and an NFA, and converts it to a DFA.
	 */
	public TokenDFA(String name, StateGraph<TokenizerNFAState> NFA) {
		this.name = name;
		this.NFA = NFA;
		
		convertToDFA();
	}
	
	/**
	 * Constructor.
	 */
	public TokenDFA(String name, String regexp, boolean internal, TokenizerDefinition tokendef) throws TokenizerDefinitionException {
		
		this.name = name;
		this.regexp = regexp.trim();
		this.tokendef = tokendef;
		this.internal = internal;
		
		// create the intermediate NFA
		createNFA();
		
		// convert the NFA to a DFA
		convertToDFA();
	}
	
	/**
	 * Returns whether this DFA matches the given string
	 */
	public boolean matches(String str) {
		
		TokenizerDFAState curState = DFA.firstElement();
		
		for (char c : str.toCharArray()) {
			
			curState = curState.doTransition(c);
			
			if (curState == null) return false;
			
		}
		
		return curState.isAccepting();
		
	}
	
	/**
	 * Adds the given NFA as an alternative to the current DFA.
	 */
	public void alternNFA(StateGraph<TokenizerNFAState> alt) {
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
	
	/**
	 * Create an intermediate NFA based on the regexp previously set.
	 */
	private void createNFA() throws TokenizerDefinitionException {
		
		char c;
		
		// defines whether we are currently reading in a character class
		boolean charclass = false;
		
		//TODO use operandStack null marker instead? (same with GrammarRuleBuilder)
		// used to detect empty sub-patterns
		int operandStackSizeAtOpenSubPattern = 0;
		
		// iterate over the regexp's characters
		for (int pos = 0; pos < regexp.length(); pos++) {
			
			c = regexp.charAt(pos);
			
			if (Utils.in_array(c, ignoreList)) {
				continue;
			}
			
			if (isOperator(c)) { // operator				
				
				if (charclass && c != '^' && c != ']') {
					throw new TokenizerDefinitionException("Operator " + c + " needs escaping in character class");
				}
				
				if (c == ':') { 
					// embed token
					
					String embedTokenName = "";
					
					while ( true ) {
						
						if (pos == regexp.length() - 1) {
							throw new TokenizerDefinitionException("Could not find end ':' of embed token name");
						}
						
						c = regexp.charAt(++pos);
						
						if (c == ':') break;
						
						embedTokenName += String.valueOf(c);
					}
					
					TokenDFA embedToken = tokendef.getTokenDFA(embedTokenName);
					
					if (embedToken == null) {
						throw new TokenizerDefinitionException("Cannot find token \"" + embedTokenName + "\" for embedding");
					}
					
					// push a _copy_ of the token's graph
					pushOperand(copyGraph(embedToken.NFA));
				}
				else if (c == ')') {
					// end of a sub-pattern, evaluate inside

					if (operandStack.size() == operandStackSizeAtOpenSubPattern) {
						throw new TokenizerDefinitionException("Empty subpattern");
					}
					
					while ( true ) {
						
						if (operatorStack.empty()) {
							throw new TokenizerDefinitionException("Could not find beggining subpattern (");
						}
						
						if (operatorStack.peek() == '(') {
							operatorStack.pop();
							break;
						}
					
						evaluate();
					}
					
				}
				else if (c == ']') {		
					// end of a character class
					
					if (!charclass) {
						throw new TokenizerDefinitionException("Character class has not been opened with [");
					}
					
					if (operandStack.peek().firstElement().getTransitionCharacters().isEmpty()) {
						throw new TokenizerDefinitionException("Character classes cannot be empty");
					}
					
					charclass = false;
					
					if (!operatorStack.isEmpty() && operatorStack.peek() == '^') {
						evaluate();
					}
				}
				else if (c == '[') {
					// beginning of a character class
					
					charclass = true;
					
					TokenizerNFAState first = new TokenizerNFAState();
					TokenizerNFAState last = new TokenizerNFAState();
					
					StateGraph<TokenizerNFAState> charclassGraph = new StateGraph<TokenizerNFAState>();
					
					charclassGraph.add(first);
					charclassGraph.add(last);
					
					operandStack.push(charclassGraph);					
				}
				else if (c == '^') {
					// character class negation
					
					// operator only valid in a character class
					if (!charclass) {
						throw new TokenizerDefinitionException("Operator ^ must be in a character class");
					}
						
					// operator needs to be defined before any literals in the character class 
					if (!operandStack.peek().firstElement().getTransitionCharacters().isEmpty()) {
						throw new TokenizerDefinitionException("Character class negation operator ^ needs to be first after opening [, or escaped if literal");
					}
					
					// push without evaluating
					pushOperator(c, false);
				}
				else if (c == '(') {
					// beginning of a sub-pattern
					
					// push without evaluating
					pushOperator(c, false);
					operandStackSizeAtOpenSubPattern = operandStack.size();
				}
				else {
					// default to push (and possible evaluate)
					pushOperator(c);
				}
				
			} else { // operand
				
				// first step, deal with special characters
				if (c == '\\') {
					// escaped character
					
					if (pos == regexp.length()-1) {
						throw new TokenizerDefinitionException("Nothing following the escape character");
					}
					
					c = regexp.charAt(++pos);
					
					switch (c) {
						case 's': c = 32; break; // a space
						case 'n': c = '\n'; break;
						case 'r': c = '\r'; break;
						case 't': c = '\t'; break;
						case '.': case '\\': break;
						default: if (!isOperator(c)) throw new TokenizerDefinitionException("Invalid escaping of character " + c);
					}
					
				}
				else if (c == '.') {
					c = TokenizerState.wildcard;
				}
				
				// second step, add to appropriate place
				if (charclass) {
					addToCharClass(c);
				} else {				
					pushOperand(c);
				}
				
			}
			
			// detect and push implicit concat operator
			// when not in a character class or at the end of the regex
			if (!charclass && pos != regexp.length()-1) {
				detectImplicitConcat(regexp, pos);
			}
			
		}

		// ended the regex, but still in a character class...
		if (charclass) {
			throw new TokenizerDefinitionException("Character class has not been ended with a ]");
		}
		
		// finish evaluating operators
		while (!operatorStack.empty()) {
			c = operatorStack.peek();
			
			if (c == '(') throw new TokenizerDefinitionException("Could not find end subpattern )");
			
			evaluate();
		}
		
		if (operandStack.empty()) {
			throw new RuntimeException("Empty NFA stack");
		}
		
		// NFA is at the top of the operand
		NFA = operandStack.pop();
		
		if (NFA.isEmpty()) {
			throw new RuntimeException("Empty NFA");
		}
		
		// last state of NFA is accepting
		NFA.lastElement().setAccepting(true);
	}
	
	/**
	 * Convert the intermediate NFA to a DFA 
	 */
	private void convertToDFA() {
		
		// make sure we have an NFA
		if (NFA == null) {
			throw new RuntimeException("Empty NFA, cannot convert to DFA");
		}
		
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
	
	/**
	 * Deep copy of the graph (both edges and vertices), and make (this) the owner
	 */
	public StateGraph<TokenizerNFAState> copyGraph(StateGraph<TokenizerNFAState> graph) {
		StateGraph<TokenizerNFAState> newgraph = new StateGraph<TokenizerNFAState>();
		Hashtable<TokenizerNFAState, TokenizerNFAState> copytable = new Hashtable<TokenizerNFAState, TokenizerNFAState>();
		
		graph.start().copyGraph(newgraph, copytable);
		
		// last state in original graph might not be last in new graph, so move it to the end
		newgraph.add( newgraph.remove( newgraph.indexOf( copytable.get(graph.lastElement()) ) ) );
		
		return newgraph;
	}
	
	/**
	 * Returns the epsilon closure of the given state 
	 */
	public static Vector<TokenizerNFAState> epsilonClosure(TokenizerNFAState s) {
		Vector<TokenizerNFAState> states = new Vector<TokenizerNFAState>();
		states.add(s);
		return epsilonClosure(states);
	}
	
	/**
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
	
	/**
	 * Returns all states attainable from the given state with the transition character specified
	 */
	public static Vector<TokenizerNFAState> move(TokenizerDFAState s, Character c) {
		Vector<TokenizerDFAState> states = new Vector<TokenizerDFAState>();
		states.add(s);
		return move(states, c);
	}
	
	/**
	 * Returns all states attainable from the given states with the transition character specified
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
	
	/**
	 * Test for an implicit concat between pos and pos+1 of the string and push the concat operator to the operatorStack if true
	 */
	private void detectImplicitConcat (String str, int pos) throws TokenizerDefinitionException {
		
		// operator characters for which a concat is possible
		char[] leftChars = { ')', ']', '*', '?', '+', '.', ':', '#' };
		char[] rightChars = { '(', '[', '.', ':', '#' };
		
		// left character to test
		char left = str.charAt(pos);
		
		// right character to test (passing over characters to ignore)
		int rightpos = pos;		
		while (Utils.in_array(str.charAt(++rightpos), ignoreList));
		char right = str.charAt(rightpos);
		
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
	
	/**
	 * Push operator
	 */
	private void pushOperator(Character c) throws TokenizerDefinitionException { pushOperator(c, true); }
	private void pushOperator(Character c, boolean eval) throws TokenizerDefinitionException {
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
	 * Push operand
	 */
	private void pushOperand(StateGraph<TokenizerNFAState> sg) { operandStack.push(sg); }
	private void pushOperand(Character c) {
		
		TokenizerNFAState s1 = new TokenizerNFAState();
		TokenizerNFAState s2 = new TokenizerNFAState();
		
		s1.addTransition(c, s2);
		
		StateGraph<TokenizerNFAState> graph = new StateGraph<TokenizerNFAState>();
		
		graph.add(s1);
		graph.add(s2);
		
		pushOperand(graph);
		
	}
	
	/**
	 *	Adds the given character to the top of the operand stack as a character class alternative 
	 */
	private void addToCharClass(Character c) {
		StateGraph<TokenizerNFAState> charclass = operandStack.pop();
		
		charclass.firstElement().addTransition(c, charclass.lastElement());
		
		operandStack.push(charclass);
	}
	
	/**
	 * Pop an operator from the stack and evaluate
	 */
	private void evaluate() throws TokenizerDefinitionException{
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
				throw new RuntimeException("Unrecognized operator " + op);
		}
	}
	
	/**
	 * Replace character class transitions with their complement
	 */
	private void evalNegate() throws TokenizerDefinitionException {
		
		if (operandStack.size() < 1) {
			throw new TokenizerDefinitionException("Missing operand for NEGATE (^) operation");
		}
		
		StateGraph<TokenizerNFAState> graph = operandStack.pop();
		
		TokenizerNFAState first = graph.firstElement();
		TokenizerNFAState last = graph.lastElement();
		
		Set<Character> negCharSet = graph.firstElement().getTransitionCharacters();
		
		char[] negChars = new char[negCharSet.size()];
		
		int i = 0;
		for (Character c : negCharSet) {
			negChars[i++] = c;
		}
		
		first.removeAllTransitions();
		
		// start at 9 since lower values may be used for special meaning
		for (char c = 9; c <= 255; c++) {
			if (!Utils.in_array(c, negChars)) {
				first.addTransition(c, last);
			}
		}
		
		operandStack.push(graph);
	}
	
	/**
	 * Evaluate a concat of operands
	 */
	private void evalConcat() throws TokenizerDefinitionException {
		
		if (operandStack.size() < 2) {
			throw new TokenizerDefinitionException("Missing operand for CONCAT operation");
		}
		
		StateGraph<TokenizerNFAState> a, b;
		
		b = operandStack.pop();
		a = operandStack.pop();
		
		a.lastElement().addTransition(null, b.firstElement());
		a.addAll(b);
		
		operandStack.push(a);
	}
	
	/**
	 * Evaluate an alternative operation
	 */
	private void evalAlternative() throws TokenizerDefinitionException{
		
		if (operandStack.size() < 2) {
			throw new TokenizerDefinitionException("Missing operand for ALTERN (|) operation");
		}
		
		StateGraph<TokenizerNFAState> a, b;
		
		b = operandStack.pop();
		a = operandStack.pop();
		
		TokenizerNFAState start = new TokenizerNFAState();
		TokenizerNFAState end = new TokenizerNFAState();
		
		start.addTransition(null, a.firstElement());
		start.addTransition(null, b.firstElement());
		
		a.lastElement().addTransition(null, end);
		b.lastElement().addTransition(null, end);
		
		b.addLast(end);
		
		a.addFirst(start);
		a.addAll(b);
		
		operandStack.push(a);
	}
	
	/**
	 * Evaluate a zero-plus operation
	 */
	private void evalZeroPlus() throws TokenizerDefinitionException {
		
		if (operandStack.size() < 1) {
			throw new TokenizerDefinitionException("Missing operand for ZERO-PLUS (*) operation");
		}
		
		StateGraph<TokenizerNFAState> g = operandStack.pop();
		
		TokenizerNFAState start = new TokenizerNFAState();
		TokenizerNFAState end = new TokenizerNFAState();
		
		start.addTransition(null, end);
		start.addTransition(null, g.firstElement());
		
		g.lastElement().addTransition(null, end);
		g.lastElement().addTransition(null, g.firstElement());
		
		g.addFirst(start);
		g.addLast(end);
		
		operandStack.push(g);
	}
	
	/**
	 * Evaluate a one-plus operation
	 */
	private void evalOnePlus() throws TokenizerDefinitionException  {
		
		if (operandStack.size() < 1) {
			throw new TokenizerDefinitionException("Missing operand for ONE-PLUS (+) operation");
		}
		
		StateGraph<TokenizerNFAState> g = operandStack.pop();
		
		TokenizerNFAState start = new TokenizerNFAState();
		TokenizerNFAState end = new TokenizerNFAState();
		
		start.addTransition(null, g.firstElement());
		
		g.lastElement().addTransition(null, end);
		g.lastElement().addTransition(null, g.firstElement());
		
		g.addFirst(start);
		g.addLast(end);
		
		operandStack.push(g);
	}
	
	/**
	 * Evaluate an optional operation
	 */
	private void evalOptional() throws TokenizerDefinitionException {
		
		if (operandStack.size() < 1) {
			throw new TokenizerDefinitionException("Missing operand for OPTIONAL (?) operation");
		}
		
		StateGraph<TokenizerNFAState> g = operandStack.pop();
		
		TokenizerNFAState start = new TokenizerNFAState();
		TokenizerNFAState end = new TokenizerNFAState();
		
		start.addTransition(null, end);
		start.addTransition(null, g.firstElement());
		
		g.lastElement().addTransition(null, end);
		
		g.addFirst(start);
		g.addLast(end);
		
		operandStack.push(g);
	}
	
	/**
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
	
	/**
	 * Returns whether the character is an operator 
	 */
	private static boolean isOperator(char c) { return Utils.in_array(c, operatorlist); }
	
	/**
	 * Returns whether both graphs are equal by comparing each item
	 */
	public static boolean equals(Vector<TokenizerNFAState> va, Vector<TokenizerNFAState> vb) {
		
		if (va.size() != vb.size()) return false;
		
		Iterator<TokenizerNFAState> ita = va.iterator();
		Iterator<TokenizerNFAState> itb = vb.iterator();
		
		while (ita.hasNext() && itb.hasNext()) {
			if (ita.next().getID() != itb.next().getID()) return false;
		}
		
		return true;
		
	}
	
	/**
	 * A bunch of getters.
	 */
	public boolean isInternal() { return internal; }
	
	public String getRegexp() { return regexp; }
	
	public TokenizerDFAState getStartState() { return DFA.start(); }
	
	public int getPosition() { return position; }

	/**
	 * String representation.
	 */
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
