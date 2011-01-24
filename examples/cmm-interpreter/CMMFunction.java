
public class CMMFunction extends CMMData {
	protected CMMASTFunctionDefinitionNode value;
	
	public CMMFunction(CMMASTFunctionDefinitionNode value) {
		this.value = value;
	}
	
	public CMMASTFunctionDefinitionNode value() {
		return value;
	}
}
