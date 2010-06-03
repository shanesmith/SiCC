
public class GrammarRunner {

	private TokenizerRunner tokenizer;
	
	private GrammarDefinition grammardef;
	
	public GrammarRunner(GrammarDefinition grammardef, TokenizerRunner tokenizer) {
		this.grammardef = grammardef;
		this.tokenizer = tokenizer;
	}
	
	public void run() throws Exception {
		
		Token tok;
		
		//GrammarRule curRule = 
		
		while ( true ) {
			
			tok = tokenizer.nextToken();
			
			
			
		}
		
	}
	
}
