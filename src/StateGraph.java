
import java.util.ArrayList;

public class StateGraph<T> extends ArrayList<T> {
	
	private static final long serialVersionUID = 1L;

	public StateGraph() { 
		//do nothing 
	}
	
	public void addFirst(T s) { add(0, s); }
	public void addLast(T s) { add(s); }
	
	public T start() { return get(0); }
	public T end() { return get(size()-1); }
	
	public String toString() {
		String str = "";
		
		for (T st : this) {
			str += st.toString() + " => ";
		}
		
		return str;
	}
}
