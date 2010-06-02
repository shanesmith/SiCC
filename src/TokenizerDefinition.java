import java.io.LineNumberReader;
import java.io.Reader;
import java.util.Collection;
import java.util.Hashtable;
import java.util.Vector;


public class TokenizerDefinition {

	private Hashtable<String, TokenDFA> tokenDFAs = new Hashtable<String, TokenDFA>();
	
	private TokenDFA masterDFA;
	
	private boolean finalized = false;
	
	public TokenizerDefinition(Reader definitions) throws Exception {
		parse(new LineNumberReader(definitions));
	}
	
	private void parse(LineNumberReader definition) throws Exception {
		
		String line, name, regexp;
		
		boolean internal;
		
		while( (line=definition.readLine()) != null ) {
			
			line = line.trim();
			
			if (line.isEmpty() || line.charAt(0) == '#') continue;
			
			internal = (line.charAt(0) == ':');
			
			if (internal) line = line.substring(1);
			
			name = line.substring(0, line.indexOf(':'));
			
			regexp = line.substring(line.indexOf(':')+1);
			
			defineToken(name, regexp, internal);
			
		}
		
	}
	
	public void defineToken(String name, String definition) throws Exception {
		defineToken(name, definition, false);
	}
	
	public void defineInternalToken(String name, String definition) throws Exception {
		defineToken(name, definition, true);
	}
	
	public void defineToken(String name, String definition, boolean isInternal) throws Exception {
		finalized = false;
		
		TokenDFA tok = new TokenDFA(name, definition, this, isInternal);
		
		if (tokenDFAs.containsKey(name)) {
			tokenDFAs.get(name).alternNFA(tok.NFA);
		} else {
			tokenDFAs.put(name, tok);
		}
	}
	
	public void finalize() throws Exception {
		// mash all non-internal tokenDFAs into on masterDFA
		
		finalized = true;
		
		TokenizerState.resetNextID();
		
		StateGraph<TokenizerNFAState> NFA = new StateGraph<TokenizerNFAState>();
		
		TokenizerNFAState start = new TokenizerNFAState();
		
		NFA.add(start);
		
		for (TokenDFA dfa : tokenDFAs.values()) {
			if (dfa.isInternal()) continue;
			
			StateGraph<TokenizerNFAState> copy = dfa.copyGraph(dfa.NFA);
			
			copy.lastElement().setAccepting(true);
			copy.lastElement().addOwner(dfa);
			
			start.addTransition(null, copy.firstElement());
			NFA.addAll(copy);
		}
		
		// null name indicates master DFA
		masterDFA = new TokenDFA(null, NFA);
		
	}
	
	public TokenDFA getTokenDFA(String name) {
		return tokenDFAs.get(name);
	}
	
	public Collection<TokenDFA> getAllTokenDFA() { return tokenDFAs.values(); }
	
	public Vector<String> getAllTokenNames() { return new Vector<String>(tokenDFAs.keySet()); }
	
	public TokenDFA getMasterTokenDFA() throws Exception {
		if (!finalized) {
			finalize();
			finalized = true;
		}
		
		return masterDFA;
	}
	
}
