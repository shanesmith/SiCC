
/**
 * Token class automatically created for GrammarTokenizer 
 */
class Token {	
  public int line=-1, type=0;
  public String name, value;
  public Token (int t, String n, String v, int l) { type=t; name=n; value=v; line=l; }
  public Token (String n, String v, int l) { name=n; value=v; line=l; }
  public Token (String n, String v) { name=n; value=v; }
  public Token (String n) { name=n; }
  public String toString() { return (line != -1 ? "(" + line + ") " : "") + name + (value != null ? " => " + value : ""); }
  public boolean is (String str) { return str.equals(name); }
} // end Token
