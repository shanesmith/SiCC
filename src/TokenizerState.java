
import java.util.ListIterator;
import java.util.Vector;

public abstract class TokenizerState {
	
	protected static int nextID  = 0;
	
	protected int id;
	
	public static final Character wildcard = 3;
	public static final Character neg = 4;
	
	/*
	 * Whether or not this state is accepting, defaulted to false
	 */
	protected boolean accepting = false;
	
	private Vector<TokenDFA> owners = new Vector<TokenDFA>();
	
	/*
	 * Simple constructor
	 */
	public TokenizerState() {
		id = nextID++;
	}
	
	
	public TokenizerState(TokenDFA owner) { 
		this();
		addOwner(owner);
	}
	
	public TokenizerState(Vector<TokenDFA> owners) {
		this();
		addOwners(owners);
	}
	
	public void addOwner(TokenDFA owner) { 
		// owner with null name indicates master DFA
		if (owner.name != null && !owner.isInternal() && !this.owners.contains(owner)) {
			ListIterator<TokenDFA> it = this.owners.listIterator();
			boolean inserted = false;
			
			while (it.hasNext()) {
				if (owner.getPosition() < it.next().getPosition()) {
					it.previous();
					it.add(owner);
					inserted = true;
					break;
				}
			}
			
			if (!inserted) {
				this.owners.add(owner);
			}
		}
	}
	
	public void addOwners(Vector<TokenDFA> owners) {
		for (TokenDFA o : owners) {
			addOwner(o);
		}
	}
	
	public Vector<TokenDFA> getOwners() { return owners; }
	
	/*
	 * Simple SET/GET below
	 */
	
	public void setAccepting(boolean accept) { accepting = accept; }
	
	public boolean isAccepting() { return accepting; }
	
	public int getID() { return id; }
	
	protected static void resetNextID() { nextID = 0; }
	
	public abstract boolean hasTransitions();

	
}
