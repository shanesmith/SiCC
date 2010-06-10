
class Token {
  public int line;
  public String name, value;
  public Token (String n, String v, int l) { name=n; value=v; line=l; }
  public String toString() { return "(" + line + ") " + name + " => " + value; }
  public boolean is (String str) { return str.equals(name); }
} // end Token
