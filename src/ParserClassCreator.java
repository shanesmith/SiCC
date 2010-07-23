
import java.io.PrintWriter;

//TODO Better Exception throwing/handling

/**
 * Output the Parser class based on the passed grammar definition
 */
public class ParserClassCreator {

	/**
	 * A prefix to prepend to all class names
	 */
	private String prefix = "";
	
	/**
	 * The grammar definition we base the parser on
	 */
	private GrammarDefinition grammardef;
	
	/**
	 * Constructor.
	 */
	public ParserClassCreator(String prefix, GrammarDefinition grammardef) {
		this.prefix = prefix;
		this.grammardef = grammardef; 
	}
	
	/**
	 * Output the parser class to the passed writer
	 */
	public void output(PrintWriter out) {
		
		String classname = prefix + "Parser";
		String tokenizername = prefix + "Tokenizer";
		String startRuleName = grammardef.getStartRuleName();
		
		out.println("import java.util.HashMap;");
		out.println("import java.util.Stack;");
		
		out.println("public class " + classname + " {");
		out.println();
		
		out.println("  private static final String startRuleName = \"" + startRuleName + "\";");
		out.println();
		out.println("  private " + tokenizername + " tokenizer;");
		out.println();
		out.println("  private HashMap<String, HashMap<String, GrammarRule>> table = new HashMap<String, HashMap<String, GrammarRule>>();");
		out.println();
		
		out.println("  public " + classname + " (" + tokenizername + " tokenizer) { this.tokenizer = tokenizer; buildTable(); }");
		out.println();
		
		outputParseFunction(out, startRuleName);
		out.println();
		
		outputMakeNodeFunction(out);
		out.println();
		
		outputBuildTableFunction(out);
		out.println();
		
		outputGrammarRuleClass(out);
		out.println();
		
		outputGrammarStateClass(out);
		out.println();
		
		out.println("} // end " + classname);
		
	}
	
	/**
	 * Parser's main parse function
	 */
	private void outputParseFunction(PrintWriter out, String startRuleName) {
		
		String tokenname = prefix + "Token";
		
		out.println("  public " + node(startRuleName) + " parse() throws ParserException, TokenizerException {");
		out.println("    " + tokenname + " curToken;");
		out.println();
		out.println("    GrammarState curState;");
		out.println();
		out.println("    Stack<GrammarState> stateStack = new Stack<GrammarState>();");
		out.println();
		out.println("    " + node(startRuleName) + " parseTree = null;");
		out.println("    ASTNode curNode = null;");
		out.println();
		out.println("    stateStack.push(new GrammarState(startRuleName, GrammarState.RULE));");
		out.println();
		out.println("    curToken = tokenizer.nextToken();");
		out.println();
		out.println("    while ( true ) {");
		out.println();
		out.println("      curState = stateStack.pop();");
		out.println();
		out.println("      if (curState == null) {");
		out.println();
		out.println("        ASTNode nextNode = curNode.getParent();");
		out.println();
		out.println("        if (curNode.isMultiChild() && curNode.numChildren() == 1) {");
		out.println("          ASTNode parentNode = curNode.getParent();");
		out.println("          parentNode.removeChild(curNode);");
		out.println("          parentNode.addChild( curNode.getChildren().firstElement() );");
		out.println("        }");
		out.println("        else if (curNode.numChildren() == 0) {");
		out.println("          curNode.getParent().removeChild(curNode);");
		out.println("        }");
		out.println();
		out.println("        curNode = nextNode;");
		out.println();
		out.println("      }"); 
		out.println("      else if (curState.type == GrammarState.TOKEN) {");
		out.println();
		out.println("        if (!curState.name.equals(curToken.name)) {");
		out.println("          throw new ParserException(\"Expected token \\\"\" + curState.name + \"\\\" but received \\\"\" + curToken.name + \"\\\"\");");
		out.println("        }");
		out.println();
		out.println("        if (curToken.name.equals(\"eof\")) break;");
		out.println();
		out.println("        curNode.addChild(new ASTToken(curToken.name, curToken.value));");
		out.println();		
		out.println("        curToken = tokenizer.nextToken();");
		out.println();
		out.println("      }");
		out.println("      else if (curState.type == GrammarState.RULE) {");
		out.println();
		out.println("        GrammarRule newrule = table.get(curState.name).get(curToken.name);");
		out.println();				
		out.println("        if (newrule == null) {");
		out.println("          throw new ParserException(\"Invalid token \\\"\" + curToken.name + \"\\\" for rule \\\"\" + curState.name + \"\\\"\");");
		out.println("        }");
		out.println();
		out.println("        if (!newrule.subrule) {");
		out.println("          if (parseTree == null) {");
		out.println("            curNode = parseTree = new " + node(startRuleName) + "(newrule.name, null, newrule.multi_child);");
		out.println("          } else {");
		out.println("            ASTNode newnode = makenode(newrule.name, null, newrule.multi_child);");
		out.println("            curNode.addChild(newnode);");
		out.println("            curNode = newnode;");
		out.println("          }");
		out.println();
		out.println("          stateStack.push(null);");
		out.println("        }");
		out.println();
		out.println("        for (int i = newrule.graph.length-1; i >= 0; i--) {");
		out.println("          stateStack.push(newrule.graph[i]);");
		out.println("        }");
		out.println("      }");
		out.println("      else if (curState.type == GrammarState.EPSILON) {");
		out.println("        continue; //do nothing");
		out.println("      }");
		out.println();				
		out.println("    }");
		out.println();
		out.println("    return parseTree;");
		out.println();
		out.println("  }");
		
	}
	
	/**
	 * Parser's makenode function, which return a node of the correct type based on rulename
	 */
	private void outputMakeNodeFunction(PrintWriter out) {
		
		out.println("  private ASTNode makenode(String rulename, String value, boolean multi_child) {");
		
		for (String rulename : grammardef.getRuleNames()) {
			if (grammardef.getRules(rulename).get(0).isSubrule()) continue;
		
			out.println("    if (rulename.equals(\"" + rulename + "\")) return new " + node(rulename) + "(rulename, value, multi_child);");
		}
		
		out.println("    throw new RuntimeException(\"Unknown rule name, cannot make node \\\"\" + rulename + \"\\\"\");");
		
		out.println("  }");
		
	}
	
	/**
	 * Parser's table creator, based on the grammar definition's table  
	 */
	private void outputBuildTableFunction(PrintWriter out) {
		
		out.println("  private void buildTable() {");
		
		out.println("    GrammarState[] graph;");
		
		for (String rulename : grammardef.getTable().keySet()) {
			
			out.println("    table.put(\"" + rulename + "\", new HashMap<String, GrammarRule>());");
			
			for (String tokname : grammardef.getTable().get(rulename).keySet()) {
				
				GrammarRule rule = grammardef.getTable().get(rulename).get(tokname);
				
				String multi = rule.isMultiChild() ? "true" : "false";
				String sub = rule.isSubrule() ? "true" : "false";
				
				int i = 0;
				out.println("      graph = new GrammarState[" + rule.getGraph().size() + "];");
				for (GrammarState state : rule.getGraph()) {
					out.println("      graph[" + (i++) + "] = new GrammarState(\"" + state.name + "\", " + state.type + ");");
				}
				
				out.println("      table.get(\"" + rulename + "\").put(\"" + tokname + "\", new GrammarRule(\"" + rule.getName() + "\", " + multi + ", " + sub + ", graph));");
				out.println();
				
			}
			
		}
		
		out.println("  }");
		
	}
	
	/**
	 * Parser's internal GrammarRule class used in the table
	 */
	private void outputGrammarRuleClass(PrintWriter out) {
		
		out.println("  private class GrammarRule {");
		out.println("    String name;");
		out.println("    boolean multi_child, subrule;");
		out.println("    GrammarState[] graph;");
		out.println();
		out.println("    public GrammarRule(String n, boolean m, boolean s, GrammarState[] g) {");
		out.println("      name = n; multi_child = m; subrule = s; graph = g;");
		out.println("    }");
		out.println("  } // end GrammarRule");
		
	}
	
	/**
	 * Parser's internal GrammarState class, used in GrammarRule
	 */
	private void outputGrammarStateClass(PrintWriter out) {
		
		out.println("  private class GrammarState {");
		out.println("    public static final int TOKEN = 1, RULE = 2, EPSILON = 3;");
		out.println("    String name;");
		out.println("    int type;");
		out.println("    public GrammarState(String n, int t) { name = n; type = t; }");
		out.println("  } // end GrammarState");
		
	}

	/**
	 * Returns a node class name based on the given name 
	 */
	private String node(String name) { return "AST" + name + "Node"; }
	
}
