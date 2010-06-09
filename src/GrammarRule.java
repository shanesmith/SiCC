
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
	
	public HashSet<String> getFollowOf(String rulename) throws Exception {
		HashSet<String> follow = new HashSet<String>();
		
		if (graph == null) {
			return follow;
		}
		
		Iterator<GrammarState> it = graph.iterator();
		
		while(it.hasNext()) {
			
			GrammarState state = it.next(); 
			
			if (state.getType() == GrammarState.RULE && state.getName().equals(rulename)) {
				
				Stack<GrammarState> process = new Stack<GrammarState>();
				
				GrammarState processState;
				
				process.push(it.next());
				
				while (!process.isEmpty()) {
				
					processState = process.pop();
					
					if (processState == null) { 
						if (!this.getName().equals(rulename)) {
							follow.addAll( grammardef.follow(this.getName()) );
						}
					}					
					else if (processState.getType() == GrammarState.TOKEN) {
						follow.add(processState.getName());
					}
					else if (processState.getType() == GrammarState.RULE) {
						HashSet<String> nextFirst = grammardef.first(processState.getName());
						
						if (nextFirst.contains(null)) {
							nextFirst.remove(null);
							follow.addAll(grammardef.follow(processState.getName()));
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
			
			if (state.getType() == GrammarState.RULE) {
				if (!grammardef.hasRule(state.getName())) throw new Exception("Undefined rule: " + state.getName());
				
				first.addAll( grammardef.first(state.getName()) );
			} 
			else if (state.getType() == GrammarState.TOKEN) {
				first.add(state.getName());
			}
			
		}
		
		return first;
		
	}
	
	public String toString() { return " "+graph; } 
	
	
}
