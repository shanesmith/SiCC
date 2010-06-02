
import java.io.Reader;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Vector;

import javax.naming.ldap.StartTlsRequest;


public class GrammarDefinition {

	private String startRuleName;
	
	private Hashtable<String, Vector<GrammarRule>> rules = new Hashtable<String, Vector<GrammarRule>>();
	
	private HashMap<String, HashMap<String, GrammarRule>> table = new HashMap<String, HashMap<String, GrammarRule>>();
	
	public GrammarDefinition(Reader definitions, Vector<String> tokenNames) throws Exception {
		parse(new GrammarTokenizer(definitions), tokenNames);
		
		for (Vector<GrammarRule> rulesvector : rules.values()) {
			
			for (GrammarRule rule : rulesvector) {
				
				for (String terminal : first(rule.getName())) {
					
					if (terminal == null) {
						
						for (String followterm : follow(rule.getName())) {
							addToTable(rule.getName(), followterm, rule);
						}
						
					} else {
						
						addToTable(rule.getName(), terminal, rule);
						
					}
					
				}
				
			}
			
		}
		
	}
	
	public Vector<GrammarRule> getStartRules() { return rules.get(startRuleName); }
	
	private void addToTable(String rulename, String terminal, GrammarRule rule) {
		if (!table.containsKey(rulename)) table.put(rulename, new HashMap<String, GrammarRule>());
		
		table.get(rulename).put(terminal, rule);
	}
	
	private void parse(GrammarTokenizer tokenizer, Vector<String> tokenNames) throws Exception {
		
		while ( true ) {
			
			Token next = tokenizer.nextToken();
			
			if (next == null) {
				break;
			} else if (next.is("eol")) {
				continue;
			} else {
				tokenizer.pushToken();
			}
			
			GrammarRuleBuilder rulebuilder = new GrammarRuleBuilder(tokenizer, tokenNames, this);
			
			rules.put(rulebuilder.getName(), rulebuilder.getRules());
			
			if (startRuleName == null) startRuleName = rulebuilder.getName();
			
		}
		
		return;
		
	}
	
	public Vector<GrammarRule> getRules(String name) { return rules.get(name); }
	public boolean hasRule(String name) { return rules.containsKey(name); }
	
	public HashSet<String> first(String rulename) throws Exception {
		HashSet<String> first = new HashSet<String>();
		
		for (GrammarRule r : rules.get(rulename)) {
			first.addAll(r.first());
		}
		
		return first;
	}
	
	public HashSet<String> follow(String rulename) throws Exception {
		HashSet<String> follow = new HashSet<String>();
		
		for (Vector<GrammarRule> v : rules.values()) {
			for (GrammarRule r : v) {
				follow.addAll(r.getFollow(rulename));
			}
		}
		
		if (follow.isEmpty()) follow.add(null);
		
		return follow;
	}
	
}
