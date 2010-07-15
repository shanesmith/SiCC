
public class GrammarDefinitionException extends Exception {

	private static final long serialVersionUID = 1L;

	private int lineNumber = -1;
	
	public GrammarDefinitionException(String message) {
		super(message);
	}
	
	public GrammarDefinitionException(String message, int lineNumber) {
		super(message);
		this.lineNumber = lineNumber;
	}
	
	public GrammarDefinitionException(String message, Throwable cause, int lineNumber) {
		super(message, cause);
		this.lineNumber = lineNumber;
	}
	
	public int getLineNumber() { return lineNumber; }
	
	public String toString() { return "GrammarDefinitionException: " + (lineNumber != -1 ? "[line " + lineNumber + "] " : "") + getMessage(); }
	
}
