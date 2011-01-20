
public class CalcEvalVisitor implements CalcVisitor<Double, Object> {

	/**
	 * This should never get called
	 */
	public Double visit(CalcASTNode node, Object data) {
		System.err.println("Should never be called!");
		return null;
	}

	/**
	 * Element -> number | lparen Sum rparen
	 */
	public Double visit(CalcASTElementNode node, Object data) {
		Double r = null;
		for (int i = 0; i < node.numChildren(); i++) {
			Double x = node.getChild(i).accept(this, data);
			if (x != null) r = x;
		}
		return r;
	}

	/**
	 * Exp -> Element (exp Element)*  [>1] 
	 */
	public Double visit(CalcASTExpNode node, Object data) {
		Double r = node.getChild(0).accept(this, data);
		for (int i = 1; i < node.numChildren(); i += 2) {
			Double x = node.getChild(i+1).accept(this, data);
			r = Math.pow(r,x);
		}
		return r;
	}

	/**
	 * Expression -> Sum
	 */
	public Double visit(CalcASTExpressionNode node, Object data) {
		return node.getChild(0).accept(this, data);
	}

	/**
	 * Sum -> Term ((plus|minus) Term)*  [>1]
	 */
	public Double visit(CalcASTSumNode node, Object data) {
		Double r = node.getChild(0).accept(this, data);
		for (int i = 1; i < node.numChildren(); i += 2) {
			Double x = node.getChild(i+1).accept(this, data);
			if (node.getChild(i).getValue().equals("+")) {
				r = r + x;
			} else {
				r = r - x;				
			}
		}
		return r;
	}

	/**
	 * Term -> Exp ((multiply|divide) Exp)* [>1]  
	 */
	public Double visit(CalcASTTermNode node, Object data) {
		Double r = node.getChild(0).accept(this, data);
		for (int i = 1; i < node.numChildren(); i += 2) {
			Double x = node.getChild(i+1).accept(this, data);
			if (node.getChild(i).getValue().equals("*")) {
				r = r * x;
			} else {
				r = r / x;				
			} 
		}
		return r;
	}

	/**
	 * raw tokens, either numbers or symbols
	 */
	public Double visit(CalcASTToken node, Object data) {
		Double r = null;
		if (node.getValue() != null) {
			r = Double.valueOf(node.getValue());
		}
		return r;
	}
}
