
import java.util.Stack;

public class GrammarRunner {

	private TokenizerRunner tokenizer;
	
	private GrammarDefinition grammardef;
	
	public GrammarRunner(GrammarDefinition grammardef, TokenizerRunner tokenizer) {
		this.grammardef = grammardef;
		this.tokenizer = tokenizer;
	}
	
	public ASTNode run() throws Exception {
		
		Token curToken;
		
		GrammarState curState;
		
		Stack<GrammarState> stateStack = new Stack<GrammarState>();
		
		
		ASTNode parseTree = null, curNode = null;
		
		stateStack.push(new GrammarState(grammardef.getStartRuleName(), GrammarState.RULE));
		
		curToken = tokenizer.nextToken();
		
		while ( true ) {
			
			curState = stateStack.pop();
			
			if (curState == null) {
				
				if (curNode.getParent() == null) {
					return parseTree;
				}
				
				if (curNode.isMultiChild() && curNode.numChildren() == 1) {
					curNode.getParent().removeChild(curNode);
					curNode.getParent().addChild( curNode.getChildren().firstElement() );
				}
				
				curNode = curNode.getParent();
				
			} 
			else if (curState.type == GrammarState.TOKEN) {
				
				if (!curState.name.equals(curToken.name)) {
					throw new Exception("Tokens do not match! read: \"" + curToken.name + "\"  stack: \"" + curState.name + "\"");
				}
				
				if (curToken.type == GrammarTokenizer.EOF_TOKEN) return parseTree;
				
				curNode.addChild(new ASTNode(curToken.name, curNode, curToken.value));
				
				System.out.println(curToken);
				
				curToken = tokenizer.nextToken();
				
			}
			else if (curState.type == GrammarState.RULE) {
				
				GrammarRule newrule = grammardef.getProduction(curState.name, curToken.name);
				
				if (newrule != null) {

					if (!newrule.isSubrule()) {
						if (parseTree == null) {
							parseTree = curNode = new ASTNode(newrule, null);
						} else {
							ASTNode newnode = new ASTNode(newrule, curNode);
							curNode.addChild(newnode);
							curNode = newnode;
						}
						
						stateStack.push(null);
					}
					
					if (newrule.hasGraph()) {
						newrule.pushGraphToStack(stateStack);
					}
				}
				
			}
			else {
				throw new Exception("Invalid state type: " + curState.getTypeString());
			}
			
		}
		
	}
	
}
