import java.io.*;
import java.util.Hashtable;
import java.util.Vector;
import java.util.Stack;
import java.util.ListIterator;

/**
 * Tokenizer used to read the grammar definition file, 
 * created automatically with this project's tokenizer class creator
 * with the following token definitions
 * 
 * 		eol: (\n | \r | \r\n)
 * 
 * 		skip: \# [^\n\r]*
 * 		skip: [\s\t]
 * 		
 * 		sep: ->
 * 		
 * 		op: [\* \? \|]
 * 		
 * 		lparen: \(
 * 		rparen: \)
 * 
 * 		multi_child: \[>1\]
 * 
 * 		:alpha: [abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ_]
 * 		:digit: [0123456789]
 * 		id: :alpha: (:digit: | :alpha:)*
 * 
 * 
 */
public class GrammarTokenizer implements iTokenizer {

	  public static final int ID_TOKEN = 1; // :alpha: (:digit: | :alpha:)*
	  public static final int OP_TOKEN = 2; // [\* \? \|]
	  public static final int EOL_TOKEN = 3; // (\n | \r | \r\n)
	  public static final int RPAREN_TOKEN = 4; // \)
	  public static final int MULTI_CHILD_TOKEN = 5; // \[>1\]
	  public static final int SKIP_TOKEN = 6; // \# [^\n\r]*
	  public static final int LPAREN_TOKEN = 7; // \(
	  public static final int SEP_TOKEN = 8; // ->
	  public static final int EOF_TOKEN = -1;

	  private static final char wildcard = 3;
	  private static final char neg = 4;

	  private LineNumberReader input;

	  private Hashtable<Integer, Hashtable<Character, Integer>> DFA = new Hashtable<Integer, Hashtable<Character, Integer>>();

	  private Hashtable<Integer, String> accepting = new Hashtable<Integer, String>();

	  private Vector<Token> tokenHistory = new Vector<Token>();
	  private ListIterator<Token> tokenHistoryIT = tokenHistory.listIterator();
	  private int tokenHistorySize = 20;

	  private Stack<Integer> pushedChars = new Stack<Integer>();

	  public GrammarTokenizer (Reader reader) {
	    input = new LineNumberReader(reader);
	    buildDFA();
	  } // end constructor

	  public void setTokenHistorySize(int size) { tokenHistorySize = size; }
	  public int getTokenHistorySize() { return tokenHistorySize; }

	  public int getLineNumber() { return input.getLineNumber() + 1; }

	  public Token nextToken() throws TokenizerException {
	    if (tokenHistoryIT.hasNext()) {
	      return tokenHistoryIT.next();
	    } else {
	      Token token = _nextToken();
	      tokenHistoryIT.add(token);
	      if (tokenHistory.size() > tokenHistorySize) {
	        tokenHistory.remove(0);
	        tokenHistoryIT = tokenHistory.listIterator(tokenHistorySize);
	      }
	      return token;
	    }
	  } // end nextToken

	  private Token _nextToken() throws TokenizerException {
	    int c;
	    String value;
	    int curState;

	  tokenLoop:
	    while (true) {
	      int lineNumber = input.getLineNumber() + 1; // LineNumberReader starts at zero
	      curState = 0;
	      value = "";

	      while ( (c=getChar()) != -1 ) { // read in a character (-1 indicates EOF)
	        if (transition(curState, (char)c) == -1) break;
	        value += (char)c;
	        curState = transition(curState, (char)c);
	      }

	      if (c == -1 && value.isEmpty()) {
	        return createToken("eof", "", lineNumber);
	      } else if (accepting.containsKey(curState)) {
	        pushChar(c);
	        if (accepting.get(curState).equals("skip")) continue tokenLoop;
	        return createToken(accepting.get(curState), value, lineNumber);
	      } else {
	        value += (char)c;
	        throw new NoSuchTokenException(value, lineNumber);
	      }
	    }
	  } // end _nextToken

	  public void pushToken() throws TokenizerException {
	    if (tokenHistoryIT.hasPrevious()) {
	      tokenHistoryIT.previous();
	    } else {
	      throw new TokenizerException("Token push limit (" + tokenHistorySize + ") reached.");
	    }
	  } // end pushToken

	  private int transition(Integer state, Character c) {
	    Integer nextState = DFA.get(state).get(c);

	    if (nextState == null) {
	      nextState = DFA.get(state).get(wildcard);

	      if (nextState == null) {
	        nextState = DFA.get(state).get(neg);

	        if (nextState == null) {
	          nextState = -1;
	        }

	      }

	    }

	    return nextState;
	  } // end transition

	  private void pushChar(Integer c) { pushedChars.push(c); }

	  private int getChar() throws TokenizerException {
	    if (!pushedChars.empty()) {
	      return pushedChars.pop();
	    } else {
	      try {
	        return input.read();
	      }
	      catch (IOException ex) {
	        throw new TokenizerException(ex);
	      }
	    }
	  } //end getChar

	  private void buildDFA() {
	    Hashtable<Character, Integer> trans;

	    // state 0
	    trans = new Hashtable<Character, Integer>();
	    DFA.put(0, trans);
	    trans.put((char)91, 4);
	    trans.put((char)90, 1);
	    trans.put((char)89, 1);
	    trans.put((char)88, 1);
	    trans.put((char)87, 1);
	    trans.put((char)86, 1);
	    trans.put((char)85, 1);
	    trans.put((char)84, 1);
	    trans.put((char)83, 1);
	    trans.put((char)82, 1);
	    trans.put((char)81, 1);
	    trans.put((char)80, 1);
	    trans.put((char)79, 1);
	    trans.put((char)78, 1);
	    trans.put((char)77, 1);
	    trans.put((char)76, 1);
	    trans.put((char)75, 1);
	    trans.put((char)74, 1);
	    trans.put((char)73, 1);
	    trans.put((char)72, 1);
	    trans.put((char)71, 1);
	    trans.put((char)70, 1);
	    trans.put((char)69, 1);
	    trans.put((char)68, 1);
	    trans.put((char)67, 1);
	    trans.put((char)66, 1);
	    trans.put((char)65, 1);
	    trans.put((char)63, 2);
	    trans.put((char)45, 6);
	    trans.put((char)42, 2);
	    trans.put((char)41, 3);
	    trans.put((char)40, 5);
	    trans.put((char)35, 7);
	    trans.put((char)32, 8);
	    trans.put((char)124, 2);
	    trans.put((char)122, 1);
	    trans.put((char)121, 1);
	    trans.put((char)120, 1);
	    trans.put((char)119, 1);
	    trans.put((char)118, 1);
	    trans.put((char)117, 1);
	    trans.put((char)116, 1);
	    trans.put((char)115, 1);
	    trans.put((char)114, 1);
	    trans.put((char)113, 1);
	    trans.put((char)112, 1);
	    trans.put((char)111, 1);
	    trans.put((char)110, 1);
	    trans.put((char)109, 1);
	    trans.put((char)13, 9);
	    trans.put((char)108, 1);
	    trans.put((char)107, 1);
	    trans.put((char)106, 1);
	    trans.put((char)10, 10);
	    trans.put((char)105, 1);
	    trans.put((char)9, 8);
	    trans.put((char)104, 1);
	    trans.put((char)103, 1);
	    trans.put((char)102, 1);
	    trans.put((char)101, 1);
	    trans.put((char)100, 1);
	    trans.put((char)99, 1);
	    trans.put((char)98, 1);
	    trans.put((char)97, 1);
	    trans.put((char)95, 1);

	    // state 1
	    trans = new Hashtable<Character, Integer>();
	    DFA.put(1, trans);
	    trans.put((char)90, 19);
	    trans.put((char)89, 19);
	    trans.put((char)88, 19);
	    trans.put((char)87, 19);
	    trans.put((char)86, 19);
	    trans.put((char)85, 19);
	    trans.put((char)84, 19);
	    trans.put((char)83, 19);
	    trans.put((char)82, 19);
	    trans.put((char)81, 19);
	    trans.put((char)80, 19);
	    trans.put((char)79, 19);
	    trans.put((char)78, 19);
	    trans.put((char)77, 19);
	    trans.put((char)76, 19);
	    trans.put((char)75, 19);
	    trans.put((char)74, 19);
	    trans.put((char)73, 19);
	    trans.put((char)72, 19);
	    trans.put((char)71, 19);
	    trans.put((char)70, 19);
	    trans.put((char)69, 19);
	    trans.put((char)68, 19);
	    trans.put((char)67, 19);
	    trans.put((char)66, 19);
	    trans.put((char)65, 19);
	    trans.put((char)57, 18);
	    trans.put((char)56, 18);
	    trans.put((char)55, 18);
	    trans.put((char)54, 18);
	    trans.put((char)53, 18);
	    trans.put((char)52, 18);
	    trans.put((char)51, 18);
	    trans.put((char)50, 18);
	    trans.put((char)49, 18);
	    trans.put((char)48, 18);
	    trans.put((char)122, 19);
	    trans.put((char)121, 19);
	    trans.put((char)120, 19);
	    trans.put((char)119, 19);
	    trans.put((char)118, 19);
	    trans.put((char)117, 19);
	    trans.put((char)116, 19);
	    trans.put((char)115, 19);
	    trans.put((char)114, 19);
	    trans.put((char)113, 19);
	    trans.put((char)112, 19);
	    trans.put((char)111, 19);
	    trans.put((char)110, 19);
	    trans.put((char)109, 19);
	    trans.put((char)108, 19);
	    trans.put((char)107, 19);
	    trans.put((char)106, 19);
	    trans.put((char)105, 19);
	    trans.put((char)104, 19);
	    trans.put((char)103, 19);
	    trans.put((char)102, 19);
	    trans.put((char)101, 19);
	    trans.put((char)100, 19);
	    trans.put((char)99, 19);
	    trans.put((char)98, 19);
	    trans.put((char)97, 19);
	    trans.put((char)95, 19);

	    // state 2
	    trans = new Hashtable<Character, Integer>();
	    DFA.put(2, trans);

	    // state 3
	    trans = new Hashtable<Character, Integer>();
	    DFA.put(3, trans);

	    // state 4
	    trans = new Hashtable<Character, Integer>();
	    DFA.put(4, trans);
	    trans.put((char)62, 15);

	    // state 5
	    trans = new Hashtable<Character, Integer>();
	    DFA.put(5, trans);

	    // state 6
	    trans = new Hashtable<Character, Integer>();
	    DFA.put(6, trans);
	    trans.put((char)62, 14);

	    // state 7
	    trans = new Hashtable<Character, Integer>();
	    DFA.put(7, trans);
	    trans.put((char)10, 13);
	    trans.put((char)4, 12);
	    trans.put((char)13, 13);

	    // state 8
	    trans = new Hashtable<Character, Integer>();
	    DFA.put(8, trans);

	    // state 9
	    trans = new Hashtable<Character, Integer>();
	    DFA.put(9, trans);
	    trans.put((char)10, 11);

	    // state 10
	    trans = new Hashtable<Character, Integer>();
	    DFA.put(10, trans);

	    // state 11
	    trans = new Hashtable<Character, Integer>();
	    DFA.put(11, trans);

	    // state 12
	    trans = new Hashtable<Character, Integer>();
	    DFA.put(12, trans);
	    trans.put((char)10, 13);
	    trans.put((char)4, 12);
	    trans.put((char)13, 13);

	    // state 13
	    trans = new Hashtable<Character, Integer>();
	    DFA.put(13, trans);

	    // state 14
	    trans = new Hashtable<Character, Integer>();
	    DFA.put(14, trans);

	    // state 15
	    trans = new Hashtable<Character, Integer>();
	    DFA.put(15, trans);
	    trans.put((char)49, 16);

	    // state 16
	    trans = new Hashtable<Character, Integer>();
	    DFA.put(16, trans);
	    trans.put((char)93, 17);

	    // state 17
	    trans = new Hashtable<Character, Integer>();
	    DFA.put(17, trans);

	    // state 18
	    trans = new Hashtable<Character, Integer>();
	    DFA.put(18, trans);
	    trans.put((char)90, 19);
	    trans.put((char)89, 19);
	    trans.put((char)88, 19);
	    trans.put((char)87, 19);
	    trans.put((char)86, 19);
	    trans.put((char)85, 19);
	    trans.put((char)84, 19);
	    trans.put((char)83, 19);
	    trans.put((char)82, 19);
	    trans.put((char)81, 19);
	    trans.put((char)80, 19);
	    trans.put((char)79, 19);
	    trans.put((char)78, 19);
	    trans.put((char)77, 19);
	    trans.put((char)76, 19);
	    trans.put((char)75, 19);
	    trans.put((char)74, 19);
	    trans.put((char)73, 19);
	    trans.put((char)72, 19);
	    trans.put((char)71, 19);
	    trans.put((char)70, 19);
	    trans.put((char)69, 19);
	    trans.put((char)68, 19);
	    trans.put((char)67, 19);
	    trans.put((char)66, 19);
	    trans.put((char)65, 19);
	    trans.put((char)57, 18);
	    trans.put((char)56, 18);
	    trans.put((char)55, 18);
	    trans.put((char)54, 18);
	    trans.put((char)53, 18);
	    trans.put((char)52, 18);
	    trans.put((char)51, 18);
	    trans.put((char)50, 18);
	    trans.put((char)49, 18);
	    trans.put((char)48, 18);
	    trans.put((char)122, 19);
	    trans.put((char)121, 19);
	    trans.put((char)120, 19);
	    trans.put((char)119, 19);
	    trans.put((char)118, 19);
	    trans.put((char)117, 19);
	    trans.put((char)116, 19);
	    trans.put((char)115, 19);
	    trans.put((char)114, 19);
	    trans.put((char)113, 19);
	    trans.put((char)112, 19);
	    trans.put((char)111, 19);
	    trans.put((char)110, 19);
	    trans.put((char)109, 19);
	    trans.put((char)108, 19);
	    trans.put((char)107, 19);
	    trans.put((char)106, 19);
	    trans.put((char)105, 19);
	    trans.put((char)104, 19);
	    trans.put((char)103, 19);
	    trans.put((char)102, 19);
	    trans.put((char)101, 19);
	    trans.put((char)100, 19);
	    trans.put((char)99, 19);
	    trans.put((char)98, 19);
	    trans.put((char)97, 19);
	    trans.put((char)95, 19);

	    // state 19
	    trans = new Hashtable<Character, Integer>();
	    DFA.put(19, trans);
	    trans.put((char)90, 19);
	    trans.put((char)89, 19);
	    trans.put((char)88, 19);
	    trans.put((char)87, 19);
	    trans.put((char)86, 19);
	    trans.put((char)85, 19);
	    trans.put((char)84, 19);
	    trans.put((char)83, 19);
	    trans.put((char)82, 19);
	    trans.put((char)81, 19);
	    trans.put((char)80, 19);
	    trans.put((char)79, 19);
	    trans.put((char)78, 19);
	    trans.put((char)77, 19);
	    trans.put((char)76, 19);
	    trans.put((char)75, 19);
	    trans.put((char)74, 19);
	    trans.put((char)73, 19);
	    trans.put((char)72, 19);
	    trans.put((char)71, 19);
	    trans.put((char)70, 19);
	    trans.put((char)69, 19);
	    trans.put((char)68, 19);
	    trans.put((char)67, 19);
	    trans.put((char)66, 19);
	    trans.put((char)65, 19);
	    trans.put((char)57, 18);
	    trans.put((char)56, 18);
	    trans.put((char)55, 18);
	    trans.put((char)54, 18);
	    trans.put((char)53, 18);
	    trans.put((char)52, 18);
	    trans.put((char)51, 18);
	    trans.put((char)50, 18);
	    trans.put((char)49, 18);
	    trans.put((char)48, 18);
	    trans.put((char)122, 19);
	    trans.put((char)121, 19);
	    trans.put((char)120, 19);
	    trans.put((char)119, 19);
	    trans.put((char)118, 19);
	    trans.put((char)117, 19);
	    trans.put((char)116, 19);
	    trans.put((char)115, 19);
	    trans.put((char)114, 19);
	    trans.put((char)113, 19);
	    trans.put((char)112, 19);
	    trans.put((char)111, 19);
	    trans.put((char)110, 19);
	    trans.put((char)109, 19);
	    trans.put((char)108, 19);
	    trans.put((char)107, 19);
	    trans.put((char)106, 19);
	    trans.put((char)105, 19);
	    trans.put((char)104, 19);
	    trans.put((char)103, 19);
	    trans.put((char)102, 19);
	    trans.put((char)101, 19);
	    trans.put((char)100, 19);
	    trans.put((char)99, 19);
	    trans.put((char)98, 19);
	    trans.put((char)97, 19);
	    trans.put((char)95, 19);

	    accepting.put(19, "id");
	    accepting.put(18, "id");
	    accepting.put(17, "multi_child");
	    accepting.put(14, "sep");
	    accepting.put(12, "skip");
	    accepting.put(11, "eol");
	    accepting.put(10, "eol");
	    accepting.put(9, "eol");
	    accepting.put(8, "skip");
	    accepting.put(7, "skip");
	    accepting.put(5, "lparen");
	    accepting.put(3, "rparen");
	    accepting.put(2, "op");
	    accepting.put(1, "id");
	  } //end buildDFA()

	  private Token createToken(String name, String value, int lineNumber) {
	    if ( name.equals("id") ) return new Token(ID_TOKEN, name, value, lineNumber);
	    if ( name.equals("op") ) return new Token(OP_TOKEN, name, value, lineNumber);
	    if ( name.equals("eol") ) return new Token(EOL_TOKEN, name, value, lineNumber);
	    if ( name.equals("rparen") ) return new Token(RPAREN_TOKEN, name, value, lineNumber);
	    if ( name.equals("multi_child") ) return new Token(MULTI_CHILD_TOKEN, name, value, lineNumber);
	    if ( name.equals("skip") ) return new Token(SKIP_TOKEN, name, value, lineNumber);
	    if ( name.equals("lparen") ) return new Token(LPAREN_TOKEN, name, value, lineNumber);
	    if ( name.equals("sep") ) return new Token(SEP_TOKEN, name, value, lineNumber);
	    if ( name.equals("eof") ) return new Token(EOF_TOKEN, name, value, lineNumber);
	    throw new RuntimeException("Cannot create token, unknown token name: " + name);
	  }

	} // end Tokenizer