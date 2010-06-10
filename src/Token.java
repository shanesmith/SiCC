
class Token {	
  public int line, type;
  public String name, value;
  public Token (int t, String n, String v, int l) { type=t; name=n; value=v; line=l; }
  public String toString() { return "(" + line + ") " + name + " => " + value; }
  public boolean is (String str) { return str.equals(name); }
} // end Token
