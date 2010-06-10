
public class GrammarState {

	public static final int UNKNOWN = 0, TOKEN = 1, RULE = 2;
	
	public int type;
	
	public String name;
	
	public GrammarState(String name, int type) throws Exception {		
		this.name = name;
		this.type = type;
	}
	
	public String getTypeString() {
		switch (type) {
			case TOKEN: return "T";
			case RULE: return "R";
			case UNKNOWN: default: return "?";
		}
	}
	
	public String toString() { return name + "[" + getTypeString() + "]"; }
	
}
