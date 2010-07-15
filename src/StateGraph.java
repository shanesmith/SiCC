
import java.util.Vector;

public class StateGraph<T> extends Vector<T> {
	
	private static final long serialVersionUID = 1L;

	public StateGraph() { 
		//do nothing 
	}
	
	public void addFirst(T s) { add(0, s); }
	public void addLast(T s) { add(s); }
	
	public T start() { return firstElement(); }
	public T end() { return lastElement(); }
	
	public String toString() {
		String str = "";
		
		for (T st : this) {
			str += st.toString() + " => ";
		}
		
		return str;
	}
}
