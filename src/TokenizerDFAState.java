
import java.util.Hashtable;
import java.util.Vector;

public class TokenizerDFAState extends TokenizerState {

	/*
	 * A multimap from Characters to States.
	 * 
	 * Since NFAs can have multiple transitions for a single input, a vector of states is held instead of a single state.
	 * 
	 * Also note that the null key denotes an epsilon transition (Hashtables do not allow null keys, but HashMaps do).
	 */
	private Hashtable<Character, TokenizerDFAState> transitions = new Hashtable<Character, TokenizerDFAState>();
	
	/*
	 * A DFA state is constructed from multiple NFA states when a conversion is done, this is where the NFA states are held.
	 */
	private Vector<TokenizerNFAState> NFAStates;
	
	/*
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
	
	/*
	 * Add a transition on a Character to a state
	 */
	public void addTransition(Character c, TokenizerDFAState next) { 
		transitions.put(c, next);
	}
	
	public TokenizerDFAState doTransition(Character c) {
		TokenizerDFAState trans = transitions.get(c);
		
		if (trans == null) {
			trans = transitions.get(TokenizerState.wildcard);
			
			if (trans == null) {
				trans = transitions.get(TokenizerState.neg);
			}
		}
		
		return trans;
	}
	
	public boolean transitionExists(char c) {
		return doTransition(c) != null;
	}
	
	public Vector<TokenizerNFAState> getNFATransitions(char c) {
		Vector<TokenizerNFAState> trans = new Vector<TokenizerNFAState>();
		
		for (TokenizerNFAState s : NFAStates) {
			trans.addAll(s.getTransitions(c));
		}
		
		return trans;
	}
	
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
	
	public Vector<Character> getTransitionCharacters() {
		return new Vector<Character>(transitions.keySet());
	}
	
	public Vector<TokenizerNFAState> getNFAStates() { return NFAStates; }
	
	public boolean hasTransitions() { return !transitions.isEmpty(); }
	
	/*
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
 