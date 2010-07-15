
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
	public GrammarDefinition(Reader definitions) throws GrammarDefinitionException, TokenizerException {
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
	public GrammarRule getProduction(String rulename, String tokenname) { 
		
		if (!table.containsKey(rulename)) {
			throw new RuntimeException("Invalid rule name: " + rulename);
		}
		
		if (!table.get(rulename).containsKey(tokenname)) {
			throw new RuntimeException("Invalid token \"" + tokenname + "\" for rule \"" + rulename + "\"");
		}
		
		return table.get(rulename).get(tokenname); 
	}
	
	/**
	 * Add a production to the table at location (rulename, tokenname) 
	 */
	private void addToTable(String rulename, String tokenname, GrammarRule rule) {
		
		if (!table.containsKey(rulename)) {
			table.put(rulename, new HashMap<String, GrammarRule>());
		}
		
		if (table.get(rulename).containsKey(tokenname)) {
			throw new RuntimeException("Terminal \"" + tokenname + "\" already exists for rule \"" + rulename + "\"");
		}
		
		table.get(rulename).put(tokenname, rule);
	}
	
	/**
	 * Parse definitions into a set of rules 
	 */
	private void parse(GrammarTokenizer tokenizer) throws TokenizerException, GrammarDefinitionException {
		
		while ( true ) {
			
			String rulename;
			
			int lineNumber;
			
			Token tok;

			// Rule name
			try {
				tok = tokenizer.nextToken();
			}
			catch (NoSuchTokenException ex) {
				throw new GrammarDefinitionException("Rule does not begin with a valid ID", ex, ex.getLineNumber());
			}
			
			if (tok.type == GrammarTokenizer.EOF_TOKEN) {
				break; // we reached the end of the file, stop reading
			} 
			else if (tok.type == GrammarTokenizer.EOL_TOKEN) {
				continue; // empty line
			} 
			else if (tok.type != GrammarTokenizer.ID_TOKEN) {
				throw new GrammarDefinitionException("Rule does not begin with a valid ID", tok.line);
			}
			else {
				rulename = tok.value;
				lineNumber = tok.line;
			}
			
			
			// Separator ->
			try {
				tok = tokenizer.nextToken();
			}
			catch (NoSuchTokenException ex) {
				throw new GrammarDefinitionException("No such token for: " + ex.getValue(), ex, ex.getLineNumber());
			}
			
			if (tok.type != GrammarTokenizer.SEP_TOKEN) {
				throw new GrammarDefinitionException("Rule seperator -> not found after rule name", tok.line);
			}
			
			
			// Build the rules
			GrammarRuleBuilder rulebuilder;
			
			try {
				rulebuilder = new GrammarRuleBuilder(rulename, tokenizer, this, startRuleName == null);
			}
			catch (GrammarDefinitionException ex) {
				// re-throw with a line number
				throw new GrammarDefinitionException(ex.getMessage(), lineNumber);
			}
			
			// Add the rules returned by the rule builder to the set of all rules
			for (String name : rulebuilder.getRules().keySet()) {
				if (!rules.containsKey(name)) rules.put(name, new Vector<GrammarRule>());
				
				rules.get(name).addAll(rulebuilder.getRules(name));
			}
			
			// If startRuleName is null then this rule is the first defined, and therefore the start rule
			if (startRuleName == null) {
				startRuleName = rulename;
			}
			
		}
		
		// Iterate through each state of each rule, marking the state as being a token or rule state
		// by comparing the state's name with the names of defined rules
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
	private void buildParseTable() {
		
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
	public HashSet<String> first(String rulename) {
		HashSet<String> first = new HashSet<String>();
		
		for (GrammarRule r : rules.get(rulename)) {
			first.addAll(r.first());
		}
		
		return first;
	}
	
	/**
	 * Returns the set of terminals that can be found immediately following the given rule 
	 */
	public HashSet<String> follow(String rulename) {
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
