public class TokenizerException extends Exception {
  private static final long serialVersionUID = 1L;
  private int lineNumber = -1;
  public TokenizerException (Throwable cause) { super(cause); }
  public TokenizerException (String msg) { super(msg); }
  public TokenizerException (String msg, int lineNumber) { super(msg); this.lineNumber = lineNumber; }
  public int getLineNumber() { return lineNumber; }
  public String toString() { return (lineNumber != -1 ? "[line " + lineNumber + "]" : "") + getMessage(); }
} // end TokenizerException
