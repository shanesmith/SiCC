
/**
 * Exception thrown by ssCC's argument parser
 * when invalid arguments are provided.
 * 
 * @see ssCC(String[] args)
 */
public class ArgumentParsingException extends Exception {

	private static final long serialVersionUID = 1L;

	public ArgumentParsingException(String message) { super(message); }

	public ArgumentParsingException(Throwable cause) { super(cause); }

	public ArgumentParsingException(String message, Throwable cause) { super(message, cause); }

}
