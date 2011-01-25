
public class CMMNumber extends CMMData {
	protected double value;
	
	public CMMNumber(double value) {
		this.value = value;
	}
	
	public double value() {
		return value;
	}
	
	public String toString() {
		return Double.toString(value);
	}
}
