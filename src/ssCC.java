
/*
 * TODO
 * - Error report in definition and input files
 * - Testing
 * - Example files
 */

import java.io.*;

public class ssCC {

	private static final int TOKENIZER_ONLY = 1;
	private static final int PARSER_ONLY = 2;
	
	private String[] args;
	private int only = 0;
	private String prefix = "";
	private String packagename;
	private File tokenFile, grammarFile;
	private TokenizerDefinition tokendef;
	private GrammarDefinition grammardef;
	
	public static void main(String[] args) {
		
		try {
			ssCC sscc = new ssCC(args);
			
			sscc.createClasses();
			
			System.out.println("DONE!");
		}
		catch (TokenizerDefinitionException e) {
			System.out.println(e);
		}
		catch (ArgumentParsingException e) {
			System.out.println(e);
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		
	}
	
	private ssCC(String[] args) throws Exception {
		this.args = args;
		
		// parse the passed arguments
		parseArgs();
		
		if (only != PARSER_ONLY) {
			if (tokenFile == null) throw new ArgumentParsingException("Missing token definition file!");
			if (!tokenFile.isFile()) throw new ArgumentParsingException (tokenFile + " is not a valid file!");
			tokendef = new TokenizerDefinition(new FileReader(tokenFile));
		}
		
		if (only != TOKENIZER_ONLY) {
			if (grammarFile == null) throw new ArgumentParsingException("Missing grammar definition file!");
			if (!grammarFile.isFile()) throw new ArgumentParsingException (grammarFile + " is not a valid file!");
			grammardef = new GrammarDefinition(new FileReader(grammarFile));
		}
		
	}
	
	private void parseArgs() throws Exception {		
		
		for(int i = 0; i < args.length; i++) {
			
			if (args[i].startsWith("-")) {
				
				if (tokenFile != null || grammarFile != null) throw new ArgumentParsingException("Cannot place options after file definitions.");
				
				if (args[i].equals("--tokenizer-only")) {
					if (only != 0) throw new ArgumentParsingException("\"only\" already defined!");
					only = TOKENIZER_ONLY;
				}
				else if (args[i].equals("--parser-only")) {
					if (only != 0) throw new ArgumentParsingException("\"only\" already defined!");
					only = PARSER_ONLY;
				}
				else if (args[i].equals("--package")) {
					if (i == args.length-1) throw new ArgumentParsingException("Package name not defined");
					packagename = args[++i];
					if (!packagename.matches("[a-zA-Z]\\w*")) throw new ArgumentParsingException("Invalid package name: " + packagename);
				}
				else if (args[i].equals("--prefix")) {
					if (i == args.length-1) throw new ArgumentParsingException("Prefix not defined");
					prefix = args[++i];
					if (!prefix.matches("[a-zA-Z]\\w*")) throw new ArgumentParsingException("Invalid prefix: " + packagename);
				}
				else {
					throw new ArgumentParsingException("Unknown option: " + args[i]);
				}
				
			} else {
				
				switch(only) {
				case TOKENIZER_ONLY:
					if (tokenFile != null) throw new ArgumentParsingException("Token file has already been set.");
					tokenFile = new File(args[i]);
					break;
				case PARSER_ONLY:
					if (grammarFile != null) throw new ArgumentParsingException("Grammar file has already been set.");
					grammarFile = new File(args[i]);
					break;
				default:
					if (tokenFile == null) tokenFile = new File(args[i]);
					else if (grammarFile == null) grammarFile = new File(args[i]);
					else throw new ArgumentParsingException("Token and Grammar files have already been set.");
				}
				
			}
			
		}		
		
	}
	
	public void runTokenizer(Reader reader) throws Exception {
		Token tok;
		
		TokenizerRunner runner = new TokenizerRunner(tokendef, reader);
		
		while ( (tok=runner.nextToken()) != null ) {
			System.out.println(tok);
		}
		
	}
	
	public void runGrammar(Reader reader) throws Exception {
		
		GrammarRunner runner = new GrammarRunner(grammardef, new TokenizerRunner(tokendef, reader));
		
		ASTNode parseTree = runner.run();
		
		System.out.println(parseTree);
		
	}
	
	public void createClasses() throws Exception {
		createTokenClass();
		
		if (tokendef != null) {
			createTokenizerClass();
		}
		
		if (grammardef != null) {
			createParserClass();
			
			createVisitorInterface();
			
			createASTNodeClasses();
		}
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
		
		out.println("public class " + classname + " {");
		out.println("  public int line=-1, type=0;");
		out.println("  public String name, value;");
		out.println("  public " + classname + " (int t, String n, String v, int l) { type=t; name=n; value=v; line=l; }");
		out.println("  public " + classname + " (String n, String v, int l) { name=n; value=v; line=l; }");
		out.println("  public " + classname + " (String n, String v) { name=n; value=v; }");
		out.println("  public " + classname + " (String n) { name=n; }");
		out.println("  public String toString() { return (line != -1 ? \"(\" + line + \") \" : \"\") + name + (value != null ? \" => \" + value : \"\"); }");
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
		out.println("public class " + classname + " {");
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
			
			out.println("public class " + classname + " extends " + extendname + " {");
			out.println("  public " + classname + " (String n, String v, boolean m, ASTNode p) { super(n,v,m,p); }");
			out.println("} // end " + classname);
			
			out.close();
			
		}
		
		// ASTToken
		
		classname = prefix + "ASTToken";
		
		out = getWriter(classname + ".java");
		
		out.println("public class " + classname + " extends " + extendname + " {");
		out.println("  public " + classname + " (String n, String v, ASTNode p) { super(n,v,false,p); }" );
		out.println("}");
		
		out.close();
		
	}
	
	private PrintWriter getWriter(String filename) throws Exception {
		if (packagename != null && !packagename.isEmpty()) {
			new File(packagename).mkdir();
			
			filename = packagename + "/" + filename;
		}
		
		PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter(filename)));
		
		if (packagename != null && !packagename.isEmpty()) {
			out.println("package " + packagename + ";");
			out.println();
		}
		
		return out;
	}
	
	@SuppressWarnings("serial")
	private class ArgumentParsingException extends Exception {
		public ArgumentParsingException(String message) { super(message); }
	}
	
}