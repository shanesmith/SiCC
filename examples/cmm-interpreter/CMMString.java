
public class CMMString extends CMMData {
	protected String value;
	
	public CMMString(String value) {
		this.value = value.replaceAll("\\\"", "\"");
	}
	
	public String value() {
		return value;
	}
	
	public String toString() {
		return value;
	}
}
