
import java.io.Reader;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Set;
import java.util.Vector;

/**
 * Creates a parse table based on a given set of rules. 
 */
public class GrammarDefinition {

	/**
	 * The name of the starting rule (ie: the first rule's name)
	 */
	private String startRuleName;
	
	/**
	 * The set of all rules, keyed by their name
	 */
	private Hashtable<String, Vector<GrammarRule>> rules = new Hashtable<String, Vector<GrammarRule>>();
	
	/**
	 * The parse table created from the given set of rules.
	 * Rows are rule names, while columns are token names. <CurrentRuleName, <TokenName, NextRule>>
	 */
	private HashMap<String, HashMap<String, GrammarRule>> table = new HashMap<String, HashMap<String, GrammarRule>>();
	
	/**
	 * Parse the given grammar definition input, creating rules, and build the table. 
	 */
	public GrammarDefinition(Reader definitions) throws Exception {
		parse(new GrammarTokenizer(definitions));
		
		buildParseTable();
	}
	
	/**
	 * A bunch of simple getters 
	 */
	public Set<String> getRuleNames() { return rules.keySet(); }
	public String getStartRuleName() { return startRuleName; }
	public HashMap<String, HashMap<String, GrammarRule>> getTable() { return table; }
	public Vector<GrammarRule> getRules(String name) { return rules.get(name); }
	public boolean hasRule(String name) { return rules.containsKey(name); }
	
	/**
	 * Returns the result of the table lookup of (rulename, tokenname)  
	 */
	public GrammarRule getProduction(String rulename, String tokenname) throws Exception { 
		
		if (!table.containsKey(rulename)) throw new Exception("Invalid rule name: " + rulename);
		
		if (!table.get(rulename).containsKey(tokenname)) throw new Exception("Invalid token \"" + tokenname + "\" for rule \"" + rulename + "\"");
		
		return table.get(rulename).get(tokenname); 
	}
	
	/**
	 * Add a production to the table at location (rulename, tokenname) 
	 */
	private void addToTable(String rulename, String tokenname, GrammarRule rule) throws Exception {
		if (!table.containsKey(rulename)) table.put(rulename, new HashMap<String, GrammarRule>());
		
		if (table.get(rulename).containsKey(tokenname)) throw new Exception("Terminal \"" + tokenname + "\" already exists for rule \"" + rulename + "\"");
		
		table.get(rulename).put(tokenname, rule);
	}
	
	/**
	 * Parse definitions into a set of rules 
	 */
	private void parse(GrammarTokenizer tokenizer) throws Exception {
		
		// iterate through rule definitions and fill up the set
		while ( true ) {
			
			Token firsttok = tokenizer.nextToken();
			
			if (firsttok.type == GrammarTokenizer.EOF_TOKEN) {
				// we reached the end of the file, stop reading
				break;
			} else if (firsttok.type == GrammarTokenizer.EOL_TOKEN) {
				// empty line
				continue;
			} else if (firsttok.type != GrammarTokenizer.ID_TOKEN) {
				// if not EOF or EOL, the first token must be an ID which defines the name of the rule
				throw new Exception("Rule must start with a valid ID");
			}
			
			// the token following the name must be the separator
			if (tokenizer.nextToken().type != GrammarTokenizer.SEP_TOKEN) throw new Exception("Rule seperator -> not found after rule name");
			
			// build the rules
			GrammarRuleBuilder rulebuilder = new GrammarRuleBuilder(firsttok.value, tokenizer, this, startRuleName == null);
			
			// add the rules returned by the rule builder to the set of all rules
			for (String rulename : rulebuilder.getRules().keySet()) {
				if (!rules.containsKey(rulename)) rules.put(rulename, new Vector<GrammarRule>());
				
				rules.get(rulename).addAll(rulebuilder.getRules(rulename));
			}
			
			if (startRuleName == null) {
				// if startRuleName is null then this rule is the first defined, and therefore the start rule
				startRuleName = rulebuilder.getName();
			}
			
		}
		
		// iterate through each state of each rule, marking the state as being a rule or token state
		// by checking whether the name of the state has been defined as a rule
		for (Vector<GrammarRule> altrules : rules.values()) {
			
			for (GrammarRule rule : altrules) {
				
				if (!rule.hasGraph()) continue; 
					
				for (GrammarState state : rule.getGraph()) {
					
					if (state.type != GrammarState.UNKNOWN) continue;
						
					state.type = rules.containsKey(state.name) ? GrammarState.RULE : GrammarState.TOKEN;
					
				}
					
			}
			
		}
		
	}
	
	/**
	 * Build the parse table by using FIRST and FOLLOW
	 */
	private void buildParseTable() throws Exception {
		
		for (Vector<GrammarRule> altrules : rules.values()) {
			
			for (GrammarRule rule : altrules) {
				
				for (String terminal : rule.first()) {
					
					if (terminal == null) {
						// FIRST of rule includes epsilon, therefore include FOLLOW of rule
						
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
	
	/**
	 * Return the set of terminals that can be found as the first terminal of the given rule 
	 */
	public HashSet<String> first(String rulename) throws Exception {
		HashSet<String> first = new HashSet<String>();
		
		for (GrammarRule r : rules.get(rulename)) {
			first.addAll(r.first());
		}
		
		return first;
	}
	
	/**
	 * Returns the set of terminals that can be found immediately following the given rule 
	 */
	public HashSet<String> follow(String rulename) throws Exception {
		HashSet<String> follow = new HashSet<String>();
		
		for (Vector<GrammarRule> v : rules.values()) {
			for (GrammarRule r : v) {
				follow.addAll(r.getFollowOf(rulename));
			}
		}
		
		if (follow.isEmpty()) follow.add(null);
		
		return follow;
	}
	
}
