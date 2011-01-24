import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;


public class CMMEnvironment {
	List<Map<String,CMMData>> env;
	
	/**
	 * Create a new environment with one (empty) frame
	 */
	public CMMEnvironment() {
		env = new ArrayList<Map<String,CMMData>>();
		env.add(new HashMap<String,CMMData>());
	}
	
	/**
	 * Push a new scope frame on to the environment
	 */
	public void pushFrame() {
		env.add(new HashMap<String,CMMData>());		
	}
	
	/**
	 * Pop the topmost scope frame from the environment
	 */
	public void popFrame() {
		env.remove(env.size()-1);		
	}
	
	/**
	 * Lookup an identifier in the environment
	 * @param id the identifier to lookup
	 * @return the value bound to id, or null if no value
	 * is bound to id
	 */
	public CMMData lookup(String id) {
		ListIterator<Map<String,CMMData>> i = env.listIterator(env.size());
		while (i.hasPrevious()) {
			Map<String,CMMData> frame = i.previous();
			if (frame.containsKey(id)) {
				return frame.get(id);
			}
		}
		return null;
	}
	
	/**
	 * Add an id/value pair to the topmost stack frame
	 * @param id
	 * @param value
	 */
	public void bind(String id, CMMData value) {
		Map<String,CMMData> frame = env.get(env.size()-1);
		frame.put(id, value);		
	}
}
