
@SuppressWarnings("serial")
public class TokenizerDefinitionException extends Exception {

	int lineNumber;
	
	public TokenizerDefinitionException(String message, int lineNumber) {
		super(message);
		this.lineNumber = lineNumber;
	}
	
	public String toString() { return "(" + lineNumber + ") " + getMessage(); }

}
