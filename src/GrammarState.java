
import java.util.Vector;

public class GrammarState {

	public static final int UNKNOWN_TYPE = -1, EMPTY = 0, TOKEN_TYPE = 1, RULE_TYPE = 2;
	
	private int type;
	
	private String name;
	
	private Vector<GrammarState> next = new Vector<GrammarState>();
	
	public GrammarState() {
		this.type = EMPTY;
	}
	
	public GrammarState(String name) {
		this.name = name;
		this.type = UNKNOWN_TYPE;
	}
	
	public GrammarState(String name, int type) throws Exception {
		
		if (type < -1 || type > 2) {
			throw new Exception("Invalid state type: " + type);
		}
		
		this.name = name;
		this.type = type;
		
	}
	
	public void addNext(GrammarState state) { next.add(state); }
	public Vector<GrammarState> getNext() { return next; }
	public boolean hasNext() { return !next.isEmpty(); }
	
	public int getType() { return type; }
	public String getName() { return name; }
	
}
