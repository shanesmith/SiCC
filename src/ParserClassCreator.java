
import java.io.PrintWriter;

public class ParserClassCreator {

	String prefix = "";
	
	GrammarDefinition grammardef;
	
	public ParserClassCreator(GrammarDefinition grammardef) { 
		this.grammardef = grammardef; 
	}
	
	public ParserClassCreator(String prefix, GrammarDefinition grammardef) {
		this(grammardef);
		this.prefix = prefix;
	}
	
	public void Output(PrintWriter out) {
		
		String classname = prefix + "Parser";
		String tokenizername = prefix + "Tokenizer";
		
		out.println("class " + classname + " {");
		
		out.println("  private " + tokenizername + " tokenizer;");
		
		out.println("  public " + classname + " (" + tokenizername + " tokenizer) { this.tokenizer = tokenizer; }");
		
		
		out.println("} // end " + classname);
		
	}
	
}
