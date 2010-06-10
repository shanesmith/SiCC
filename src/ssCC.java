
/*
 * TODO
 * - Token ID (int)
 * - Package and Project command line args
 * - Error report in definition and input files
 * - Example files
 */

import java.io.*;

public class ssCC {

	private String[] args;
	private String prefix = "";
	private File tokenFile, grammarFile;
	private TokenizerDefinition tokendef;
	private GrammarDefinition grammardef;
	
	public static void main(String[] args) {
		
		try {
			ssCC sscc = new ssCC(args);
			
			//sscc.runGrammar(new InputStreamReader(System.in));
			
			sscc.createClasses();
			
			println("DONE!");
		}
		catch (Exception e) {
			e.printStackTrace();
			die();
		}
		
	}
	
	private ssCC(String[] args) throws Exception {
		this.args = args;
		
		parseArgs();
	}
	
	private void parseArgs() throws Exception {
		if (args == null || args.length == 0) return;
		
		int i = 0; 
		
		if (args[i].equals("-p")) {
			if (args.length <= ++i) die("Missing prefix!");
			prefix = args[i++];
		}
		
		if (args.length <= i) die("Missing token definition file path!");
		tokenFile = new File(args[i++]);
		
		if (!tokenFile.isFile()) {
			die (tokenFile + " is not a valid file!");
		}
		
		tokendef = new TokenizerDefinition(new FileReader(tokenFile));
		
		if (args.length > i) {
			grammarFile = new File(args[i++]);
			
			if (!grammarFile.isFile()) {
				die(grammarFile + " is not a valid file!");
			}
			
			grammardef = new GrammarDefinition(new FileReader(grammarFile));
		}
		
	}
	
	public void runTokenizer(Reader reader) throws Exception {
		Token tok;
		
		TokenizerRunner runner = new TokenizerRunner(tokendef, reader);
		
		while ( (tok=runner.nextToken()) != null ) {
			println(tok);
		}
		
	}
	
	public void runGrammar(Reader reader) throws Exception {
		
		GrammarRunner runner = new GrammarRunner(grammardef, new TokenizerRunner(tokendef, reader));
		
		ASTNode parseTree = runner.run();
		
		println(parseTree);
		
	}
	
	public void createClasses() throws Exception {
		createTokenClass();
		
		createVisitorInterface();
		
		createASTNodeClasses();
		
		if (tokendef != null) createTokenizerClass();
		
		if (grammardef != null) createParserClass();
	}
	
	private void createTokenizerClass() throws Exception {
		PrintWriter out = getWriter(prefix + "Tokenizer.java");
		
		(new TokenizerClassCreator(prefix, tokendef)).output(out);
		
		out.close();
	}
	
	private void createParserClass() throws Exception {
		PrintWriter out = getWriter(prefix + "Parser.java");
		
		(new ParserClassCreator(prefix, grammardef)).output(out);
		
		out.close();
	}
	
	private void createTokenClass() throws Exception {
		String classname = prefix + "Token";
		
		PrintWriter out = getWriter(classname + ".java");
		
		out.println("class " + classname + " {");
		out.println("  public int line, type;");
		out.println("  public String name, value;");
		out.println("  public " + classname + " (int t, String n, String v, int l) { type=t; name=n; value=v; line=l; }");
		out.println("  public String toString() { return \"(\" + line + \") \" + name + \" => \" + value; }");
		out.println("  public boolean is (String str) { return str.equals(name); }");
		out.println("} // end " + classname);
		
		out.close();
	}
	
	private void createVisitorInterface() throws Exception {
		
		String interfacename = prefix + "Visitor";
		
		PrintWriter out = getWriter(interfacename + ".java");
		
		out.println("public interface " + interfacename + " {");
		
		out.println("  public Object visit(" + prefix + "ASTNode node, Object data);");
		
		for (String rulename : grammardef.getRuleNames()) {
			if (grammardef.getRules(rulename).get(0).isSubrule()) continue;
		
			String classname = prefix + "AST" + rulename + "Node";
			
			out.println("  public Object visit(" + classname + " node, Object data);");
			
		}
		
		out.println("} // end " + interfacename);
		
		out.close();
		
	}
	
	private void createASTNodeClasses() throws Exception {
		createASTNodeSuperClass();
		createASTNodeSubClasses();
	}
	
	private void createASTNodeSuperClass() throws Exception {
		String classname = prefix + "ASTNode";
		String visitorname = prefix + "Visitor";
		
		PrintWriter out = getWriter(classname + ".java");
		
		out.println("import java.util.Vector;");
		out.println();
		out.println("class " + classname + " {");
		out.println("  private ASTNode parent;");
		out.println("  private Vector<ASTNode> children = new Vector<ASTNode>();");
		out.println("  private String name, value;");
		out.println("  private boolean multi_child;");
		out.println("  public " + classname + " (String n, String v, boolean m, ASTNode p) { name=n; value=v; multi_child=m; parent=p; }");
		out.println("  public boolean isMultiChild() { return multi_child; }");
		out.println("  public void addChild(ASTNode node) { children.add(node); }");
		out.println("  public void removeChild(ASTNode node) { children.remove(node); }");
		out.println("  public Vector<ASTNode> getChildren() { return children; }");
		out.println("  public ASTNode getChild(int i) { return children.get(i); }");
		out.println("  public int numChildren() { return children.size(); }");
		out.println("  public String getName() { return name; }");
		out.println("  public String getValue() { return value; }");
		out.println("  public ASTNode getParent() { return parent; }");
		out.println("  public Object accept(" + visitorname + " visitor, Object data) { return visitor.visit(this, data); }");
		out.println("  public Object childrenAccept(" + visitorname + " visitor, Object data) { for(ASTNode node : children) node.accept(visitor, data); return data; }");
		out.println("  public String toString() { if (value == null || value.isEmpty()) { return name; } else { return name + \" => \" + value; } }");
		out.println("} // end " + classname);
		
		out.close();
	}
	
	private void createASTNodeSubClasses() throws Exception {
		
		String classname;
		
		String extendname = prefix + "ASTNode";
		
		PrintWriter out;
		
		for (String rulename : grammardef.getRuleNames()) {
			if (grammardef.getRules(rulename).get(0).isSubrule()) continue;
			
			classname = prefix + "AST" + rulename + "Node";
			
			out = getWriter(classname + ".java");
			
			out.println("class " + classname + " extends " + extendname + " {");
			out.println("  public " + classname + " (String n, String v, boolean m, ASTNode p) { super(n,v,m,p); }");
			out.println("} // end " + classname);
			
			out.close();
			
		}
		
		// ASTToken
		
		classname = prefix + "ASTToken";
		
		out = getWriter(classname + ".java");
		
		out.println("class " + classname + " extends " + extendname + " {");
		out.println("  public " + classname + " (String n, String v, ASTNode p) { super(n,v,false,p); }" );
		out.println("}");
		
		out.close();
		
	}
	
	private static PrintWriter getWriter(String filename) throws Exception {
		return new PrintWriter(new BufferedWriter(new FileWriter(filename)));
	}
	
	public static void die() { exit(1); }
	public static void end() { exit(0); }
	
	public static void die(Object msg) { exit(msg, 1); }
	public static void end(Object msg) { exit(msg, 0); }
	
	public static void exit(Object msg, int status) { println(msg); System.exit(status); }
	public static void exit(int status) { System.exit(status); }
	
	public static void println(int num) { while(num-- > 0) println(); }
	
	public static void println(Object o, int ln) { println(o.toString(), ln); }
	public static void println(String s, int ln) { System.out.println(s); println(ln); }
	
	public static void println() { println(""); }
	public static void println(Object o) { println(o.toString()); }
	public static void println(String s) { System.out.println(s); }
	
	
	public static void print(Object o) { print(o.toString()); }
	public static void print(String s) { System.out.print(s); }
	
}