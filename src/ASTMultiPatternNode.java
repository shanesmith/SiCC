import java.util.Vector;


public class ASTMultiPatternNode extends ASTPatternNode {

	public static final int ALTERN = 1;
	public static final int CONCAT = 2;
	
	// CONCAT, ALTERN -> how the subpatterns relate
	// ex: a|b|c  -> {a, b, c}, operator = ALTERN
	//     a[bc]d -> {a, [bc], d}, operator = CONCAT
	int operator;
	
	// for use with character classes only (create new pattern node type?)
	boolean negate;
	
	// tree definition of token
	Vector<ASTPatternNode> subpatterns;
	
}
