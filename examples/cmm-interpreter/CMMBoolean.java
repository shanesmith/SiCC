
public class CMMBoolean extends CMMData {
	protected boolean value;
	
	public CMMBoolean(boolean value) {
		this.value = value;
	}
	
	public boolean value() {
		return value;
	}
	
	public String toString() {
		return Boolean.toString(value);
	}
}
