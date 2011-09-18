
import java.util.ListIterator;
import java.util.ArrayList;

/**
 * Base class for NFA and DFA state classes
 */
public abstract class TokenizerState {
	
	/**
	 * Each state has a unique id, keep track of next id to use 
	 */
	protected static int nextID  = 0;
	
	/**
	 * Special characters to be used in transitions
	 */
	public static final Character wildcard = 3;
	
	/**
	 * The state's id
	 */
	protected int id;
	
	/**
	 * Whether this state is accepting
	 */
	protected boolean accepting = false;
	
	
	private ArrayList<TokenDFA> owners = new ArrayList<TokenDFA>();
	
	/**
	 * Constructor.
	 */
	public TokenizerState() {
		id = nextID++;
	}
	
	/**
	 * Add the given TokenDFA as an owner of this state 
	 */
	public void addOwner(TokenDFA newowner) { 
		// do not accept owners that have null names (master dfa), internal owners, or if we already have the owner
		if (newowner.name != null && !newowner.isInternal() && !owners.contains(newowner)) {
			
			boolean inserted = false;
			
			ListIterator<TokenDFA> it = this.owners.listIterator();
			
			// iterate over owners and insert in order of position
			while (it.hasNext()) {
				if (newowner.getPosition() < it.next().getPosition()) {
					it.previous();
					it.add(newowner);
					inserted = true;
					break;
				}
			}
			
			// hasn't been inserted, so just put it at the end
			if (!inserted) {
				owners.add(newowner);
			}
			
		}
	}
	
	/**
	 * Add all the given owners to this state
	 */
	public void addOwners(ArrayList<TokenDFA> owners) {
		// use addOwner instead of owners.addAll because of special handling
		for (TokenDFA o : owners) {
			addOwner(o);
		}
	}
	
	public ArrayList<TokenDFA> getOwners() { return owners; }

	/**
	 * Reset the next id (ex: for new graph)
	 */
	protected static void resetNextID() { nextID = 0; }
	
	/**
	 * A bunch of getters and setters
	 */
	
	public void setAccepting(boolean accept) { accepting = accept; }
	
	public boolean isAccepting() { return accepting; }
	
	public int getID() { return id; }
	
}
