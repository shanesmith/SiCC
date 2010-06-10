
import java.util.*;

public class GrammarRule {

	private String name;
	
	private boolean multi_child = false;
	
	private boolean subrule = false;
	
	private GrammarDefinition grammardef;
	
	private StateGraph<GrammarState> graph;
	
	public String getName() { return name; }
	public boolean isMultiChild() { return multi_child; }
	public void setMultiChild(boolean multi) { multi_child = multi; } 
	public boolean isSubrule() { return subrule; }

	public GrammarRule(String name, StateGraph<GrammarState> graph, boolean multi_child, GrammarDefinition grammardef) {
		this(name, graph, multi_child, grammardef, false);
	}
	
	public GrammarRule(String name, StateGraph<GrammarState> graph, boolean multi_child, GrammarDefinition grammardef, boolean subrule) {
		this.name = name;
		this.graph = graph;
		this.multi_child = multi_child;
		this.grammardef = grammardef;
		this.subrule = subrule;
	}
	
	public void pushGraphToStack(Stack<GrammarState> stateStack) {
		stateStack.addAll(0, graph);
	}
	
	public boolean hasGraph() { return graph != null; }
	public StateGraph<GrammarState> getGraph() { return graph; }
	
	public HashSet<String> getFollowOf(String rulename) throws Exception {
		HashSet<String> follow = new HashSet<String>();
		
		if (graph == null) {
			return follow;
		}
		
		Iterator<GrammarState> it = graph.iterator();
		
		while(it.hasNext()) {
			
			GrammarState state = it.next(); 
			
			if (state.type == GrammarState.RULE && state.name.equals(rulename)) {
				
				Stack<GrammarState> process = new Stack<GrammarState>();
				
				GrammarState processState;
				
				process.push(it.hasNext() ? it.next() : null);
				
				while (!process.isEmpty()) {
				
					processState = process.pop();
					
					if (processState == null) { 
						if (!this.getName().equals(rulename)) {
							follow.addAll( grammardef.follow(this.getName()) );
						}
					}					
					else if (processState.type == GrammarState.TOKEN) {
						follow.add(processState.name);
					}
					else if (processState.type == GrammarState.RULE) {
						HashSet<String> nextFirst = grammardef.first(processState.name);
						
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
	
	public Vector<String> first() throws Exception {
		
		Vector<String> first = new Vector<String>();
		
		if (graph == null) {
			first.add(null);
			return first;
		}
		
		Stack<GrammarState> process = new Stack<GrammarState>();
		
		process.push(graph.firstElement());
		
		while (!process.isEmpty()) {
			
			GrammarState state = process.pop();
			
			if (state.type == GrammarState.RULE) {
				if (!grammardef.hasRule(state.name)) throw new Exception("Undefined rule: " + state.name);
				
				first.addAll( grammardef.first(state.name) );
			} 
			else if (state.type == GrammarState.TOKEN) {
				first.add(state.name);
			}
			
		}
		
		return first;
		
	}
	
	public String toString() { return " "+graph; } 
	
	
}
