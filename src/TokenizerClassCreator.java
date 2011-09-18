
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
		out.println("import java.util.ArrayList;");
		out.println("import java.util.Stack;");
		out.println("import java.util.ListIterator;");
		out.println();
		
		out.println("public class " + classname + " implements " + interfacename + " {");
		out.println();
		
		int i = 1;
		for(TokenDFA tdfa : tokendef.getAllTokenDFA()) {
			if (tdfa.isInternal() || tdfa.name.equals("skip")) continue;
			
			out.println("  public static final int " + tdfa.name.toUpperCase() + "_TOKEN = " + (i++) + "; // " + tdfa.regexp);
		}
		out.println("  public static final int EOF_TOKEN = -1;");
		out.println();
		
		out.println("  private static final char wildcard = " + (int)TokenizerState.wildcard + ";");
		out.println();
		
		out.println("  private LineNumberReader input;");
		out.println();
		
		out.println("  private int currentColumn = 1;");
		out.println();
		
		out.println("  private Hashtable<Integer, Hashtable<Character, Integer>> DFA = new Hashtable<Integer, Hashtable<Character, Integer>>();");
		out.println();
		
		out.println("  private Hashtable<Integer, String> accepting = new Hashtable<Integer, String>();");
		out.println();
		
		out.println("  private ArrayList<" + tokclass + "> tokenHistory = new ArrayList<" + prefix + "Token>();");
		out.println("  private ListIterator<" + tokclass+ "> tokenHistoryIT = tokenHistory.listIterator();");
		out.println("  private int tokenHistorySize = 20;");
		out.println();
		
		out.println("  private Stack<Integer> pushedChars = new Stack<Integer>();");
		out.println();
		
		out.println("  public " + classname + " (Reader reader) {");
		out.println("    input = new LineNumberReader(reader);");
		out.println("    input.setLineNumber(1); //start at one");
		out.println("    buildDFA();");
		out.println("  } // end constructor");
		out.println();
		
		out.println("  public void setTokenHistorySize(int size) { tokenHistorySize = size; }");
		out.println("  public int getTokenHistorySize() { return tokenHistorySize; }");
		out.println();
		
		out.println("  public int getLineNumber() { return input.getLineNumber(); }");
		out.println();
		
		out.println("  public " + tokclass + " nextToken() throws " + prefix + "TokenizerException {");
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
		
		out.println("  private " + tokclass + " _nextToken() throws " + prefix + "TokenizerException {");
		out.println("    int c;");
		out.println("    String value;");
		out.println("    int curState;");
		out.println();
		out.println("  tokenLoop:");
		out.println("    while (true) {");
		out.println("      int lineNumber = getLineNumber();");
		out.println("      int column = currentColumn;");
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
		out.println("        return createToken(\"eof\", \"\", lineNumber, column);");
		out.println("      } else if (accepting.containsKey(curState)) {");
		out.println("        pushChar(c);");
		out.println("        if (accepting.get(curState).equals(\"skip\")) continue tokenLoop;");
		out.println("        return createToken(accepting.get(curState), value, lineNumber, column);");
		out.println("      } else {");
		out.println("        value += (char)c;");
		out.println("        throw new " + prefix + "NoSuchTokenException(value, lineNumber, column);");
		out.println("      }");
		out.println("    }");
		out.println("  } // end _nextToken");
		out.println();
		
		out.println("  public void pushToken() throws " + prefix + "TokenizerException {");
		out.println("    if (tokenHistoryIT.hasPrevious()) {");
		out.println("      tokenHistoryIT.previous();");
		out.println("    } else {");
		out.println("      throw new " + prefix + "TokenizerException(\"Token push limit (\" + tokenHistorySize + \") exceeded.\");");
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
		out.println("        nextState = -1;");
		out.println("      }");
		out.println();
		out.println("    }");
		out.println();
		out.println("    return nextState;");
		out.println("  } // end transition");
		out.println();
		
		out.println("  private void pushChar(Integer c) {");
		out.println("    pushedChars.push(c);");
		out.println("    if (c == 10 || (c == 13 && pushedChars.peek() != 10)) {");
		out.println("      input.setLineNumber(input.getLineNumber()-1);");
		out.println("      currentColumn = 999999999; // don't know the previous line's length, so set to a bajillion");
		out.println("    } else {");
		out.println("      currentColumn--;");
		out.println("    }");
		out.println("  }");
		out.println();
		
		out.println("  private int getChar() throws " + prefix + "TokenizerException {");
		out.println("    int c; int startLine = getLineNumber();");
		out.println("    if (!pushedChars.empty()) {");
		out.println("      c = pushedChars.pop();");
		out.println("      if (c == 10 || (c == 13 && pushedChars.peek() != 10)) input.setLineNumber(input.getLineNumber()+1);");
		out.println("    } else {");
		out.println("      try {");
		out.println("        c = input.read();");
		out.println("      }");
		out.println("      catch (IOException ex) {");
		out.println("        throw new " + prefix + "TokenizerException(ex);");
		out.println("      }");
		out.println("    }");
		out.println("    if (getLineNumber() > startLine) { currentColumn = 1; } else { currentColumn++; }");
		out.println("    return c;");
		out.println("  } //end getChar");
		out.println();

		out.println("  private " + tokclass + " createToken(String name, String value, int lineNumber, int column) {");
		for(TokenDFA tdfa : tokendef.getAllTokenDFA()) {
			if (tdfa.isInternal() || tdfa.name.equals("skip")) continue;
			
			out.println("    if ( name.equals(\"" + tdfa.name + "\") ) return new " + tokclass + "(" + tdfa.name.toUpperCase() + "_TOKEN, name, value, lineNumber, column);");
		}
		out.println("    if ( name.equals(\"eof\") ) return new " + tokclass + "(EOF_TOKEN, name, value, lineNumber, column);");
		out.println("    throw new RuntimeException(\"Cannot create token, unknown token name: \" + name);");
		out.println("  }");
		out.println();
		
		out.println("  private void buildDFA() {");
		for (TokenizerDFAState s : tokendef.getMasterTokenDFA().DFA) {
			
			if (s.isAccepting()) {
				accepting.put(s.id, s.getOwners().get(0).name);
			}
			
			out.println("    buildState" + s.id + "();");
			
		}
		out.println();
		for (Integer accept : accepting.keySet()) {
			out.printf("    accepting.put(%d, \"%s\");", accept, accepting.get(accept));
			out.println();
		}
		out.println("  } // end buildDFA");
		out.println();
		
		for (TokenizerDFAState s : tokendef.getMasterTokenDFA().DFA) {
			
			String tc = new String();
			String st = new String();
			
			for (Character c : s.getTransitionCharacters()) {
				tc += (int)c + ",";
				st += s.doTransition(c).getID() + ",";
			}
			
			out.println("  private void buildState" + s.id + "() {");
			out.println("    char[] tc = {" + tc + "};");
			out.println("    int[]  st = {" + st + "};");
			out.println("    Hashtable<Character, Integer> trans = new Hashtable<Character, Integer>();");
			out.println("    for (int i = 0; i < tc.length; i++) trans.put(tc[i], st[i]);");
			out.println("    DFA.put(" + s.id + ", trans);"); 
			out.println("  } // end buildState" + s.id);
			out.println();
			
		}
		
		/*
		out.println("  private void buildDFA() {");
		out.println("    Hashtable<Character, Integer> trans;");
		for (TokenizerDFAState s : tokendef.getMasterTokenDFA().DFA) {
			
			if (s.isAccepting()) {
				accepting.put(s.id, s.getOwners().start().name);
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
		*/
		
		
		
		out.println("} // end " + classname);
		
	}
	
}
