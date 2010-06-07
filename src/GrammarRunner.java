
import java.util.Stack;

public class GrammarRunner {

	private TokenizerRunner tokenizer;
	
	private GrammarDefinition grammardef;
	
	public GrammarRunner(GrammarDefinition grammardef, TokenizerRunner tokenizer) {
		this.grammardef = grammardef;
		this.tokenizer = tokenizer;
	}
	
	public void run() throws Exception {
		
		Token curToken;
		
		GrammarState curState;
		
		Stack<GrammarState> stateStack = new Stack<GrammarState>();
		
		Stack<GrammarRule> ruleStack = new Stack<GrammarRule>();
		
		
		curToken = tokenizer.nextToken();
		
		//ruleStack.push(grammardef.getSta)
		
		stateStack.push(new GrammarState(grammardef.getStartRuleName(), GrammarState.RULE));
		
		while ( true ) {
			
			curState = stateStack.pop();
			
			if (curState.getType() == GrammarState.TOKEN) {
				
				if (!curState.getName().equals(curToken.name)) {
					throw new Exception("Tokens do not match! read: \"" + curToken.name + "\"  stack: \"" + curState.getName() + "\"");
				}
				

				System.out.println(curToken);
				
				curToken = tokenizer.nextToken();
				
			}
			else if (curState.getType() == GrammarState.RULE) {
				
				GrammarRule newrule = grammardef.getProduction(curState.getName(), curToken.name);
				
				if (newrule != null && newrule.hasGraph()) {
					newrule.pushToStack(stateStack);
				}
				
			}
			else {
				throw new Exception("Invalid state type: " + curState.getTypeString());
			}
			
			if (stateStack.isEmpty()) return;
			
		}
		
	}
	
}
