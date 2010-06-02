
@SuppressWarnings("serial")
public class TokenizerException extends Exception {

	private TokenDFA tokendfa;
	
	public TokenizerException(String message) {
		super(message);
	}
	
	public TokenizerException(String message, TokenDFA tokendfa) {
		super(message);
		this.tokendfa = tokendfa; 
	}
	
	public TokenDFA getTokenDFA() {
		return tokendfa;
	}
	
	public String toString() {
		return (tokendfa!=null ? "[" + tokendfa.name + "] " : "") + getMessage(); 
	}

}
