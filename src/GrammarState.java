
public class GrammarState {

	public static final int TOKEN = 1, RULE = 2;
	
	private int type;
	
	private String name;
	
	public GrammarState(String name, int type) throws Exception {
		
		if (type < 1 || type > 2) throw new Exception("Invalid state type: " + type);
		
		this.name = name;
		this.type = type;
		
	}
	
	public int getType() { return type; }
	
	public String getTypeString() {
		switch (type) {
			case TOKEN: return "T";
			case RULE: return "R";
			default: return "?";
		}
	}
	
	public String getName() { return name; }
	
	public String toString() { return name + "[" + getTypeString() + "]"; }
	
}
