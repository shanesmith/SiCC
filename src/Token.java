
class Token {
  public int line;
  public String name, value;
  public Token (String n, String v, int l) { name=n; value=v; line=l; }
  public String toString() { return "(" + line + ") " + name + " => " + value; }
  public boolean is (String str) { return str.equals(name); }
} // end Token

/*public class Token {

	private TokenDFA DFA;
	
	private String value;
	
	public Token (TokenDFA aDFA, String val) {
		DFA = aDFA;
		value = val;
	}
	
	public TokenDFA getTokenDFA() {
		return DFA;
	}
	
	public String getName() { 
		return DFA.name;
	}
	
	public String getValue() {
		return value;
	}
	
	public String toString() {
		return DFA.name + " -> " + value;
	}
	
}*/
