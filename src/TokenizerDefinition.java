import java.io.LineNumberReader;
import java.io.Reader;
import java.io.IOException;
import java.util.Collection;
import java.util.Hashtable;

/**
 * Creates a DFA meant for a tokenizer.
 * 
 * A token definition is written one per line in the following format:
 * 
 * 		[:] token_name : token_regexp
 * 
 * The first colon is optional and indicates that the token is internal.
 * Spaces and tabs are ignored.
 */
public class TokenizerDefinition {

	/**
	 * The collection of individual token DFAs, keyed by token name
	 */
	private Hashtable<String, TokenDFA> tokenDFAs = new Hashtable<String, TokenDFA>();
	
	/**
	 * The master DFA for this tokenizer (a joining of all individual DFAs as alternatives)
	 */
	private TokenDFA masterDFA;
	
	/**
	 * Constructor.
	 */
	public TokenizerDefinition(Reader definitions) throws TokenizerDefinitionException, IOException {
		createTokenDFAs(new LineNumberReader(definitions));
		constructMasterDFA();
	}
	
	/**
	 * Parse a Reader's input and builds TokenDFAs for each definition line
	 */
	private void createTokenDFAs(LineNumberReader definition) throws TokenizerDefinitionException, IOException {
		
		String line, name, regexp;
		
		boolean internal;
		
		while( (line=definition.readLine()) != null ) {
			
			int lineNumber = definition.getLineNumber();
			
			line = line.trim();
			
			// if there is a comment character and it isn't preceded by an escape character, remove the comment
			if (line.indexOf('#') != -1 && ( line.indexOf('#') == 0 || line.charAt(line.indexOf('#')-1) != '\\' )) {
				line = line.substring(0, line.indexOf('#'));
			}
			
			// either commented or empty line 
			if (line.isEmpty()) continue;
			
			internal = (line.startsWith(":"));
			
			// remove internal indicator if it exists for easier processing later
			if (internal) line = line.substring(1);
			
			// make sure we have another colon separating name and regexp
			if (line.indexOf(':') == -1) throw new TokenizerDefinitionException("Missing separator : after token name", lineNumber);
			
			name = line.substring(0, line.indexOf(':')).trim();
			
			if (name.isEmpty()) throw new TokenizerDefinitionException("Token name not defined", lineNumber);
			if (!name.matches("[a-zA-Z]\\w*")) throw new TokenizerDefinitionException("Invalid token name \"" + name + "\"", lineNumber);
			
			regexp = line.substring(line.indexOf(':')+1).trim();
			
			if (regexp.isEmpty()) throw new TokenizerDefinitionException("Regular expression not defined for token \"" + name + "\"", lineNumber);

			TokenDFA tok;

			// create the TokenDFA and catch any exception
			try {
				tok = new TokenDFA(name, regexp, internal, this);
			}
			catch (TokenizerDefinitionException ex) {
				throw new TokenizerDefinitionException(ex.getMessage(), lineNumber);
			}
			catch (Exception e) {
				throw new TokenizerDefinitionException(e.toString(), lineNumber);
			}
			
			// if the created TokenDFA's name already exists, add it to the existing one as an alternative
			if (tokenDFAs.containsKey(name)) {
				tokenDFAs.get(name).alternNFA(tok.NFA);
			} else {
				tokenDFAs.put(name, tok);
			}
			
		}
		
	}
	
	/**
	 * Join all TokenDFAs as alternatives to the master DFA
	 */
	private void constructMasterDFA() {
		
		// we are creating new states, start from scratch
		TokenizerState.resetNextID();
		
		// master NFA, which will become the master DFA
		StateGraph<TokenizerNFAState> NFA = new StateGraph<TokenizerNFAState>();
		
		// create a new start state
		TokenizerNFAState start = new TokenizerNFAState();
		
		NFA.add(start);
		
		// for each non-internal token DFA, take its NFA and add it
		// as an alternative to the master NFA
		for (TokenDFA dfa : tokenDFAs.values()) {
			if (dfa.isInternal()) continue;
			
			// make a copy, don't want to mess with the original
			StateGraph<TokenizerNFAState> copy = dfa.copyGraph(dfa.NFA);
			
			// the last element is accepting and owned by the current DFA
			copy.lastElement().setAccepting(true);
			copy.lastElement().addOwner(dfa);
			
			// add as alternative
			start.addTransition(null, copy.firstElement());
			NFA.addAll(copy);
		}
		
		// create master DFA from master NFA (null name indicates master)
		masterDFA = new TokenDFA(null, NFA);
		
	}
	
	/**
	 * Returns the token DFA by the given name.
	 */
	public TokenDFA getTokenDFA(String name) { return tokenDFAs.get(name); }
	
	/**
	 * Returns the list of all token DFAs
	 */
	public Collection<TokenDFA> getAllTokenDFA() { return tokenDFAs.values(); }

	/**
	 * Returns the master DFA
	 */
	public TokenDFA getMasterTokenDFA() { return masterDFA; }
	
}
