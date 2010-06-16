import java.io.LineNumberReader;
import java.io.Reader;
import java.util.Collection;
import java.util.Hashtable;


public class TokenizerDefinition {

	private Hashtable<String, TokenDFA> tokenDFAs = new Hashtable<String, TokenDFA>();
	
	private TokenDFA masterDFA;
	
	public TokenizerDefinition(Reader definitions) throws Exception {
		parse(new LineNumberReader(definitions));
		constructMasterDFA();
	}
	
	private void parse(LineNumberReader definition) throws Exception {
		
		String line, name, regexp;
		
		boolean internal;
		
		while( (line=definition.readLine()) != null ) {
			
			int lineNumber = definition.getLineNumber();
			
			line = line.trim();
			
			if (line.isEmpty() || line.startsWith("#")) continue;
			
			internal = (line.startsWith(":"));
			
			if (internal) line = line.substring(1);
			
			if (line.indexOf(':') == -1) throw new TokenizerDefinitionException("Invalid token definition.", lineNumber);
			
			name = line.substring(0, line.indexOf(':'));
			
			if (name.isEmpty()) throw new TokenizerDefinitionException("Token name not defined.", lineNumber);
			if (!name.matches("\\w+")) throw new TokenizerDefinitionException("Invalid token name \"" + name + "\"", lineNumber);
			
			regexp = line.substring(line.indexOf(':')+1);
			
			if (regexp.isEmpty()) throw new TokenizerDefinitionException("Regular expression not defined for token \"" + name + "\"", lineNumber);
			
			TokenDFA tok = new TokenDFA(name, regexp, this, internal);
			
			if (tokenDFAs.containsKey(name)) {
				tokenDFAs.get(name).alternNFA(tok.NFA);
			} else {
				tokenDFAs.put(name, tok);
			}
			
		}
		
	}
	
	private void constructMasterDFA() throws Exception {
		
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
	
	public TokenDFA getTokenDFA(String name) { return tokenDFAs.get(name); }
	
	public Collection<TokenDFA> getAllTokenDFA() { return tokenDFAs.values(); }
	
	public TokenDFA getMasterTokenDFA() throws Exception { return masterDFA; }
	
}
