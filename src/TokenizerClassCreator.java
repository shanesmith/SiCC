import java.io.PrintWriter;
import java.util.Hashtable;


public class TokenizerClassCreator {

	String prefix = "";
	TokenizerDefinition tokendef;
	
	public TokenizerClassCreator (TokenizerDefinition tokendef) {
		this.tokendef = tokendef;
	}
	
	public TokenizerClassCreator (String prefix, TokenizerDefinition tokendef) {
		this(tokendef);
		this.prefix = prefix;
	}
	
	public void output(PrintWriter out) throws Exception {		
		Hashtable<Integer, String> accepting = new Hashtable<Integer, String>();
		
		/*
		out.println();
		out.println("/*");
		out.println();
		for(TokenDFA tdfa : tokendef.getAllTokenDFA()) {
			String def = "  ";
			
			if (tdfa.isInternal()) def += ":";
			
			def += tdfa.name + ": " + tdfa.regexp;
			
			out.println(def);
			out.println();	
			
		}
		out.println("*\/");
		out.println();
		*/
		
		out.println("import java.io.*;");
		out.println("import java.util.Hashtable;");
		out.println("import java.util.Vector;");
		out.println("import java.util.Stack;");
		out.println("import java.util.ListIterator;");
		out.println();
		
		out.println("class " + prefix + "Tokenizer {");
		out.println();
		
		int i = 1;
		for(TokenDFA tdfa : tokendef.getAllTokenDFA()) {
			if (tdfa.isInternal()) continue;
			
			out.println("  public static final int " + tdfa.name.toUpperCase() + "_TOKEN = " + (i++) + "; // " + tdfa.regexp);
		}
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
		
		out.println("  private Vector<" + prefix + "Token> tokenHistory = new Vector<" + prefix + "Token>();");
		out.println("  private ListIterator<" + prefix + "Token> tokenHistoryIT = tokenHistory.listIterator();");
		out.println("  private int tokenHistorySize = 20;");
		out.println();
		
		out.println("  private Stack<Integer> pushedChars = new Stack<Integer>();");
		out.println();
		
		out.println("  public " + prefix + "Tokenizer(Reader reader) {");
		out.println("    input = new LineNumberReader(reader);");
		out.println("    buildDFA();");
		out.println("  } // end constructor");
		out.println();
		
		out.println("  public " + prefix + "Token nextToken() throws Exception {");
		out.println("    if (tokenHistoryIT.hasNext()) {");
		out.println("      return tokenHistoryIT.next();");
		out.println("    } else {");
		out.println("      " + prefix + "Token token = _nextToken();");
		out.println("      tokenHistoryIT.add(token);");
		out.println("      if (tokenHistory.size() > tokenHistorySize) {");
		out.println("        tokenHistory.remove(0);");
		out.println("        tokenHistoryIT = tokenHistory.listIterator(tokenHistorySize);");
		out.println("      }");
		out.println("      return token;");
		out.println("    }");
		out.println("  } // end nextToken");
		out.println();
		
		out.println("  private " + prefix + "Token _nextToken() throws Exception{");
		out.println("    int c;");
		out.println("    String value;");
		out.println("    int curState;");
		out.println("    int lineNumber = input.getLineNumber(); //get line number right away since reading might change it");
		out.println();
		out.println("  tokenLoop:");
		out.println("    while (true) {");
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
		// TODO proper token type
		out.println("        return new Token(0, \"eof\", \"\", lineNumber);");
		out.println("      } else if (accepting.containsKey(curState)) {");
		out.println("        pushChar(c);");
		out.println("        if (accepting.get(curState) == \"skip\") continue tokenLoop;");
		// TODO proper token type
		out.println("        return new " + prefix + "Token(0, accepting.get(curState), value, lineNumber);");
		out.println("      } else {");
		out.println("        value += (char)c;");
		out.println("        String error = \"(\" + lineNumber + \") No such token for char sequence: \";");
		out.println("        for (char cc : value.toCharArray()) error += (int)cc + \" (\" + cc + \"), \";");
		out.println("        throw new Exception(error);");
		out.println("      }");
		out.println("    }");
		out.println("  } // end _nextToken");
		out.println();
		
		out.println("  public void pushToken() throws Exception {");
		out.println("    if (tokenHistoryIT.hasPrevious()) {");
		out.println("      tokenHistoryIT.previous();");
		out.println("    } else {");
		out.println("      throw new Exception(\"Too many token pushbacks!\");");
		out.println("    }");
		out.println("  } // end pushToken");
		out.println();
		
		out.println("  public void setTokenHistorySize(int size) { tokenHistorySize = size; }");
		out.println("  public int getTokenHistorySize() { return tokenHistorySize; }");
		
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
		out.println("  } // end feed");
		out.println();
		
		out.println("  private void pushChar(Integer c) { pushedChars.push(c); }");
		out.println();
		
		out.println("  private int getChar() throws Exception {");
		out.println("    if (!pushedChars.empty()) {");
		out.println("      return pushedChars.pop();");
		out.println("    } else {");
		out.println("      int charcode = input.read();");
		out.println("      if (isInvalidCharCode(charcode)) throw new Exception(\"invalid character! (\" + charcode + \")\");");
		out.println("      return charcode;");
		out.println("    }");
		out.println("  } //end getChar");
		out.println();
		
		out.println("  public static boolean isInvalidCharCode(int c) { return c < -1 || (c > -1 && c < 10) || (c > 13 && c < 32) || c > 126; }");
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
			out.printf("    DFA.put(%d, trans);", s.id); out.println(); 
			
			for (Character c : s.getTransitionCharacters()) {
				out.printf("    trans.put((char)%d, %d);", (int)c, s.doTransition(c).getID()); out.println();
			}
			
		}
		out.println();
		for (Integer accept : accepting.keySet()) {
			out.printf("    accepting.put(%d, \"%s\");", accept, accepting.get(accept));
			out.println();
		}
		out.println("  } //end buildDFA()");
		out.println();
		
		out.println("} // end " + prefix + "Tokenizer");
		
	}
	
}
