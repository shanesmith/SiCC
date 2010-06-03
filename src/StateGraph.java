
import java.util.Vector;

@SuppressWarnings("serial")
public class StateGraph<T> extends Vector<T> {
	
	public StateGraph() { 
		//do nothing 
	}
	
	public StateGraph(StateGraph<? extends T> copy) { 
		super(copy); 
	}
	
	public StateGraph<T> copy() {
		return new StateGraph<T>(this);
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
