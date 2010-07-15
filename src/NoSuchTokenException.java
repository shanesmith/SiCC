
public class NoSuchTokenException extends TokenizerException {
  private static final long serialVersionUID = 1L;
  private String value;
  public NoSuchTokenException(String value, int lineNumber) { super("No such token for: " + value, lineNumber); this.value = value; }
  public String getValue() { return value; }
}
