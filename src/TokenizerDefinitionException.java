
public class TokenizerDefinitionException extends Exception {

	private static final long serialVersionUID = 1L;
	
	private int lineNumber = -1;
	
	public TokenizerDefinitionException(String message) { 
		super(message); 
	}
	
	public TokenizerDefinitionException(String message, int lineNumber) {
		this(message);
		this.lineNumber = lineNumber;
	}
	
	public int getLineNumber() { return lineNumber; }
	
	public String toString() { return "TokenizerDefinitionException: " + (lineNumber != -1 ? "[line " + lineNumber + "] " : "") + getMessage(); }
	

}
