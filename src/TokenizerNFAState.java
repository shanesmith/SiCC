import java.util.HashMap;
import java.util.Hashtable;
import java.util.Vector;


public class TokenizerNFAState extends TokenizerState {

	/*
	 * A multimap from Characters to States.
	 * 
	 * Since NFAs can have multiple transitions for a single input, a vector of states is held instead of a single state.
	 * 
	 * Also note that the null key denotes an epsilon transition (Hashtables do not allow null keys, but HashMaps do).
	 */
	private HashMap<Character, Vector<TokenizerNFAState>> transitions = new HashMap<Character, Vector<TokenizerNFAState>>();
	
	public TokenizerNFAState() { super(); }
	public TokenizerNFAState(Object o) { this(); }
	
	/*
	 * Add a transition on a Character to a state
	 */
	public void addTransition(Character c, TokenizerNFAState next) { 
		if (!transitions.containsKey(c)) {
			transitions.put(c, new Vector<TokenizerNFAState>());
		}
		
		transitions.get(c).add(next);
	}
	
	public void removeTransition(Character c, TokenizerNFAState next) {
		if (!transitions.containsKey(c)) return;
		
		transitions.get(c).remove(next);
		
		if (transitions.get(c).isEmpty()) {
			transitions.remove(c);
		}
	}
	
	public Vector<TokenizerNFAState> getTransitions(Character c) {
		Vector<TokenizerNFAState> trans = new Vector<TokenizerNFAState>(); 
		
		if (transitions.containsKey(c)) {
			trans.addAll(transitions.get(c));
		}
		
		if (transitions.containsKey(TokenizerState.wildcard)) {
			trans.addAll(transitions.get(TokenizerState.wildcard));
		}
		
		if (trans.isEmpty() && transitions.containsKey(TokenizerState.neg)) {
			trans.addAll(transitions.get(TokenizerState.neg));
		}
		
		return trans;
	}
	
	
	public Vector<TokenizerNFAState> getEpsilonTransitions() {
		if (transitions.containsKey(null)) {
			return transitions.get(null);
		} else {
			return new Vector<TokenizerNFAState>();
		}
	}
	
	public Vector<Character> getTransitionCharacters() {
		return new Vector<Character>(transitions.keySet());
	}
	
	public boolean hasTransitions() { return !transitions.isEmpty(); }
	
	public void copyGraph(StateGraph<TokenizerNFAState> newgraph, Hashtable<TokenizerNFAState, TokenizerNFAState> copyTable, TokenDFA newowner) {
		TokenizerNFAState newstate = new TokenizerNFAState(); 
		
		copyTable.put(this, newstate);
		
		newgraph.add(newstate);
		
		for (Character c : transitions.keySet()) {
			
			for (TokenizerNFAState trans : transitions.get(c)) {
				
				if (!copyTable.containsKey(trans)) {
					trans.copyGraph(newgraph, copyTable, newowner);
				}
				
				newstate.addTransition(c, copyTable.get(trans));
				
			}
			
		}
		
	}
	
	/*
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
