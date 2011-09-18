import java.io.*;
import java.util.Hashtable;
import java.util.ArrayList;
import java.util.Stack;
import java.util.ListIterator;

/**
 * Tokenizer used to read the grammar definition file, 
 * created automatically with this project's tokenizer class creator
 * with the following token definitions
 * 
 * 		skip: \# [^\n\r]*
 * 		skip: [\s\t]
 * 
 * 		eol: (\n | \r | \r\n)
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
 * 		epsilon: \\0
 * 
 * 
 */
public class GrammarTokenizer implements iTokenizer {

  public static final int ID_TOKEN = 1; // :alpha: (:digit: | :alpha:)*
  public static final int OP_TOKEN = 2; // [\* \? \|]
  public static final int EOL_TOKEN = 3; // (\n | \r | \r\n)
  public static final int RPAREN_TOKEN = 4; // \)
  public static final int MULTI_CHILD_TOKEN = 5; // \[>1\]
  public static final int EPSILON_TOKEN = 6; // \\0
  public static final int LPAREN_TOKEN = 7; // \(
  public static final int SEP_TOKEN = 8; // ->
  public static final int EOF_TOKEN = -1;

  private static final char wildcard = 3;

  private LineNumberReader input;

  private Hashtable<Integer, Hashtable<Character, Integer>> DFA = new Hashtable<Integer, Hashtable<Character, Integer>>();

  private Hashtable<Integer, String> accepting = new Hashtable<Integer, String>();

  private ArrayList<Token> tokenHistory = new ArrayList<Token>();
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
        nextState = -1;
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

  private Token createToken(String name, String value, int lineNumber) {
    if ( name.equals("id") ) return new Token(ID_TOKEN, name, value, lineNumber);
    if ( name.equals("op") ) return new Token(OP_TOKEN, name, value, lineNumber);
    if ( name.equals("eol") ) return new Token(EOL_TOKEN, name, value, lineNumber);
    if ( name.equals("rparen") ) return new Token(RPAREN_TOKEN, name, value, lineNumber);
    if ( name.equals("multi_child") ) return new Token(MULTI_CHILD_TOKEN, name, value, lineNumber);
    if ( name.equals("epsilon") ) return new Token(EPSILON_TOKEN, name, value, lineNumber);
    if ( name.equals("lparen") ) return new Token(LPAREN_TOKEN, name, value, lineNumber);
    if ( name.equals("sep") ) return new Token(SEP_TOKEN, name, value, lineNumber);
    if ( name.equals("eof") ) return new Token(EOF_TOKEN, name, value, lineNumber);
    throw new RuntimeException("Cannot create token, unknown token name: " + name);
  }

  private void buildDFA() {
    buildState0();
    buildState1();
    buildState2();
    buildState3();
    buildState4();
    buildState5();
    buildState6();
    buildState7();
    buildState8();
    buildState9();
    buildState10();
    buildState11();
    buildState12();
    buildState13();
    buildState14();
    buildState15();
    buildState16();
    buildState17();
    buildState18();
    buildState19();
    buildState20();

    accepting.put(20, "id");
    accepting.put(19, "id");
    accepting.put(18, "multi_child");
    accepting.put(15, "epsilon");
    accepting.put(14, "sep");
    accepting.put(13, "skip");
    accepting.put(12, "eol");
    accepting.put(11, "eol");
    accepting.put(10, "eol");
    accepting.put(9, "skip");
    accepting.put(8, "skip");
    accepting.put(6, "lparen");
    accepting.put(3, "rparen");
    accepting.put(2, "op");
    accepting.put(1, "id");
  } // end buildDFA

  private void buildState0() {
    char[] tc = {92,91,90,89,88,87,86,85,84,83,82,81,80,79,78,77,76,75,74,73,72,71,70,69,68,67,66,65,63,45,42,41,40,35,32,124,122,121,120,119,118,117,116,115,114,113,112,111,110,109,13,108,107,106,10,105,9,104,103,102,101,100,99,98,97,95,};
    int[]  st = {5,4,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,2,7,2,3,6,8,9,2,1,1,1,1,1,1,1,1,1,1,1,1,1,1,10,1,1,1,11,1,9,1,1,1,1,1,1,1,1,1,};
    Hashtable<Character, Integer> trans = new Hashtable<Character, Integer>();
    for (int i = 0; i < tc.length; i++) trans.put(tc[i], st[i]);
    DFA.put(0, trans);
  } // end buildState0

  private void buildState1() {
    char[] tc = {90,89,88,87,86,85,84,83,82,81,80,79,78,77,76,75,74,73,72,71,70,69,68,67,66,65,57,56,55,54,53,52,51,50,49,48,122,121,120,119,118,117,116,115,114,113,112,111,110,109,108,107,106,105,104,103,102,101,100,99,98,97,95,};
    int[]  st = {20,20,20,20,20,20,20,20,20,20,20,20,20,20,20,20,20,20,20,20,20,20,20,20,20,20,19,19,19,19,19,19,19,19,19,19,20,20,20,20,20,20,20,20,20,20,20,20,20,20,20,20,20,20,20,20,20,20,20,20,20,20,20,};
    Hashtable<Character, Integer> trans = new Hashtable<Character, Integer>();
    for (int i = 0; i < tc.length; i++) trans.put(tc[i], st[i]);
    DFA.put(1, trans);
  } // end buildState1

  private void buildState2() {
    char[] tc = {};
    int[]  st = {};
    Hashtable<Character, Integer> trans = new Hashtable<Character, Integer>();
    for (int i = 0; i < tc.length; i++) trans.put(tc[i], st[i]);
    DFA.put(2, trans);
  } // end buildState2

  private void buildState3() {
    char[] tc = {};
    int[]  st = {};
    Hashtable<Character, Integer> trans = new Hashtable<Character, Integer>();
    for (int i = 0; i < tc.length; i++) trans.put(tc[i], st[i]);
    DFA.put(3, trans);
  } // end buildState3

  private void buildState4() {
    char[] tc = {62,};
    int[]  st = {16,};
    Hashtable<Character, Integer> trans = new Hashtable<Character, Integer>();
    for (int i = 0; i < tc.length; i++) trans.put(tc[i], st[i]);
    DFA.put(4, trans);
  } // end buildState4

  private void buildState5() {
    char[] tc = {48,};
    int[]  st = {15,};
    Hashtable<Character, Integer> trans = new Hashtable<Character, Integer>();
    for (int i = 0; i < tc.length; i++) trans.put(tc[i], st[i]);
    DFA.put(5, trans);
  } // end buildState5

  private void buildState6() {
    char[] tc = {};
    int[]  st = {};
    Hashtable<Character, Integer> trans = new Hashtable<Character, Integer>();
    for (int i = 0; i < tc.length; i++) trans.put(tc[i], st[i]);
    DFA.put(6, trans);
  } // end buildState6

  private void buildState7() {
    char[] tc = {62,};
    int[]  st = {14,};
    Hashtable<Character, Integer> trans = new Hashtable<Character, Integer>();
    for (int i = 0; i < tc.length; i++) trans.put(tc[i], st[i]);
    DFA.put(7, trans);
  } // end buildState7

  private void buildState8() {
    char[] tc = {255,254,253,252,251,250,249,248,247,246,245,244,243,242,241,240,239,238,237,236,235,234,233,232,231,230,229,228,227,226,225,224,223,222,221,220,219,218,217,216,215,214,213,212,211,210,209,208,207,206,205,204,203,202,201,200,199,198,197,196,195,194,193,192,191,190,189,188,187,186,185,184,183,182,181,180,179,178,177,176,175,174,173,172,171,170,169,168,167,166,165,164,163,162,161,160,159,158,157,156,155,154,153,152,151,150,149,148,147,146,145,144,143,142,141,140,139,138,137,136,135,134,133,132,131,130,129,128,127,126,125,124,123,122,121,120,119,118,117,116,115,114,113,112,111,110,109,108,107,106,105,104,103,102,101,100,99,98,97,96,95,94,93,92,91,90,89,88,87,86,85,84,83,82,81,80,79,78,77,76,75,74,73,72,71,70,69,68,67,66,65,64,63,62,61,60,59,58,57,56,55,54,53,52,51,50,49,48,47,46,45,44,43,42,41,40,39,38,37,36,35,34,33,32,31,30,29,28,27,26,25,24,23,22,21,20,19,18,17,16,15,14,12,11,9,};
    int[]  st = {13,13,13,13,13,13,13,13,13,13,13,13,13,13,13,13,13,13,13,13,13,13,13,13,13,13,13,13,13,13,13,13,13,13,13,13,13,13,13,13,13,13,13,13,13,13,13,13,13,13,13,13,13,13,13,13,13,13,13,13,13,13,13,13,13,13,13,13,13,13,13,13,13,13,13,13,13,13,13,13,13,13,13,13,13,13,13,13,13,13,13,13,13,13,13,13,13,13,13,13,13,13,13,13,13,13,13,13,13,13,13,13,13,13,13,13,13,13,13,13,13,13,13,13,13,13,13,13,13,13,13,13,13,13,13,13,13,13,13,13,13,13,13,13,13,13,13,13,13,13,13,13,13,13,13,13,13,13,13,13,13,13,13,13,13,13,13,13,13,13,13,13,13,13,13,13,13,13,13,13,13,13,13,13,13,13,13,13,13,13,13,13,13,13,13,13,13,13,13,13,13,13,13,13,13,13,13,13,13,13,13,13,13,13,13,13,13,13,13,13,13,13,13,13,13,13,13,13,13,13,13,13,13,13,13,13,13,13,13,13,13,13,13,13,13,};
    Hashtable<Character, Integer> trans = new Hashtable<Character, Integer>();
    for (int i = 0; i < tc.length; i++) trans.put(tc[i], st[i]);
    DFA.put(8, trans);
  } // end buildState8

  private void buildState9() {
    char[] tc = {};
    int[]  st = {};
    Hashtable<Character, Integer> trans = new Hashtable<Character, Integer>();
    for (int i = 0; i < tc.length; i++) trans.put(tc[i], st[i]);
    DFA.put(9, trans);
  } // end buildState9

  private void buildState10() {
    char[] tc = {10,};
    int[]  st = {12,};
    Hashtable<Character, Integer> trans = new Hashtable<Character, Integer>();
    for (int i = 0; i < tc.length; i++) trans.put(tc[i], st[i]);
    DFA.put(10, trans);
  } // end buildState10

  private void buildState11() {
    char[] tc = {};
    int[]  st = {};
    Hashtable<Character, Integer> trans = new Hashtable<Character, Integer>();
    for (int i = 0; i < tc.length; i++) trans.put(tc[i], st[i]);
    DFA.put(11, trans);
  } // end buildState11

  private void buildState12() {
    char[] tc = {};
    int[]  st = {};
    Hashtable<Character, Integer> trans = new Hashtable<Character, Integer>();
    for (int i = 0; i < tc.length; i++) trans.put(tc[i], st[i]);
    DFA.put(12, trans);
  } // end buildState12

  private void buildState13() {
    char[] tc = {255,254,253,252,251,250,249,248,247,246,245,244,243,242,241,240,239,238,237,236,235,234,233,232,231,230,229,228,227,226,225,224,223,222,221,220,219,218,217,216,215,214,213,212,211,210,209,208,207,206,205,204,203,202,201,200,199,198,197,196,195,194,193,192,191,190,189,188,187,186,185,184,183,182,181,180,179,178,177,176,175,174,173,172,171,170,169,168,167,166,165,164,163,162,161,160,159,158,157,156,155,154,153,152,151,150,149,148,147,146,145,144,143,142,141,140,139,138,137,136,135,134,133,132,131,130,129,128,127,126,125,124,123,122,121,120,119,118,117,116,115,114,113,112,111,110,109,108,107,106,105,104,103,102,101,100,99,98,97,96,95,94,93,92,91,90,89,88,87,86,85,84,83,82,81,80,79,78,77,76,75,74,73,72,71,70,69,68,67,66,65,64,63,62,61,60,59,58,57,56,55,54,53,52,51,50,49,48,47,46,45,44,43,42,41,40,39,38,37,36,35,34,33,32,31,30,29,28,27,26,25,24,23,22,21,20,19,18,17,16,15,14,12,11,9,};
    int[]  st = {13,13,13,13,13,13,13,13,13,13,13,13,13,13,13,13,13,13,13,13,13,13,13,13,13,13,13,13,13,13,13,13,13,13,13,13,13,13,13,13,13,13,13,13,13,13,13,13,13,13,13,13,13,13,13,13,13,13,13,13,13,13,13,13,13,13,13,13,13,13,13,13,13,13,13,13,13,13,13,13,13,13,13,13,13,13,13,13,13,13,13,13,13,13,13,13,13,13,13,13,13,13,13,13,13,13,13,13,13,13,13,13,13,13,13,13,13,13,13,13,13,13,13,13,13,13,13,13,13,13,13,13,13,13,13,13,13,13,13,13,13,13,13,13,13,13,13,13,13,13,13,13,13,13,13,13,13,13,13,13,13,13,13,13,13,13,13,13,13,13,13,13,13,13,13,13,13,13,13,13,13,13,13,13,13,13,13,13,13,13,13,13,13,13,13,13,13,13,13,13,13,13,13,13,13,13,13,13,13,13,13,13,13,13,13,13,13,13,13,13,13,13,13,13,13,13,13,13,13,13,13,13,13,13,13,13,13,13,13,13,13,13,13,13,13,};
    Hashtable<Character, Integer> trans = new Hashtable<Character, Integer>();
    for (int i = 0; i < tc.length; i++) trans.put(tc[i], st[i]);
    DFA.put(13, trans);
  } // end buildState13

  private void buildState14() {
    char[] tc = {};
    int[]  st = {};
    Hashtable<Character, Integer> trans = new Hashtable<Character, Integer>();
    for (int i = 0; i < tc.length; i++) trans.put(tc[i], st[i]);
    DFA.put(14, trans);
  } // end buildState14

  private void buildState15() {
    char[] tc = {};
    int[]  st = {};
    Hashtable<Character, Integer> trans = new Hashtable<Character, Integer>();
    for (int i = 0; i < tc.length; i++) trans.put(tc[i], st[i]);
    DFA.put(15, trans);
  } // end buildState15

  private void buildState16() {
    char[] tc = {49,};
    int[]  st = {17,};
    Hashtable<Character, Integer> trans = new Hashtable<Character, Integer>();
    for (int i = 0; i < tc.length; i++) trans.put(tc[i], st[i]);
    DFA.put(16, trans);
  } // end buildState16

  private void buildState17() {
    char[] tc = {93,};
    int[]  st = {18,};
    Hashtable<Character, Integer> trans = new Hashtable<Character, Integer>();
    for (int i = 0; i < tc.length; i++) trans.put(tc[i], st[i]);
    DFA.put(17, trans);
  } // end buildState17

  private void buildState18() {
    char[] tc = {};
    int[]  st = {};
    Hashtable<Character, Integer> trans = new Hashtable<Character, Integer>();
    for (int i = 0; i < tc.length; i++) trans.put(tc[i], st[i]);
    DFA.put(18, trans);
  } // end buildState18

  private void buildState19() {
    char[] tc = {90,89,88,87,86,85,84,83,82,81,80,79,78,77,76,75,74,73,72,71,70,69,68,67,66,65,57,56,55,54,53,52,51,50,49,48,122,121,120,119,118,117,116,115,114,113,112,111,110,109,108,107,106,105,104,103,102,101,100,99,98,97,95,};
    int[]  st = {20,20,20,20,20,20,20,20,20,20,20,20,20,20,20,20,20,20,20,20,20,20,20,20,20,20,19,19,19,19,19,19,19,19,19,19,20,20,20,20,20,20,20,20,20,20,20,20,20,20,20,20,20,20,20,20,20,20,20,20,20,20,20,};
    Hashtable<Character, Integer> trans = new Hashtable<Character, Integer>();
    for (int i = 0; i < tc.length; i++) trans.put(tc[i], st[i]);
    DFA.put(19, trans);
  } // end buildState19

  private void buildState20() {
    char[] tc = {90,89,88,87,86,85,84,83,82,81,80,79,78,77,76,75,74,73,72,71,70,69,68,67,66,65,57,56,55,54,53,52,51,50,49,48,122,121,120,119,118,117,116,115,114,113,112,111,110,109,108,107,106,105,104,103,102,101,100,99,98,97,95,};
    int[]  st = {20,20,20,20,20,20,20,20,20,20,20,20,20,20,20,20,20,20,20,20,20,20,20,20,20,20,19,19,19,19,19,19,19,19,19,19,20,20,20,20,20,20,20,20,20,20,20,20,20,20,20,20,20,20,20,20,20,20,20,20,20,20,20,};
    Hashtable<Character, Integer> trans = new Hashtable<Character, Integer>();
    for (int i = 0; i < tc.length; i++) trans.put(tc[i], st[i]);
    DFA.put(20, trans);
  } // end buildState20

} // end GrammarTokenizer