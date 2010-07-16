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
	    trans.put((char)90, 18);
	    trans.put((char)89, 18);
	    trans.put((char)88, 18);
	    trans.put((char)87, 18);
	    trans.put((char)86, 18);
	    trans.put((char)85, 18);
	    trans.put((char)84, 18);
	    trans.put((char)83, 18);
	    trans.put((char)82, 18);
	    trans.put((char)81, 18);
	    trans.put((char)80, 18);
	    trans.put((char)79, 18);
	    trans.put((char)78, 18);
	    trans.put((char)77, 18);
	    trans.put((char)76, 18);
	    trans.put((char)75, 18);
	    trans.put((char)74, 18);
	    trans.put((char)73, 18);
	    trans.put((char)72, 18);
	    trans.put((char)71, 18);
	    trans.put((char)70, 18);
	    trans.put((char)69, 18);
	    trans.put((char)68, 18);
	    trans.put((char)67, 18);
	    trans.put((char)66, 18);
	    trans.put((char)65, 18);
	    trans.put((char)57, 17);
	    trans.put((char)56, 17);
	    trans.put((char)55, 17);
	    trans.put((char)54, 17);
	    trans.put((char)53, 17);
	    trans.put((char)52, 17);
	    trans.put((char)51, 17);
	    trans.put((char)50, 17);
	    trans.put((char)49, 17);
	    trans.put((char)48, 17);
	    trans.put((char)122, 18);
	    trans.put((char)121, 18);
	    trans.put((char)120, 18);
	    trans.put((char)119, 18);
	    trans.put((char)118, 18);
	    trans.put((char)117, 18);
	    trans.put((char)116, 18);
	    trans.put((char)115, 18);
	    trans.put((char)114, 18);
	    trans.put((char)113, 18);
	    trans.put((char)112, 18);
	    trans.put((char)111, 18);
	    trans.put((char)110, 18);
	    trans.put((char)109, 18);
	    trans.put((char)108, 18);
	    trans.put((char)107, 18);
	    trans.put((char)106, 18);
	    trans.put((char)105, 18);
	    trans.put((char)104, 18);
	    trans.put((char)103, 18);
	    trans.put((char)102, 18);
	    trans.put((char)101, 18);
	    trans.put((char)100, 18);
	    trans.put((char)99, 18);
	    trans.put((char)98, 18);
	    trans.put((char)97, 18);
	    trans.put((char)95, 18);

	    // state 2
	    trans = new Hashtable<Character, Integer>();
	    DFA.put(2, trans);

	    // state 3
	    trans = new Hashtable<Character, Integer>();
	    DFA.put(3, trans);

	    // state 4
	    trans = new Hashtable<Character, Integer>();
	    DFA.put(4, trans);
	    trans.put((char)62, 14);

	    // state 5
	    trans = new Hashtable<Character, Integer>();
	    DFA.put(5, trans);

	    // state 6
	    trans = new Hashtable<Character, Integer>();
	    DFA.put(6, trans);
	    trans.put((char)62, 13);

	    // state 7
	    trans = new Hashtable<Character, Integer>();
	    DFA.put(7, trans);
	    trans.put((char)255, 12);
	    trans.put((char)254, 12);
	    trans.put((char)253, 12);
	    trans.put((char)252, 12);
	    trans.put((char)251, 12);
	    trans.put((char)250, 12);
	    trans.put((char)249, 12);
	    trans.put((char)248, 12);
	    trans.put((char)247, 12);
	    trans.put((char)246, 12);
	    trans.put((char)245, 12);
	    trans.put((char)244, 12);
	    trans.put((char)243, 12);
	    trans.put((char)242, 12);
	    trans.put((char)241, 12);
	    trans.put((char)240, 12);
	    trans.put((char)239, 12);
	    trans.put((char)238, 12);
	    trans.put((char)237, 12);
	    trans.put((char)236, 12);
	    trans.put((char)235, 12);
	    trans.put((char)234, 12);
	    trans.put((char)233, 12);
	    trans.put((char)232, 12);
	    trans.put((char)231, 12);
	    trans.put((char)230, 12);
	    trans.put((char)229, 12);
	    trans.put((char)228, 12);
	    trans.put((char)227, 12);
	    trans.put((char)226, 12);
	    trans.put((char)225, 12);
	    trans.put((char)224, 12);
	    trans.put((char)223, 12);
	    trans.put((char)222, 12);
	    trans.put((char)221, 12);
	    trans.put((char)220, 12);
	    trans.put((char)219, 12);
	    trans.put((char)218, 12);
	    trans.put((char)217, 12);
	    trans.put((char)216, 12);
	    trans.put((char)215, 12);
	    trans.put((char)214, 12);
	    trans.put((char)213, 12);
	    trans.put((char)212, 12);
	    trans.put((char)211, 12);
	    trans.put((char)210, 12);
	    trans.put((char)209, 12);
	    trans.put((char)208, 12);
	    trans.put((char)207, 12);
	    trans.put((char)206, 12);
	    trans.put((char)205, 12);
	    trans.put((char)204, 12);
	    trans.put((char)203, 12);
	    trans.put((char)202, 12);
	    trans.put((char)201, 12);
	    trans.put((char)200, 12);
	    trans.put((char)199, 12);
	    trans.put((char)198, 12);
	    trans.put((char)197, 12);
	    trans.put((char)196, 12);
	    trans.put((char)195, 12);
	    trans.put((char)194, 12);
	    trans.put((char)193, 12);
	    trans.put((char)192, 12);
	    trans.put((char)191, 12);
	    trans.put((char)190, 12);
	    trans.put((char)189, 12);
	    trans.put((char)188, 12);
	    trans.put((char)187, 12);
	    trans.put((char)186, 12);
	    trans.put((char)185, 12);
	    trans.put((char)184, 12);
	    trans.put((char)183, 12);
	    trans.put((char)182, 12);
	    trans.put((char)181, 12);
	    trans.put((char)180, 12);
	    trans.put((char)179, 12);
	    trans.put((char)178, 12);
	    trans.put((char)177, 12);
	    trans.put((char)176, 12);
	    trans.put((char)175, 12);
	    trans.put((char)174, 12);
	    trans.put((char)173, 12);
	    trans.put((char)172, 12);
	    trans.put((char)171, 12);
	    trans.put((char)170, 12);
	    trans.put((char)169, 12);
	    trans.put((char)168, 12);
	    trans.put((char)167, 12);
	    trans.put((char)166, 12);
	    trans.put((char)165, 12);
	    trans.put((char)164, 12);
	    trans.put((char)163, 12);
	    trans.put((char)162, 12);
	    trans.put((char)161, 12);
	    trans.put((char)160, 12);
	    trans.put((char)159, 12);
	    trans.put((char)158, 12);
	    trans.put((char)157, 12);
	    trans.put((char)156, 12);
	    trans.put((char)155, 12);
	    trans.put((char)154, 12);
	    trans.put((char)153, 12);
	    trans.put((char)152, 12);
	    trans.put((char)151, 12);
	    trans.put((char)150, 12);
	    trans.put((char)149, 12);
	    trans.put((char)148, 12);
	    trans.put((char)147, 12);
	    trans.put((char)146, 12);
	    trans.put((char)145, 12);
	    trans.put((char)144, 12);
	    trans.put((char)143, 12);
	    trans.put((char)142, 12);
	    trans.put((char)141, 12);
	    trans.put((char)140, 12);
	    trans.put((char)139, 12);
	    trans.put((char)138, 12);
	    trans.put((char)137, 12);
	    trans.put((char)136, 12);
	    trans.put((char)135, 12);
	    trans.put((char)134, 12);
	    trans.put((char)133, 12);
	    trans.put((char)132, 12);
	    trans.put((char)131, 12);
	    trans.put((char)130, 12);
	    trans.put((char)129, 12);
	    trans.put((char)128, 12);
	    trans.put((char)127, 12);
	    trans.put((char)126, 12);
	    trans.put((char)125, 12);
	    trans.put((char)124, 12);
	    trans.put((char)123, 12);
	    trans.put((char)122, 12);
	    trans.put((char)121, 12);
	    trans.put((char)120, 12);
	    trans.put((char)119, 12);
	    trans.put((char)118, 12);
	    trans.put((char)117, 12);
	    trans.put((char)116, 12);
	    trans.put((char)115, 12);
	    trans.put((char)114, 12);
	    trans.put((char)113, 12);
	    trans.put((char)112, 12);
	    trans.put((char)111, 12);
	    trans.put((char)110, 12);
	    trans.put((char)109, 12);
	    trans.put((char)108, 12);
	    trans.put((char)107, 12);
	    trans.put((char)106, 12);
	    trans.put((char)105, 12);
	    trans.put((char)104, 12);
	    trans.put((char)103, 12);
	    trans.put((char)102, 12);
	    trans.put((char)101, 12);
	    trans.put((char)100, 12);
	    trans.put((char)99, 12);
	    trans.put((char)98, 12);
	    trans.put((char)97, 12);
	    trans.put((char)96, 12);
	    trans.put((char)95, 12);
	    trans.put((char)94, 12);
	    trans.put((char)93, 12);
	    trans.put((char)92, 12);
	    trans.put((char)91, 12);
	    trans.put((char)90, 12);
	    trans.put((char)89, 12);
	    trans.put((char)88, 12);
	    trans.put((char)87, 12);
	    trans.put((char)86, 12);
	    trans.put((char)85, 12);
	    trans.put((char)84, 12);
	    trans.put((char)83, 12);
	    trans.put((char)82, 12);
	    trans.put((char)81, 12);
	    trans.put((char)80, 12);
	    trans.put((char)79, 12);
	    trans.put((char)78, 12);
	    trans.put((char)77, 12);
	    trans.put((char)76, 12);
	    trans.put((char)75, 12);
	    trans.put((char)74, 12);
	    trans.put((char)73, 12);
	    trans.put((char)72, 12);
	    trans.put((char)71, 12);
	    trans.put((char)70, 12);
	    trans.put((char)69, 12);
	    trans.put((char)68, 12);
	    trans.put((char)67, 12);
	    trans.put((char)66, 12);
	    trans.put((char)65, 12);
	    trans.put((char)64, 12);
	    trans.put((char)63, 12);
	    trans.put((char)62, 12);
	    trans.put((char)61, 12);
	    trans.put((char)60, 12);
	    trans.put((char)59, 12);
	    trans.put((char)58, 12);
	    trans.put((char)57, 12);
	    trans.put((char)56, 12);
	    trans.put((char)55, 12);
	    trans.put((char)54, 12);
	    trans.put((char)53, 12);
	    trans.put((char)52, 12);
	    trans.put((char)51, 12);
	    trans.put((char)50, 12);
	    trans.put((char)49, 12);
	    trans.put((char)48, 12);
	    trans.put((char)47, 12);
	    trans.put((char)46, 12);
	    trans.put((char)45, 12);
	    trans.put((char)44, 12);
	    trans.put((char)43, 12);
	    trans.put((char)42, 12);
	    trans.put((char)41, 12);
	    trans.put((char)40, 12);
	    trans.put((char)39, 12);
	    trans.put((char)38, 12);
	    trans.put((char)37, 12);
	    trans.put((char)36, 12);
	    trans.put((char)35, 12);
	    trans.put((char)34, 12);
	    trans.put((char)33, 12);
	    trans.put((char)32, 12);
	    trans.put((char)31, 12);
	    trans.put((char)30, 12);
	    trans.put((char)29, 12);
	    trans.put((char)28, 12);
	    trans.put((char)27, 12);
	    trans.put((char)26, 12);
	    trans.put((char)25, 12);
	    trans.put((char)24, 12);
	    trans.put((char)23, 12);
	    trans.put((char)22, 12);
	    trans.put((char)21, 12);
	    trans.put((char)20, 12);
	    trans.put((char)19, 12);
	    trans.put((char)18, 12);
	    trans.put((char)17, 12);
	    trans.put((char)16, 12);
	    trans.put((char)15, 12);
	    trans.put((char)14, 12);
	    trans.put((char)13, 12);
	    trans.put((char)12, 12);
	    trans.put((char)11, 12);
	    trans.put((char)10, 12);
	    trans.put((char)9, 12);
	    trans.put((char)8, 12);
	    trans.put((char)7, 12);
	    trans.put((char)6, 12);
	    trans.put((char)5, 12);
	    trans.put((char)4, 12);
	    trans.put((char)3, 12);
	    trans.put((char)2, 12);
	    trans.put((char)1, 12);

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
	    trans.put((char)255, 12);
	    trans.put((char)254, 12);
	    trans.put((char)253, 12);
	    trans.put((char)252, 12);
	    trans.put((char)251, 12);
	    trans.put((char)250, 12);
	    trans.put((char)249, 12);
	    trans.put((char)248, 12);
	    trans.put((char)247, 12);
	    trans.put((char)246, 12);
	    trans.put((char)245, 12);
	    trans.put((char)244, 12);
	    trans.put((char)243, 12);
	    trans.put((char)242, 12);
	    trans.put((char)241, 12);
	    trans.put((char)240, 12);
	    trans.put((char)239, 12);
	    trans.put((char)238, 12);
	    trans.put((char)237, 12);
	    trans.put((char)236, 12);
	    trans.put((char)235, 12);
	    trans.put((char)234, 12);
	    trans.put((char)233, 12);
	    trans.put((char)232, 12);
	    trans.put((char)231, 12);
	    trans.put((char)230, 12);
	    trans.put((char)229, 12);
	    trans.put((char)228, 12);
	    trans.put((char)227, 12);
	    trans.put((char)226, 12);
	    trans.put((char)225, 12);
	    trans.put((char)224, 12);
	    trans.put((char)223, 12);
	    trans.put((char)222, 12);
	    trans.put((char)221, 12);
	    trans.put((char)220, 12);
	    trans.put((char)219, 12);
	    trans.put((char)218, 12);
	    trans.put((char)217, 12);
	    trans.put((char)216, 12);
	    trans.put((char)215, 12);
	    trans.put((char)214, 12);
	    trans.put((char)213, 12);
	    trans.put((char)212, 12);
	    trans.put((char)211, 12);
	    trans.put((char)210, 12);
	    trans.put((char)209, 12);
	    trans.put((char)208, 12);
	    trans.put((char)207, 12);
	    trans.put((char)206, 12);
	    trans.put((char)205, 12);
	    trans.put((char)204, 12);
	    trans.put((char)203, 12);
	    trans.put((char)202, 12);
	    trans.put((char)201, 12);
	    trans.put((char)200, 12);
	    trans.put((char)199, 12);
	    trans.put((char)198, 12);
	    trans.put((char)197, 12);
	    trans.put((char)196, 12);
	    trans.put((char)195, 12);
	    trans.put((char)194, 12);
	    trans.put((char)193, 12);
	    trans.put((char)192, 12);
	    trans.put((char)191, 12);
	    trans.put((char)190, 12);
	    trans.put((char)189, 12);
	    trans.put((char)188, 12);
	    trans.put((char)187, 12);
	    trans.put((char)186, 12);
	    trans.put((char)185, 12);
	    trans.put((char)184, 12);
	    trans.put((char)183, 12);
	    trans.put((char)182, 12);
	    trans.put((char)181, 12);
	    trans.put((char)180, 12);
	    trans.put((char)179, 12);
	    trans.put((char)178, 12);
	    trans.put((char)177, 12);
	    trans.put((char)176, 12);
	    trans.put((char)175, 12);
	    trans.put((char)174, 12);
	    trans.put((char)173, 12);
	    trans.put((char)172, 12);
	    trans.put((char)171, 12);
	    trans.put((char)170, 12);
	    trans.put((char)169, 12);
	    trans.put((char)168, 12);
	    trans.put((char)167, 12);
	    trans.put((char)166, 12);
	    trans.put((char)165, 12);
	    trans.put((char)164, 12);
	    trans.put((char)163, 12);
	    trans.put((char)162, 12);
	    trans.put((char)161, 12);
	    trans.put((char)160, 12);
	    trans.put((char)159, 12);
	    trans.put((char)158, 12);
	    trans.put((char)157, 12);
	    trans.put((char)156, 12);
	    trans.put((char)155, 12);
	    trans.put((char)154, 12);
	    trans.put((char)153, 12);
	    trans.put((char)152, 12);
	    trans.put((char)151, 12);
	    trans.put((char)150, 12);
	    trans.put((char)149, 12);
	    trans.put((char)148, 12);
	    trans.put((char)147, 12);
	    trans.put((char)146, 12);
	    trans.put((char)145, 12);
	    trans.put((char)144, 12);
	    trans.put((char)143, 12);
	    trans.put((char)142, 12);
	    trans.put((char)141, 12);
	    trans.put((char)140, 12);
	    trans.put((char)139, 12);
	    trans.put((char)138, 12);
	    trans.put((char)137, 12);
	    trans.put((char)136, 12);
	    trans.put((char)135, 12);
	    trans.put((char)134, 12);
	    trans.put((char)133, 12);
	    trans.put((char)132, 12);
	    trans.put((char)131, 12);
	    trans.put((char)130, 12);
	    trans.put((char)129, 12);
	    trans.put((char)128, 12);
	    trans.put((char)127, 12);
	    trans.put((char)126, 12);
	    trans.put((char)125, 12);
	    trans.put((char)124, 12);
	    trans.put((char)123, 12);
	    trans.put((char)122, 12);
	    trans.put((char)121, 12);
	    trans.put((char)120, 12);
	    trans.put((char)119, 12);
	    trans.put((char)118, 12);
	    trans.put((char)117, 12);
	    trans.put((char)116, 12);
	    trans.put((char)115, 12);
	    trans.put((char)114, 12);
	    trans.put((char)113, 12);
	    trans.put((char)112, 12);
	    trans.put((char)111, 12);
	    trans.put((char)110, 12);
	    trans.put((char)109, 12);
	    trans.put((char)108, 12);
	    trans.put((char)107, 12);
	    trans.put((char)106, 12);
	    trans.put((char)105, 12);
	    trans.put((char)104, 12);
	    trans.put((char)103, 12);
	    trans.put((char)102, 12);
	    trans.put((char)101, 12);
	    trans.put((char)100, 12);
	    trans.put((char)99, 12);
	    trans.put((char)98, 12);
	    trans.put((char)97, 12);
	    trans.put((char)96, 12);
	    trans.put((char)95, 12);
	    trans.put((char)94, 12);
	    trans.put((char)93, 12);
	    trans.put((char)92, 12);
	    trans.put((char)91, 12);
	    trans.put((char)90, 12);
	    trans.put((char)89, 12);
	    trans.put((char)88, 12);
	    trans.put((char)87, 12);
	    trans.put((char)86, 12);
	    trans.put((char)85, 12);
	    trans.put((char)84, 12);
	    trans.put((char)83, 12);
	    trans.put((char)82, 12);
	    trans.put((char)81, 12);
	    trans.put((char)80, 12);
	    trans.put((char)79, 12);
	    trans.put((char)78, 12);
	    trans.put((char)77, 12);
	    trans.put((char)76, 12);
	    trans.put((char)75, 12);
	    trans.put((char)74, 12);
	    trans.put((char)73, 12);
	    trans.put((char)72, 12);
	    trans.put((char)71, 12);
	    trans.put((char)70, 12);
	    trans.put((char)69, 12);
	    trans.put((char)68, 12);
	    trans.put((char)67, 12);
	    trans.put((char)66, 12);
	    trans.put((char)65, 12);
	    trans.put((char)64, 12);
	    trans.put((char)63, 12);
	    trans.put((char)62, 12);
	    trans.put((char)61, 12);
	    trans.put((char)60, 12);
	    trans.put((char)59, 12);
	    trans.put((char)58, 12);
	    trans.put((char)57, 12);
	    trans.put((char)56, 12);
	    trans.put((char)55, 12);
	    trans.put((char)54, 12);
	    trans.put((char)53, 12);
	    trans.put((char)52, 12);
	    trans.put((char)51, 12);
	    trans.put((char)50, 12);
	    trans.put((char)49, 12);
	    trans.put((char)48, 12);
	    trans.put((char)47, 12);
	    trans.put((char)46, 12);
	    trans.put((char)45, 12);
	    trans.put((char)44, 12);
	    trans.put((char)43, 12);
	    trans.put((char)42, 12);
	    trans.put((char)41, 12);
	    trans.put((char)40, 12);
	    trans.put((char)39, 12);
	    trans.put((char)38, 12);
	    trans.put((char)37, 12);
	    trans.put((char)36, 12);
	    trans.put((char)35, 12);
	    trans.put((char)34, 12);
	    trans.put((char)33, 12);
	    trans.put((char)32, 12);
	    trans.put((char)31, 12);
	    trans.put((char)30, 12);
	    trans.put((char)29, 12);
	    trans.put((char)28, 12);
	    trans.put((char)27, 12);
	    trans.put((char)26, 12);
	    trans.put((char)25, 12);
	    trans.put((char)24, 12);
	    trans.put((char)23, 12);
	    trans.put((char)22, 12);
	    trans.put((char)21, 12);
	    trans.put((char)20, 12);
	    trans.put((char)19, 12);
	    trans.put((char)18, 12);
	    trans.put((char)17, 12);
	    trans.put((char)16, 12);
	    trans.put((char)15, 12);
	    trans.put((char)14, 12);
	    trans.put((char)13, 12);
	    trans.put((char)12, 12);
	    trans.put((char)11, 12);
	    trans.put((char)10, 12);
	    trans.put((char)9, 12);
	    trans.put((char)8, 12);
	    trans.put((char)7, 12);
	    trans.put((char)6, 12);
	    trans.put((char)5, 12);
	    trans.put((char)4, 12);
	    trans.put((char)3, 12);
	    trans.put((char)2, 12);
	    trans.put((char)1, 12);

	    // state 13
	    trans = new Hashtable<Character, Integer>();
	    DFA.put(13, trans);

	    // state 14
	    trans = new Hashtable<Character, Integer>();
	    DFA.put(14, trans);
	    trans.put((char)49, 15);

	    // state 15
	    trans = new Hashtable<Character, Integer>();
	    DFA.put(15, trans);
	    trans.put((char)93, 16);

	    // state 16
	    trans = new Hashtable<Character, Integer>();
	    DFA.put(16, trans);

	    // state 17
	    trans = new Hashtable<Character, Integer>();
	    DFA.put(17, trans);
	    trans.put((char)90, 18);
	    trans.put((char)89, 18);
	    trans.put((char)88, 18);
	    trans.put((char)87, 18);
	    trans.put((char)86, 18);
	    trans.put((char)85, 18);
	    trans.put((char)84, 18);
	    trans.put((char)83, 18);
	    trans.put((char)82, 18);
	    trans.put((char)81, 18);
	    trans.put((char)80, 18);
	    trans.put((char)79, 18);
	    trans.put((char)78, 18);
	    trans.put((char)77, 18);
	    trans.put((char)76, 18);
	    trans.put((char)75, 18);
	    trans.put((char)74, 18);
	    trans.put((char)73, 18);
	    trans.put((char)72, 18);
	    trans.put((char)71, 18);
	    trans.put((char)70, 18);
	    trans.put((char)69, 18);
	    trans.put((char)68, 18);
	    trans.put((char)67, 18);
	    trans.put((char)66, 18);
	    trans.put((char)65, 18);
	    trans.put((char)57, 17);
	    trans.put((char)56, 17);
	    trans.put((char)55, 17);
	    trans.put((char)54, 17);
	    trans.put((char)53, 17);
	    trans.put((char)52, 17);
	    trans.put((char)51, 17);
	    trans.put((char)50, 17);
	    trans.put((char)49, 17);
	    trans.put((char)48, 17);
	    trans.put((char)122, 18);
	    trans.put((char)121, 18);
	    trans.put((char)120, 18);
	    trans.put((char)119, 18);
	    trans.put((char)118, 18);
	    trans.put((char)117, 18);
	    trans.put((char)116, 18);
	    trans.put((char)115, 18);
	    trans.put((char)114, 18);
	    trans.put((char)113, 18);
	    trans.put((char)112, 18);
	    trans.put((char)111, 18);
	    trans.put((char)110, 18);
	    trans.put((char)109, 18);
	    trans.put((char)108, 18);
	    trans.put((char)107, 18);
	    trans.put((char)106, 18);
	    trans.put((char)105, 18);
	    trans.put((char)104, 18);
	    trans.put((char)103, 18);
	    trans.put((char)102, 18);
	    trans.put((char)101, 18);
	    trans.put((char)100, 18);
	    trans.put((char)99, 18);
	    trans.put((char)98, 18);
	    trans.put((char)97, 18);
	    trans.put((char)95, 18);

	    // state 18
	    trans = new Hashtable<Character, Integer>();
	    DFA.put(18, trans);
	    trans.put((char)90, 18);
	    trans.put((char)89, 18);
	    trans.put((char)88, 18);
	    trans.put((char)87, 18);
	    trans.put((char)86, 18);
	    trans.put((char)85, 18);
	    trans.put((char)84, 18);
	    trans.put((char)83, 18);
	    trans.put((char)82, 18);
	    trans.put((char)81, 18);
	    trans.put((char)80, 18);
	    trans.put((char)79, 18);
	    trans.put((char)78, 18);
	    trans.put((char)77, 18);
	    trans.put((char)76, 18);
	    trans.put((char)75, 18);
	    trans.put((char)74, 18);
	    trans.put((char)73, 18);
	    trans.put((char)72, 18);
	    trans.put((char)71, 18);
	    trans.put((char)70, 18);
	    trans.put((char)69, 18);
	    trans.put((char)68, 18);
	    trans.put((char)67, 18);
	    trans.put((char)66, 18);
	    trans.put((char)65, 18);
	    trans.put((char)57, 17);
	    trans.put((char)56, 17);
	    trans.put((char)55, 17);
	    trans.put((char)54, 17);
	    trans.put((char)53, 17);
	    trans.put((char)52, 17);
	    trans.put((char)51, 17);
	    trans.put((char)50, 17);
	    trans.put((char)49, 17);
	    trans.put((char)48, 17);
	    trans.put((char)122, 18);
	    trans.put((char)121, 18);
	    trans.put((char)120, 18);
	    trans.put((char)119, 18);
	    trans.put((char)118, 18);
	    trans.put((char)117, 18);
	    trans.put((char)116, 18);
	    trans.put((char)115, 18);
	    trans.put((char)114, 18);
	    trans.put((char)113, 18);
	    trans.put((char)112, 18);
	    trans.put((char)111, 18);
	    trans.put((char)110, 18);
	    trans.put((char)109, 18);
	    trans.put((char)108, 18);
	    trans.put((char)107, 18);
	    trans.put((char)106, 18);
	    trans.put((char)105, 18);
	    trans.put((char)104, 18);
	    trans.put((char)103, 18);
	    trans.put((char)102, 18);
	    trans.put((char)101, 18);
	    trans.put((char)100, 18);
	    trans.put((char)99, 18);
	    trans.put((char)98, 18);
	    trans.put((char)97, 18);
	    trans.put((char)95, 18);

	    accepting.put(18, "id");
	    accepting.put(17, "id");
	    accepting.put(16, "multi_child");
	    accepting.put(13, "sep");
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