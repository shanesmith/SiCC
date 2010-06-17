/**
 *  ssCC is invoked with the following options
 * 
 *  	ssCC [--package packagename] [--prefix prefixname] <definitions>
 *  
 *  where
 *  
 *  	<definitions> = token_definition_file  grammar_definition_file
 *  				  | --tokenizer-only token_definition_file
 *  				  | --parser-only grammar_definition_file
 */

/*
 * TODO
 * - Error report in definition and input files
 * - Testing
 * - Example files
 */

import java.io.*;

public class ssCC {

	/**
	 * Constants used for the "only" variable
	 */
	private static final int TOKENIZER_ONLY = 1;
	private static final int PARSER_ONLY = 2;
	
	/**
	 * Holds the arguments
	 */
	private String[] args;
	
	/**
	 * Whether or not a --XXXX-ONLY option was set, see constants for values
	 */
	private int only = 0;
	
	/**
	 *  The prefix to be added to the classes
	 */
	private String prefix = "";
	
	/**
	 *  The package name in which all created classes belong
	 */
	private String packagename;
	
	/**
	 *  File objects created by file names passed by the arguments
	 */
	private File tokenFile, grammarFile;
	
	/**
	 *  Definition objects 
	 */
	private TokenizerDefinition tokendef;
	private GrammarDefinition grammardef;
	
	/**
	 * Main. Create a ssCC instance (passing in the arguments) and invoke its createClasses() method.
	 * Exceptions are also taken care of.
	 */
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
	
	/**
	 * Constructor. Parse given arguments and create token and/or grammar
	 * definition objects according to arguments.  
	 */
	private ssCC(String[] args) throws Exception {
		this.args = args;
		
		// parse the arguments
		parseArgs();
		
		// create a token definition object if not "parser only" and if the definition file is valid
		if (only != PARSER_ONLY) {
			if (tokenFile == null) throw new ArgumentParsingException("Missing token definition file!");
			if (!tokenFile.isFile()) throw new ArgumentParsingException (tokenFile + " is not a valid file!");
			tokendef = new TokenizerDefinition(new FileReader(tokenFile));
		}

		// create a grammar definition object if not "tokenizer only" and if the definition file is valid		
		if (only != TOKENIZER_ONLY) {
			if (grammarFile == null) throw new ArgumentParsingException("Missing grammar definition file!");
			if (!grammarFile.isFile()) throw new ArgumentParsingException (grammarFile + " is not a valid file!");
			grammardef = new GrammarDefinition(new FileReader(grammarFile));
		}
		
	}
	
	/**
	 * Parse the arguments and set appropriate options/variables
	 */
	private void parseArgs() throws Exception {		
		
		for(int i = 0; i < args.length; i++) {
			
			if (args[i].startsWith("-")) { // argument is an option
				
				// definition files have been specified, cannot set more options, throw exception
				if (tokenFile != null || grammarFile != null) throw new ArgumentParsingException("Cannot place options after file definitions.");
				
				// switch through possible arguments
				if (args[i].equals("--tokenizer-only")) {
					// set only to TOKENIZER_ONLY if it hasn't yet been defined (else throw exception)
					if (only != 0) throw new ArgumentParsingException("\"only\" already defined!");
					only = TOKENIZER_ONLY;
				}
				else if (args[i].equals("--parser-only")) {
					// set only to PARSER_ONLY if it hasn't yet been defined (else throw exception)
					if (only != 0) throw new ArgumentParsingException("\"only\" already defined!");
					only = PARSER_ONLY;
				}
				else if (args[i].equals("--package")) {
					// set package name to the next argument and check validity
					if (i == args.length-1) throw new ArgumentParsingException("Package name not defined");
					packagename = args[++i];
					if (!packagename.matches("[a-zA-Z]\\w*")) throw new ArgumentParsingException("Invalid package name: " + packagename);
				}
				else if (args[i].equals("--prefix")) {
					// set prefix to the next argument and check validity
					if (i == args.length-1) throw new ArgumentParsingException("Prefix not defined");
					prefix = args[++i];
					if (!prefix.matches("[a-zA-Z]\\w*")) throw new ArgumentParsingException("Invalid prefix: " + packagename);
				}
				else {
					// argument is not a valid option
					throw new ArgumentParsingException("Unknown option: " + args[i]);
				}
				
			} else { // argument is a definition file
				
				switch(only) {
				case TOKENIZER_ONLY:
					// set token file if not already set (else throw exception)
					if (tokenFile != null) throw new ArgumentParsingException("Token file has already been set.");
					tokenFile = new File(args[i]);
					break;
				case PARSER_ONLY:
					// set grammar file if not already set (else throw exception)
					if (grammarFile != null) throw new ArgumentParsingException("Grammar file has already been set.");
					grammarFile = new File(args[i]);
					break;
				default:
					// token and grammar files defined one after the other.
					// set token file if not already defined, else set grammar file if not already defined, else throw exception
					if (tokenFile == null) tokenFile = new File(args[i]);
					else if (grammarFile == null) grammarFile = new File(args[i]);
					else throw new ArgumentParsingException("Token and Grammar files have already been set.");
				}
				
			}
			
		}		
		
	}
	
	/**
	 * Creates and outputs the classes 
	 */
	public void createClasses() throws Exception {
		// needed always
		createTokenClass();
		
		// create Tokenizer if token definition is set
		if (tokendef != null) {
			createTokenizerClass();
		}
		
		// create Parser (and others) if grammar definition is set
		if (grammardef != null) {
			createParserClass();
			
			createVisitorInterface();
			
			createASTNodeClasses();
		}
	}
	
	/**
	 *  Create and save Tokenizer class
	 */
	private void createTokenizerClass() throws Exception {
		PrintWriter out = getWriter(prefix + "Tokenizer.java");
		
		(new TokenizerClassCreator(prefix, tokendef)).output(out);
		
		out.close();
	}
	
	/**
	 * Create and save Parse class
	 */
	private void createParserClass() throws Exception {
		PrintWriter out = getWriter(prefix + "Parser.java");
		
		(new ParserClassCreator(prefix, grammardef)).output(out);
		
		out.close();
	}
	
	/**
	 * Create and save Token class
	 */
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
	
	/**
	 * Create and save Visitor interface.
	 * 
	 * A method is defined the following way for each node type:
	 * 
	 * 		public Object visit(ASTXXXXNode node, Object data);
	 *  
	 */
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
	
	/**
	 * Create and save ASTNode classes (both super and sub classes) 
	 */
	private void createASTNodeClasses() throws Exception {
		createASTNodeSuperClass();
		createASTNodeSubClasses();
	}
	
	/**
	 * Create and save ASTNode superclass
	 */
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

	/**
	 * Create and save ASTXXXXNode subclasses for each type
	 */
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
	
	
	/**
	 * Returns a PrintWriter for the given file name (with package considerations)
	 */
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
	
	/**
	 * Test run the token definition on a reader object.
	 * TokenizerRunner is a modified implementation of the Tokenizer class outputed by ssCC.
	 */
	public void runTokenizer(Reader reader) throws Exception {
		Token tok;
		
		TokenizerRunner runner = new TokenizerRunner(tokendef, reader);
		
		while ( (tok=runner.nextToken()) != null ) {
			System.out.println(tok);
		}
		
	}
	
	/**
	 * Test run the grammar definition on a reader object (which gets fed into a TokenizerRunner).
	 * GrammarRunner is a modified implementation of the Parse class outputed by ssCC. 
	 */
	public void runGrammar(Reader reader) throws Exception {
		
		GrammarRunner runner = new GrammarRunner(grammardef, new TokenizerRunner(tokendef, reader));
		
		ASTNode parseTree = runner.run();
		
		System.out.println(parseTree);
	}
	
	/**
	 * Exception used when parsing the arguments for ssCC
	 */
	@SuppressWarnings("serial")
	private class ArgumentParsingException extends Exception {
		public ArgumentParsingException(String message) { super(message); }
	}
	
}