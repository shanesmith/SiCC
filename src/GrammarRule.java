
import java.util.*;

public class GrammarRule {

	private String name;
	
	private boolean multi_child = false;
	
	private GrammarDefinition grammardef;
	
	private StateGraph<GrammarState> graph;
	
	public String getName() { return name; }
	public boolean isMultiChild() { return multi_child; }
	
	/*
	public GrammarRule(GrammarTokenizer tokenizer, Vector<String> tokenNames, GrammarDefinition grammardef) throws Exception {
		this.grammardef = grammardef;
		parse(tokenizer, tokenNames);
	}
	*/

	public GrammarRule(String name, StateGraph<GrammarState> graph, boolean multi_child, GrammarDefinition grammardef) {
		this.name = name;
		this.graph = graph;
		this.multi_child = multi_child;
		this.grammardef = grammardef;
	}
	
	public HashSet<String> getFollow(String rulename) throws Exception {
		HashSet<String> follow = new HashSet<String>();
		
		for (GrammarState state : graph) {
			if (state.getType() == GrammarState.RULE_TYPE && state.getName().equals(rulename)) {
				
				Stack<GrammarState> process = new Stack<GrammarState>();
				
				GrammarState processState;
				
				process.addAll(state.getNext());
				
				while (!process.isEmpty()) {
				
					processState = process.pop();
					
					if (processState.getType() == GrammarState.EMPTY) {
						if (processState.hasNext()) {						
							process.addAll(processState.getNext());
						} else {
							follow.addAll( grammardef.follow(this.getName()) );
						}
					}
					else if (processState.getType() == GrammarState.TOKEN_TYPE) {
						follow.add(processState.getName());
					}
					else if (processState.getType() == GrammarState.RULE_TYPE) {
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
		
		Stack<GrammarState> process = new Stack<GrammarState>();
		
		process.push(graph.firstElement());
		
		while (!process.isEmpty()) {
			
			GrammarState state = process.pop();
			
			if (state.getType() == GrammarState.EMPTY) {
				if (state.hasNext()) {
					process.addAll(state.getNext());
				} else {
					first.add(null);
				}
			}
			else if (state.getType() == GrammarState.RULE_TYPE) {
				if (!grammardef.hasRule(state.getName())) throw new Exception("Undefined rule: " + state.getName());
				
				first.addAll( grammardef.first(state.getName()) );
			} 
			else if (state.getType() == GrammarState.TOKEN_TYPE) {
				first.add(state.getName());
			}
			
		}
		
		return first;
		
	}
	
	
}
