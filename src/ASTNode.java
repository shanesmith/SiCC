
import java.util.Vector;

/**
 * A parse tree node, used by GrammarRunner
 */
public class ASTNode {

	private String name, value;
	
	private ASTNode parent;
	
	private boolean multi_child = false;
	
	private Vector<ASTNode> children = new Vector<ASTNode>();

	public ASTNode(GrammarRule rule, ASTNode parent) {
		this.name = rule.getName();
		this.multi_child = rule.isMultiChild();
		this.parent = parent;
	}
	
	public ASTNode(String name, ASTNode parent, String value) {
		this(name, parent);
		this.value = value;
	}
	
	public ASTNode(String name, ASTNode parent) {
		this.name = name;
		this.parent = parent;
	}
	
	public void addChild(ASTNode child) { children.add(child); }
	public void removeChild(ASTNode child) { children.remove(child); }
	public Vector<ASTNode> getChildren() { return children; }
	public int numChildren() { return children.size(); }
	
	public String getName() { return name; }
	public String getValue() { return value; }
	public ASTNode getParent() { return parent; }
	
	public void setMultiChild(boolean multi) { multi_child = multi; }
	public boolean isMultiChild() { return multi_child; }
	
	public String toString() { 
		return name + (value != null ? " => " + value : "");
	}
}
