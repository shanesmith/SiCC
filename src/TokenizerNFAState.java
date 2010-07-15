import java.util.HashMap;
import java.util.Hashtable;
import java.util.Set;
import java.util.Vector;

/**
 * A state to be used in a NFA for a tokenizer.
 * 
 * Multiple transitions from one character as well as epsilon transitions are allowable 
 */
public class TokenizerNFAState extends TokenizerState {

	/**
	 * A map from characters to states (null key denotes an epsilon transition)
	 */
	private HashMap<Character, Vector<TokenizerNFAState>> transitions = new HashMap<Character, Vector<TokenizerNFAState>>();
	
	/**
	 * Constructor.
	 */
	public TokenizerNFAState() { super(); }
	
	/**
	 * Add a transition on a character to a state
	 */
	public void addTransition(Character c, TokenizerNFAState next) { 
		if (!transitions.containsKey(c)) {
			transitions.put(c, new Vector<TokenizerNFAState>());
		}
		
		transitions.get(c).add(next);
	}
	
	/**
	 * Return attainable states on the given character
	 */
	public Vector<TokenizerNFAState> getTransitions(Character c) {
		Vector<TokenizerNFAState> trans = new Vector<TokenizerNFAState>(); 
		
		if (transitions.containsKey(c)) {
			trans.addAll(transitions.get(c));
		}
		
		// wildcard transitions may be taken if they exist 
		if (transitions.containsKey(TokenizerState.wildcard)) {
			trans.addAll(transitions.get(TokenizerState.wildcard));
		}
		
		// if there were no valid transitions so far, use negative transitions
		if (trans.isEmpty() && transitions.containsKey(TokenizerState.neg)) {
			trans.addAll(transitions.get(TokenizerState.neg));
		}
		
		return trans;
	}
	
	/**
	 * Get states attainable on an epsilon transition 
	 */
	public Vector<TokenizerNFAState> getEpsilonTransitions() {
		if (transitions.containsKey(null)) {
			return transitions.get(null);
		} else {
			return new Vector<TokenizerNFAState>();
		}
	}
	
	/**
	 * Returns all possible transition characters
	 */
	public Set<Character> getTransitionCharacters() {
		return transitions.keySet();
	}
	
	/**
	 * Deep copy of this state and all connected states (by recursion) to the newgraph.
	 * 
	 * copytable keeps a relation between original and copied state, useful for resolving transitions 
	 */
	public void copyGraph(StateGraph<TokenizerNFAState> newgraph, Hashtable<TokenizerNFAState, TokenizerNFAState> copyTable) {
		
		// the copy of this state
		TokenizerNFAState newstate = new TokenizerNFAState(); 
		
		// add copy to the graph
		newgraph.add(newstate);
		
		// add reference between original and copy
		copyTable.put(this, newstate);
		

		// copy transitions of original state to copy state
		for (Character c : transitions.keySet()) {
			for (TokenizerNFAState trans : transitions.get(c)) {
				
				if (!copyTable.containsKey(trans)) {
					// the next state hasn't been copied, yet, so recurse
					trans.copyGraph(newgraph, copyTable);
				}
				
				newstate.addTransition(c, copyTable.get(trans));
				
			}
		}
		
	}
	
	/**
	 * String representation of a state and its transitions 
	 */
	public String toString() {
		String str = "";
		
		str += id + (accepting?"!":"") + " ";
		
		for (Character c : transitions.keySet()) {
			for (TokenizerNFAState st : transitions.get(c)) {
				if (c != null && c == TokenizerState.wildcard) {
					str += "[[" + st.id + (st.accepting?"!":"") + "]] ";
				} else {
					str += "[" + (c==null?" ":Utils.escape(c)) + " >> " + st.id + (st.accepting?"!":"") + "] ";
				}
			}
		}
		
		return str;
	}
	
}
