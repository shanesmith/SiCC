
/*
 * A collection of useful functions
 */

public class Utils {
	
	/*
	 * Returns whether or not character c is found in array
	 */
	public static boolean in_array(char c, char[] array) {
		for (char a : array) {
			if (c == a) return true;
		}
		return false;
	}
	
	
	public static String escape(Character c) {
		return escape(c.toString());
	}
	
	public static String escape(String str) {
		return str.replace(" ", "\\s").replace("\t", "\\t").replace("\r", "\\r").replace("\n", "\\n");
	}
	
	
}
