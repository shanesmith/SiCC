
import java.io.PrintWriter;
import java.io.IOException;
import java.util.Hashtable;

/**
 * Output the Tokenizer class based on the passed token definitions
 *
 */
public class TokenizerClassCreator {

	/**
	 * A prefix to prepend to all class names
	 */
	private String prefix = "";
	
	/**
	 * The token definition we base the tokenizer on
	 */
	private TokenizerDefinition tokendef;

	/**
	 * Constructor.
	 */
	public TokenizerClassCreator (String prefix, TokenizerDefinition tokendef) {
		this.prefix = prefix;
		this.tokendef = tokendef;
	}
	
	/**
	 * Output the tokenizer class to the provided writer
	 */
	public void output(PrintWriter out) throws IOException {		
		String classname = prefix + "Tokenizer";
		String interfacename = prefix + "iTokenizer";
		String tokclass = prefix + "Token";
		
		Hashtable<Integer, String> accepting = new Hashtable<Integer, String>();
		
		out.println("import java.io.*;");
		out.println("import java.util.Hashtable;");
		out.println("import java.util.Vector;");
		out.println("import java.util.Stack;");
		out.println("import java.util.ListIterator;");
		out.println();
		
		out.println("public class " + classname + " implements " + interfacename + " {");
		out.println();
		
		int i = 1;
		for(TokenDFA tdfa : tokendef.getAllTokenDFA()) {
			if (tdfa.isInternal()) continue;
			
			out.println("  public static final int " + tdfa.name.toUpperCase() + "_TOKEN = " + (i++) + "; // " + tdfa.regexp);
		}
		out.println("  public static final int EOF_TOKEN = -1;");
		out.println();
		
		out.println("  private static final char wildcard = " + (int)TokenizerState.wildcard + ";");
		out.println("  private static final char neg = " + (int)TokenizerState.neg + ";");
		out.println();
		
		out.println("  private LineNumberReader input;");
		out.println();
		
		out.println("  private Hashtable<Integer, Hashtable<Character, Integer>> DFA = new Hashtable<Integer, Hashtable<Character, Integer>>();");
		out.println();
		
		out.println("  private Hashtable<Integer, String> accepting = new Hashtable<Integer, String>();");
		out.println();
		
		out.println("  private Vector<" + tokclass + "> tokenHistory = new Vector<" + prefix + "Token>();");
		out.println("  private ListIterator<" + tokclass+ "> tokenHistoryIT = tokenHistory.listIterator();");
		out.println("  private int tokenHistorySize = 20;");
		out.println();
		
		out.println("  private Stack<Integer> pushedChars = new Stack<Integer>();");
		out.println();
		
		out.println("  public " + classname + " (Reader reader) {");
		out.println("    input = new LineNumberReader(reader);");
		out.println("    buildDFA();");
		out.println("  } // end constructor");
		out.println();
		
		out.println("  public void setTokenHistorySize(int size) { tokenHistorySize = size; }");
		out.println("  public int getTokenHistorySize() { return tokenHistorySize; }");
		out.println();
		
		out.println("  public int getLineNumber() { return input.getLineNumber() + 1; }");
		out.println();
		
		out.println("  public " + tokclass + " nextToken() throws TokenizerException {");
		out.println("    if (tokenHistoryIT.hasNext()) {");
		out.println("      return tokenHistoryIT.next();");
		out.println("    } else {");
		out.println("      " + tokclass + " token = _nextToken();");
		out.println("      tokenHistoryIT.add(token);");
		out.println("      if (tokenHistory.size() > tokenHistorySize) {");
		out.println("        tokenHistory.remove(0);");
		out.println("        tokenHistoryIT = tokenHistory.listIterator(tokenHistorySize);");
		out.println("      }");
		out.println("      return token;");
		out.println("    }");
		out.println("  } // end nextToken");
		out.println();
		
		out.println("  private " + tokclass + " _nextToken() throws TokenizerException {");
		out.println("    int c;");
		out.println("    String value;");
		out.println("    int curState;");
		out.println();
		out.println("  tokenLoop:");
		out.println("    while (true) {");
		out.println("      int lineNumber = input.getLineNumber() + 1; // LineNumberReader starts at zero");
		out.println("      curState = 0;");
		out.println("      value = \"\";");
		out.println();
		out.println("      while ( (c=getChar()) != -1 ) { // read in a character (-1 indicates EOF)");
		out.println("        if (transition(curState, (char)c) == -1) break;");
		out.println("        value += (char)c;");
		out.println("        curState = transition(curState, (char)c);");
		out.println("      }");
		out.println();
		out.println("      if (c == -1 && value.isEmpty()) {");
		out.println("        return createToken(\"eof\", \"\", lineNumber);");
		out.println("      } else if (accepting.containsKey(curState)) {");
		out.println("        pushChar(c);");
		out.println("        if (accepting.get(curState).equals(\"skip\")) continue tokenLoop;");
		out.println("        return createToken(accepting.get(curState), value, lineNumber);");
		out.println("      } else {");
		out.println("        value += (char)c;");
		out.println("        throw new NoSuchTokenException(value, lineNumber);");
		out.println("      }");
		out.println("    }");
		out.println("  } // end _nextToken");
		out.println();
		
		out.println("  public void pushToken() throws TokenizerException {");
		out.println("    if (tokenHistoryIT.hasPrevious()) {");
		out.println("      tokenHistoryIT.previous();");
		out.println("    } else {");
		out.println("      throw new TokenizerException(\"Token push limit (\" + tokenHistorySize + \") reached.\");");
		out.println("    }");
		out.println("  } // end pushToken");
		out.println();
		
		out.println("  private int transition(Integer state, Character c) {");
		out.println("    Integer nextState = DFA.get(state).get(c);");
		out.println();
		out.println("    if (nextState == null) {");
		out.println("      nextState = DFA.get(state).get(wildcard);");
		out.println();
		out.println("      if (nextState == null) {");
		out.println("        nextState = DFA.get(state).get(neg);");
		out.println();
		out.println("        if (nextState == null) {");
		out.println("          nextState = -1;");
		out.println("        }");
		out.println();
		out.println("      }");
		out.println();
		out.println("    }");
		out.println();
		out.println("    return nextState;");
		out.println("  } // end transition");
		out.println();
		
		out.println("  private void pushChar(Integer c) { pushedChars.push(c); }");
		out.println();
		
		out.println("  private int getChar() throws TokenizerException {");
		out.println("    if (!pushedChars.empty()) {");
		out.println("      return pushedChars.pop();");
		out.println("    } else {");
		out.println("      try {");
		out.println("        return input.read();");
		out.println("      }");
		out.println("      catch (IOException ex) {");
		out.println("        throw new TokenizerException(ex);");
		out.println("      }");
		out.println("    }");
		out.println("  } //end getChar");
		out.println();
		
		out.println("  private void buildDFA() {");
		out.println("    Hashtable<Character, Integer> trans;");
		for (TokenizerDFAState s : tokendef.getMasterTokenDFA().DFA) {
			
			if (s.isAccepting()) {
				accepting.put(s.id, s.getOwners().firstElement().name);
			}
			
			out.println();
			out.println("    // state " + s.id);
			out.println("    trans = new Hashtable<Character, Integer>();");
			out.println("    DFA.put(" + s.id + ", trans);"); 
			
			for (Character c : s.getTransitionCharacters()) {
				out.printf("    trans.put((char)%d, %d);", (int)c, s.doTransition(c).getID());
				out.println();
			}
			
		}
		out.println();
		for (Integer accept : accepting.keySet()) {
			out.printf("    accepting.put(%d, \"%s\");", accept, accepting.get(accept));
			out.println();
		}
		out.println("  } //end buildDFA()");
		out.println();
		
		out.println("  private " + tokclass + " createToken(String name, String value, int lineNumber) {");
		for(TokenDFA tdfa : tokendef.getAllTokenDFA()) {
			if (tdfa.isInternal()) continue;
			
			out.println("    if ( name.equals(\"" + tdfa.name + "\") ) return new " + tokclass + "(" + tdfa.name.toUpperCase() + "_TOKEN, name, value, lineNumber);");
		}
		out.println("    if ( name.equals(\"eof\") ) return new " + tokclass + "(EOF_TOKEN, name, value, lineNumber);");
		out.println("    throw new RuntimeException(\"Cannot create token, unknown token name: \" + name);");
		out.println("  }");
		out.println();
		
		out.println("} // end " + classname);
		
	}
	
}
