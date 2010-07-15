
/**
 * Represents a simple grammar graph state
 */
public class GrammarState {

	/**
	 * Available types of states
	 */
	public static final int UNKNOWN = 0, TOKEN = 1, RULE = 2;
	
	/**
	 * The type of this state
	 */
	public int type;
	
	/**
	 * The name of this state
	 */
	public String name;
	
	/**
	 * Constructor.
	 */
	public GrammarState(String name, int type) {		
		this.name = name;
		this.type = type;
	}
	
	/**
	 * String representation of this state's type (for easier output)
	 */
	public String getTypeString() {
		switch (type) {
			case TOKEN: return "T";
			case RULE: return "R";
			case UNKNOWN: default: return "?";
		}
	}
	
	/**
	 * State's string representation
	 */
	public String toString() { return name + "[" + getTypeString() + "]"; }
	
}
