
import java.util.Stack;


public class GrammarState {

	public static final int UNKNOWN = -1, EMPTY = 0, TOKEN = 1, RULE = 2;
	
	private int type;
	
	private String name;
	
	public GrammarState() {
		this.type = EMPTY;
	}
	
	public GrammarState(String name) {
		this.name = name;
		this.type = UNKNOWN;
	}
	
	public GrammarState(String name, int type) throws Exception {
		
		if (type < -1 || type > 2) {
			throw new Exception("Invalid state type: " + type);
		}
		
		this.name = name;
		this.type = type;
		
	}
	
	public int getType() { return type; }
	
	public String getTypeString() {
		switch (type) {
			case EMPTY: return "-";
			case TOKEN: return "T";
			case RULE: return "R";
			default:	
			case UNKNOWN: return "?";
		}
	}
	
	public String getName() { return name; }
	
	public String toString() { return name + "[" + getTypeString() + "]"; }
	
}
