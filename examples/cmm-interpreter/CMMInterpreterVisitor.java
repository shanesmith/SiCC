

public class CMMInterpreterVisitor implements
		CMMVisitor<CMMData, CMMEnvironment> {

	/**
	 * Environment keeps track of variable bindings
	 */
	protected CMMEnvironment env;
	
	public CMMInterpreterVisitor() {
		env = new CMMEnvironment();
	}
	
	@Override
	public CMMData visit(CMMASTNode node, CMMEnvironment data) {
		// should never be called
		return null;
	}

	@Override
	public CMMData visit(CMMASTPrefixUnaryNode node, CMMEnvironment data) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public CMMData visit(CMMASTNumberConstantNode node, CMMEnvironment data) {
		return visitChildren(node, data);
	}

	@Override
	public CMMData visit(CMMASTParameterNode node, CMMEnvironment data) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public CMMData visit(CMMASTSumNode node, CMMEnvironment data) {
		return visitChildren(node, data);
	}

	@Override
	public CMMData visit(CMMASTSimpleStatementNode node, CMMEnvironment data) {
		return visitChildren(node, data);
	}

	@Override
	public CMMData visit(CMMASTConstantNode node, CMMEnvironment data) {
		return visitChildren(node, data);
	}

	@Override
	public CMMData visit(CMMASTLogicalNode node, CMMEnvironment data) {
		return visitChildren(node, data);
	}

	@Override
	public CMMData visit(CMMASTParameterListNode node, CMMEnvironment data) {
		return visitChildren(node, data);
	}

	@Override
	public CMMData visit(CMMASTArgumentListNode node, CMMEnvironment data) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public CMMData visit(CMMASTPrefixUnaryOpNode node, CMMEnvironment data) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public CMMData visit(CMMASTUnaryNode node, CMMEnvironment data) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public CMMData visit(CMMASTElementNode node, CMMEnvironment data) {
		return visitChildren(node, data);
	}

	@Override
	public CMMData visit(CMMASTExpressionListNode node, CMMEnvironment data) {
		return visitChildren(node, data);
	}

	@Override
	public CMMData visit(CMMASTComparisonNode node, CMMEnvironment data) {
		return visitChildren(node, data);
	}

	@Override
	public CMMData visit(CMMASTPostfixUnaryOpNode node, CMMEnvironment data) {
		throw new UnsupportedOperationException();
	}

	@Override
	public CMMData visit(CMMASTElementPlusNode node, CMMEnvironment data) {
		return visitChildren(node, data);
	}

	@Override
	public CMMData visit(CMMASTTermNode node, CMMEnvironment data) {
		return visitChildren(node, data);
	}

	@Override
	public CMMData visit(CMMASTConditionNode node, CMMEnvironment data) {
		return visitChildren(node, data);
	}

	@Override
	public CMMData visit(CMMASTWhileLoopNode node, CMMEnvironment data) {
		throw new UnsupportedOperationException();
	}

	@Override
	public CMMData visit(CMMASTBooleanConstantNode node, CMMEnvironment data) {
		return visitChildren(node, data);
	}

	@Override
	public CMMData visit(CMMASTDoLoopNode node, CMMEnvironment data) {
		throw new UnsupportedOperationException();
	}

	@Override
	public CMMData visit(CMMASTReturnStatementNode node, CMMEnvironment data) {
		CMMData res = node.getChild(1).accept(this, data);
		// TODO: finish
		return null;
	}

	@Override
	public CMMData visit(CMMASTExpNode node, CMMEnvironment data) {
		// TODO Auto-generated method stub
		return null;
	}

	// FunctionDefinition -> Type id ParameterList Block
	public CMMData visit(CMMASTFunctionDefinitionNode node, CMMEnvironment data) {
		String id = node.getChild(1).getValue();
		env.bind(id, new CMMFunction(node));
		if (id.equals("main")) {
			return node.getChild(3).accept(this, data);
		}
		return null;
	}

	// Assignment -> Logical (gets Logical)?
	public CMMData visit(CMMASTAssignmentNode node, CMMEnvironment data) {
		if (node.numChildren() > 1) {
			// an actual assignment operation
			return null; // TODO: finish
		} else {
			return visitChildren(node, data);
		}
	}

	@Override
	public CMMData visit(CMMASTStatementNode node, CMMEnvironment data) {
		return visitChildren(node, data);
	}

	@Override
	public CMMData visit(CMMASTTypeNode node, CMMEnvironment data) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public CMMData visit(CMMASTIfStatementNode node, CMMEnvironment data) {
		throw new UnsupportedOperationException();
	}

	@Override
	/*
	 * Declaration -> Type Identifier (listsep Identifier)* eol
	 */
	public CMMData visit(CMMASTDeclarationNode node, CMMEnvironment data) {
		CMMASTNode type = node.getChild(0);
		String stype = type.getChild(0).getName();
		if (stype.equals("number_t")) {
			for (int i = 1; i < node.numChildren(); i += 2)
				env.bind(node.getChild(i).getValue(), new CMMNumber(0));
		} else if (stype.equals("string_t")) {
			for (int i = 1; i < node.numChildren(); i += 2)
				env.bind(node.getChild(i).getValue(), new CMMString(""));			
		} else if (stype.equals("boolean_t")) {
			for (int i = 1; i < node.numChildren(); i += 2)
				env.bind(node.getChild(i).getValue(), new CMMBoolean(false));						
		}
		return null;
	}

	@Override
	public CMMData visit(CMMASTStringConstantNode node, CMMEnvironment data) {
		return visitChildren(node, data);
	}

	@Override
	public CMMData visit(CMMASTProgramNode node, CMMEnvironment data) {
		return visitChildren(node, data);
	}

	@Override
	public CMMData visit(CMMASTBlockNode node, CMMEnvironment data) {
		return visitChildren(node, data);
	}

	@Override
	public CMMData visit(CMMASTToken node, CMMEnvironment data) {
		if (node.getName().equals("number")) {
			return new CMMNumber(Double.parseDouble(node.getValue()));
		} else if (node.getName().equals("string")) {
			return new CMMString(node.getValue());
		} else if (node.getName().equals("boolean")) {
			return new CMMBoolean(Boolean.parseBoolean(node.getValue()));			
		} else if (node.getName().equals("id")) {
			String id = node.getValue();
			return env.lookup(id);
		}
		return null;
	}
	
	protected CMMData visitChildren(CMMASTNode node, CMMEnvironment data) {
		CMMData last = null;
		for (int i = 0; i < node.numChildren(); i++) {
			CMMData tmp = node.getChild(i).accept(this, data);
			if (tmp != null) last = tmp;
		}
		return last;	
	}
}
