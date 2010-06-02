
public class CharacterMatch {

	static final int CLASS = 0, NEG_CLASS = 1, WILDCARD = 2;
	
	String val;
	
	int type = 0;
	
	public CharacterMatch(String str) { val = replacements(str); }
	public CharacterMatch(String str, int matchType) { val = replacements(str); type = matchType;  }
	
	public CharacterMatch(char c) { val = String.valueOf(c); }
	public CharacterMatch(char c, int matchType) { val = String.valueOf(c); type = matchType;  }
	
	private String replacements(String str) {
		return str.replace("\\s", " ").replace("\\t", "\t").replace("\\r", "\r").replace("\\n", "\n");
	}
	
	private String escape(String str) {
		return str.replace(" ", "\\s").replace("\t", "\\t").replace("\r", "\\r").replace("\n", "\\n");
	}
	
	public boolean matches(char c) {
		switch(type) {
			case CLASS:
				return Utils.in_array(c, val.toCharArray());
			case NEG_CLASS:
				return !Utils.in_array(c, val.toCharArray());
			case WILDCARD:
				if (val.equals(".")) return true;
			default:
				return false;
		}
	}
	
	public String toString() {
		String str = escape(val);
		
		switch (type) {
			case CLASS:
				return str;
			case NEG_CLASS:
				return "~ "+str;
			case WILDCARD:
				return "="+str;
			default:
				return str;
		}
	}
	
	// overwriting equals needs to have hashCode overwritten to...
	/*
	public boolean equals(ssCCCharacterMatch cm) {
		return cm.val.equals(val) && cm.type == type;
	}
	*/
	
}
