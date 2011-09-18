
/**
 * Exception thrown by SiCC's argument parser
 * when invalid arguments are provided.
 */
public class ArgumentParsingException extends Exception {

	private static final long serialVersionUID = 1L;

	public ArgumentParsingException(String message) { super(message); }

	public ArgumentParsingException(Throwable cause) { super(cause); }

	public ArgumentParsingException(String message, Throwable cause) { super(message, cause); }

}
