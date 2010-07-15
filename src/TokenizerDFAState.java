
import java.util.Hashtable;
import java.util.Vector;

/**
 * A state to be used in a DFA for a tokenizer.
 * 
 * Compared to NFA states, there cannot be multiple transitions from one character as well as any epsilon transitions 
 */
public class TokenizerDFAState extends TokenizerState {

	/**
	 * A map of transitions from this state on a character
	 */
	private Hashtable<Character, TokenizerDFAState> transitions = new Hashtable<Character, TokenizerDFAState>();
	
	/**
	 * A DFA state is constructed from multiple NFA states when a conversion is done, this is where the NFA states are held.
	 */
	private Vector<TokenizerNFAState> NFAStates;
	
	/**
	 * Construct a DFA state based on a set of NFA states
	 */
	public TokenizerDFAState(Vector<TokenizerNFAState> NFAStates) {
		super();
		
		this.NFAStates = NFAStates;
		
		// set this state to accepting if any NFA state is accepting
		for (TokenizerNFAState s : NFAStates) {
			if (s.isAccepting()) {
				addOwners(s.getOwners());
				accepting = true;
			}
		}
		
	}
	
	/**
	 * Add a transition on a character to a state
	 */
	public void addTransition(Character c, TokenizerDFAState next) { 
		transitions.put(c, next);
	}
	
	/**
	 * Return the next state given a character, taking into
	 * account possible wildcard or negative implications
	 */
	public TokenizerDFAState doTransition(char c) {
		TokenizerDFAState trans = transitions.get(c);
		
		if (trans == null) {
			// there were no transitions for c, let's try a wildcard
			trans = transitions.get(TokenizerState.wildcard);
			
			if (trans == null) {
				// no transition for c or a wildcard, let's try a negate
				trans = transitions.get(TokenizerState.neg);
			}
		}
		
		return trans;
	}
	
	/**
	 * Returns whether or not we can make a transition on the given character,
	 * including wildcard and negative transitions
	 */
	public boolean transitionExists(char c) {
		return doTransition(c) != null;
	}
	
	/**
	 * Get the transitions of the NFA states on the given character
	 */
	public Vector<TokenizerNFAState> getNFATransitions(char c) {
		Vector<TokenizerNFAState> trans = new Vector<TokenizerNFAState>();
		
		for (TokenizerNFAState s : NFAStates) {
			trans.addAll(s.getTransitions(c));
		}
		
		return trans;
	}
	
	/**
	 * Get all the possible transition characters of the NFA states
	 */
	public Vector<Character> getNFATransitionCharacters() {		
		Vector<Character> result = new Vector<Character>();
		
		if (NFAStates != null) {
			
			for (TokenizerNFAState s : NFAStates) {
				for (Character c : s.getTransitionCharacters()) {
					if ( !result.contains(c) ) result.add(c);
				}
			}
			
		}
		
		return result;
	}
	
	/**
	 * Get all the possible transition characters of this state
	 */
	public Vector<Character> getTransitionCharacters() {
		return new Vector<Character>(transitions.keySet());
	}
	
	/**
	 * Return the NFA states
	 */
	public Vector<TokenizerNFAState> getNFAStates() { return NFAStates; }
	
	/**
	 * String representation of a state and its transitions 
	 */
	public String toString() {
		String str = "";
		
		str += id + (accepting?"!":"") + " ";
		
		TokenizerDFAState st;
		
		for (Character c : transitions.keySet()) {
			st = transitions.get(c);
			
			if (c == TokenizerState.wildcard) {
				str += "[[" + st.id + (st.accepting?"!":"") + "]] ";
			} else {
				str += "[" + Utils.escape(c) + " >> " + st.id + (st.accepting?"!":"") + "] ";
			}
		}
		
		return str;
	}
	
}
 