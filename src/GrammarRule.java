
import java.util.*;

/**
 *  A GrammarRule represents a single rule from a given grammar.
 *  
 *  The rule may be a sub-rule, meaning a section of another rule.
 *  
 *  ex: 
 *  	The following rule
 *  
 *  		A -> B (C D)*
 *  
 *  	would be split into
 *  
 *  		A    -> B A{1}*
 *  		A{1} -> C D
 *
 */
public class GrammarRule {

	/**
	 * The rule's name
	 */
	private String name;
	
	/**
	 * Whether the rule has the [>1] multi-child flag
	 */
	private boolean multi_child = false;
	
	/**
	 * Whether the rule is a sub-rule
	 */
	private boolean subrule = false;
	
	/**
	 * The rule's graph
	 * 
	 * null denotes an epsilon rule
	 */
	private StateGraph<GrammarState> graph;
	
	/**
	 * A reference back to the grammar definition
	 */
	private GrammarDefinition grammardef;

	/**
	 * Constructor.
	 */
	public GrammarRule(String name, StateGraph<GrammarState> graph, boolean multi_child, GrammarDefinition grammardef) {
		this(name, graph, multi_child, grammardef, false);
	}

	/**
	 * Constructor.
	 */
	public GrammarRule(String name, StateGraph<GrammarState> graph, boolean multi_child, GrammarDefinition grammardef, boolean subrule) {
		this.name = name;
		this.graph = graph;
		this.multi_child = multi_child;
		this.grammardef = grammardef;
		this.subrule = subrule;
	}
	
	/**
	 * Returns the result of FOLLOW(rulename) on this rule,
	 *  in other words finds all terminals in this rule that can be found
	 *  directly after the given rulename
	 */
	public HashSet<String> getFollowOf(String rulename) throws GrammarDefinitionException {
		HashSet<String> follow = new HashSet<String>();
		
		// rule is epsilon, return empty set
		if (graph == null) {
			return follow;
		}
		
		// iterate over this rule's graph, looking for a rule state called rulename
		Iterator<GrammarState> it = graph.iterator();
		
		while(it.hasNext()) {
			
			GrammarState state = it.next(); 
			
			if (state.type == GrammarState.RULE && state.name.equals(rulename)) {
				
				Stack<GrammarState> process = new Stack<GrammarState>();
				
				// add the next state to the process stack
				// or push null to identify the end of the graph
				process.push(it.hasNext() ? it.next() : null);
				
				while (!process.isEmpty()) {
				
					GrammarState processState = process.pop();
					
					if (processState == null) { 
						if (!this.getName().equals(rulename)) {
							follow.addAll( grammardef.follow(this.getName()) );
						}
					}					
					else if (processState.type == GrammarState.TOKEN) {
						// next state is a token (terminal), simply add to the follow set
						follow.add(processState.name);
					}
					else if (processState.type == GrammarState.RULE) {
						// next state is a rule, add FIRST of the next state
						HashSet<String> nextFirst = grammardef.first(processState.name);
						
						// if first of next state contains a null (epsilon) terminal
						// then remove it and include FOLLOW of the next state
						if (nextFirst.contains(null)) {
							nextFirst.remove(null);
							follow.addAll(grammardef.follow(processState.name));
						}
						
						follow.addAll(nextFirst);
						
					}
					
				}
				
			}
		}
		
		return follow;
	}
	
	/**
	 * Return the results of FIRST on this rule 
	 */
	public Vector<String> first() throws GrammarDefinitionException {
		return _first(new HashSet<String>());
	}
	
	
	private Vector<String> _first(HashSet<String> path) throws GrammarDefinitionException {
		
		// check for left recursion
		if (path.contains(name)) {
			throw new GrammarDefinitionException("Left recursion detected for rule \"" + name + "\"");
		} 
		
		Vector<String> first = new Vector<String>();
		
		// if graph is null (epsilon)
		// add the epsilon state and return
		if (graph == null) {
			first.add(null);
			return first;
		}
		
		GrammarState firststate = graph.firstElement();
		
		if (firststate.type == GrammarState.RULE) {
			// state is a rule, at FIRST of that rule
			path.add(name);
			for (GrammarRule rule : grammardef.getRules(firststate.name)) {
				first.addAll( rule._first(path) );
			}
		}
		else if (firststate.type == GrammarState.TOKEN) {
			// state is a token (terminal), simply add to first
			first.add(firststate.name);
		}
		
		return first;
		
	}
	
	/**
	 * A bunch of getters and setters
	 */
	public String getName() { return name; }
	
	public boolean isMultiChild() { return multi_child; }
	
	public void setMultiChild(boolean multi) { multi_child = multi; } 
	
	public boolean isSubrule() { return subrule; }
	
	public boolean hasGraph() { return graph != null; }
	
	public StateGraph<GrammarState> getGraph() { return graph; }
	
	/**
	 * String representation of this rule
	 */
	public String toString() { return graph.toString(); } 
	
	
}
