
import java.io.*;

/*

Tokenizer

Parser
	- consume tokens and create node tree
	
Validator (?)
	- Perform checks to make sure grammar is LL(1)

Interpreter
	- iterate through tree and output java classes

==========

OUTPUT

Tokenizer based on supplied tokens
	
Parser based on supplied grammar

Implementation of Visitor pattern on parse tree

*/

public class ssCC {

	private String[] args;
	private String prefix = "";
	private File tokenFile, grammarFile;
	private TokenizerDefinition tokendef;
	private GrammarDefinition grammardef;
	
	public static void main(String[] args) {
		
		try {
			ssCC sscc = new ssCC(args);
			
			sscc.runGrammar(new InputStreamReader(System.in));
			
			//sscc.createClasses();
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
			
			grammardef = new GrammarDefinition(new FileReader(grammarFile), tokendef.getAllTokenNames());
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
		createTokenizerClass();
	}
	
	private void createTokenizerClass() throws Exception {
		if (tokendef == null) die("TokenizerDefinition has not be initialized!");
		
		PrintWriter out = getWriter(prefix + "Tokenizer.java");
		
		(new TokenizerClassCreator(prefix, tokendef)).output(out);
		
		out.close();
	}
	
	private void createTokenClass() throws Exception {
		PrintWriter out = getWriter(prefix + "Token.java");
		
		out.println("class " + prefix + "Token {");
		out.println("  public int line;");
		out.println("  public String name, value;");
		out.println("  public " + prefix + "Token (String n, String v, int l) { name=n; value=v; line=l; }");
		out.println("  public String toString() { return \"(\" + line + \") \" + name + \" => \" + value; }");
		out.println("  public boolean is (String str) { return str.equals(name); }");
		out.println("} // end " + prefix + "Token");
		
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

/*tokenizer.defineToken("skip", "[\\s\\n\\r]");

tokenizer.defineToken("comment", "// [^\\r\\n]* (\\r|\\n|\\r\\n)");

tokenizer.defineToken("eol", ";");

tokenizer.defineToken("lparen", "\\(");
tokenizer.defineToken("rparen", "\\)");

tokenizer.defineToken("bb", "{");
tokenizer.defineToken("eb", "}");

tokenizer.defineToken("eq", "==");
tokenizer.defineToken("lt", "<");
tokenizer.defineToken("gt", ">");
tokenizer.defineToken("le", "<=");
tokenizer.defineToken("ge", ">=");
tokenizer.defineToken("gets", "=");

tokenizer.defineToken("op", "\\+ | - | / | \\*");

tokenizer.defineToken("do", "do");
tokenizer.defineToken("while", "while");
tokenizer.defineToken("if", "if");
tokenizer.defineToken("elsif", "elsif");
tokenizer.defineToken("else", "else");

tokenizer.defineInternalToken("integer", "[0123456789]+");
tokenizer.defineInternalToken("float", ":integer: (\\.:integer:)? | \\.:integer:");
tokenizer.defineToken("number", ":float: | :float: [eE] [-\\+]? :integer:?");

tokenizer.defineInternalToken("letter", "[ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz_]");
tokenizer.defineInternalToken("digit", "[0123456789]");
tokenizer.defineToken("id", ":letter: (:digit:|:letter:)*");*/
