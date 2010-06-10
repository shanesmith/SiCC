
/*

id: :alpha:(:digit:|:alpha:)*

:alpha: [abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ_]

op: [\*\?\|]

eol: (\n|\r|\r\n)

:digit: [0123456789]

rparen: \)

multi_child: \[>1\]

skip: \#[^\n\r]:eol:

lparen: \(

sep: ->

*/

import java.io.*;
import java.util.Hashtable;
import java.util.Vector;
import java.util.Stack;
import java.util.ListIterator;

class GrammarTokenizer {

  private static final char wildcard = 3;
  private static final char neg = 4;

  private LineNumberReader input;

  private Hashtable<Integer, Hashtable<Character, Integer>> DFA = new Hashtable<Integer, Hashtable<Character, Integer>>();

  private Hashtable<Integer, String> accepting = new Hashtable<Integer, String>();

  private Vector<Token> tokenHistory = new Vector<Token>();
  private ListIterator<Token> tokenHistoryIT = tokenHistory.listIterator();
  private int tokenHistorySize = 20;

  private Stack<Integer> pushedChars = new Stack<Integer>();

  public GrammarTokenizer(Reader reader) {
    input = new LineNumberReader(reader);
    buildDFA();
  } // end constructor

  public Token nextToken() throws Exception {
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

  private Token _nextToken() throws Exception{
    int c;
    String value;
    int curState;
    int lineNumber = input.getLineNumber(); //get line number right away since reading might change it

  tokenLoop:
    while (true) {
      curState = 0;
      value = "";

      while ( (c=getChar()) != -1 ) { // read in a character (-1 indicates EOF)
        if (transition(curState, (char)c) == -1) break;
        value += (char)c;
        curState = transition(curState, (char)c);
      }

      if (c == -1 && value.isEmpty()) {
		// TODO proper token type
    	return new Token(0, "eof", "", lineNumber);
      } else if (accepting.containsKey(curState)) {
        pushChar(c);
        if (accepting.get(curState) == "skip") continue tokenLoop;
		// TODO proper token type
        return new Token(0, accepting.get(curState), value, lineNumber);
      } else {
        value += (char)c;
        String error = "(" + lineNumber + ") No such token for char sequence: ";
        for (char cc : value.toCharArray()) error += (int)cc + " (" + cc + "), ";
        throw new Exception(error);
      }
    }
  } // end _nextToken

  public void pushToken() throws Exception {
    if (tokenHistoryIT.hasPrevious()) {
      tokenHistoryIT.previous();
    } else {
      throw new Exception("Too many token pushbacks!");
    }
  } // end pushToken

  public void setTokenHistorySize(int size) { tokenHistorySize = size; }
  public int getTokenHistorySize() { return tokenHistorySize; }
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
  } // end feed

  private void pushChar(Integer c) { pushedChars.push(c); }

  private int getChar() throws Exception {
    if (!pushedChars.empty()) {
      return pushedChars.pop();
    } else {
      int charcode = input.read();
      if (isInvalidCharCode(charcode)) throw new Exception("invalid character! (" + charcode + ")");
      return charcode;
    }
  } //end getChar

  public static boolean isInvalidCharCode(int c) { return c < -1 || (c > -1 && c < 10) || (c > 13 && c < 32) || c > 126; }

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
    trans.put((char)90, 22);
    trans.put((char)89, 22);
    trans.put((char)88, 22);
    trans.put((char)87, 22);
    trans.put((char)86, 22);
    trans.put((char)85, 22);
    trans.put((char)84, 22);
    trans.put((char)83, 22);
    trans.put((char)82, 22);
    trans.put((char)81, 22);
    trans.put((char)80, 22);
    trans.put((char)79, 22);
    trans.put((char)78, 22);
    trans.put((char)77, 22);
    trans.put((char)76, 22);
    trans.put((char)75, 22);
    trans.put((char)74, 22);
    trans.put((char)73, 22);
    trans.put((char)72, 22);
    trans.put((char)71, 22);
    trans.put((char)70, 22);
    trans.put((char)69, 22);
    trans.put((char)68, 22);
    trans.put((char)67, 22);
    trans.put((char)66, 22);
    trans.put((char)65, 22);
    trans.put((char)57, 21);
    trans.put((char)56, 21);
    trans.put((char)55, 21);
    trans.put((char)54, 21);
    trans.put((char)53, 21);
    trans.put((char)52, 21);
    trans.put((char)51, 21);
    trans.put((char)50, 21);
    trans.put((char)49, 21);
    trans.put((char)48, 21);
    trans.put((char)122, 22);
    trans.put((char)121, 22);
    trans.put((char)120, 22);
    trans.put((char)119, 22);
    trans.put((char)118, 22);
    trans.put((char)117, 22);
    trans.put((char)116, 22);
    trans.put((char)115, 22);
    trans.put((char)114, 22);
    trans.put((char)113, 22);
    trans.put((char)112, 22);
    trans.put((char)111, 22);
    trans.put((char)110, 22);
    trans.put((char)109, 22);
    trans.put((char)108, 22);
    trans.put((char)107, 22);
    trans.put((char)106, 22);
    trans.put((char)105, 22);
    trans.put((char)104, 22);
    trans.put((char)103, 22);
    trans.put((char)102, 22);
    trans.put((char)101, 22);
    trans.put((char)100, 22);
    trans.put((char)99, 22);
    trans.put((char)98, 22);
    trans.put((char)97, 22);
    trans.put((char)95, 22);

    // state 2
    trans = new Hashtable<Character, Integer>();
    DFA.put(2, trans);

    // state 3
    trans = new Hashtable<Character, Integer>();
    DFA.put(3, trans);

    // state 4
    trans = new Hashtable<Character, Integer>();
    DFA.put(4, trans);
    trans.put((char)62, 18);

    // state 5
    trans = new Hashtable<Character, Integer>();
    DFA.put(5, trans);

    // state 6
    trans = new Hashtable<Character, Integer>();
    DFA.put(6, trans);
    trans.put((char)62, 17);

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
    trans.put((char)10, 15);
    trans.put((char)13, 14);

    // state 13
    trans = new Hashtable<Character, Integer>();
    DFA.put(13, trans);

    // state 14
    trans = new Hashtable<Character, Integer>();
    DFA.put(14, trans);
    trans.put((char)10, 16);

    // state 15
    trans = new Hashtable<Character, Integer>();
    DFA.put(15, trans);

    // state 16
    trans = new Hashtable<Character, Integer>();
    DFA.put(16, trans);

    // state 17
    trans = new Hashtable<Character, Integer>();
    DFA.put(17, trans);

    // state 18
    trans = new Hashtable<Character, Integer>();
    DFA.put(18, trans);
    trans.put((char)49, 19);

    // state 19
    trans = new Hashtable<Character, Integer>();
    DFA.put(19, trans);
    trans.put((char)93, 20);

    // state 20
    trans = new Hashtable<Character, Integer>();
    DFA.put(20, trans);

    // state 21
    trans = new Hashtable<Character, Integer>();
    DFA.put(21, trans);
    trans.put((char)90, 22);
    trans.put((char)89, 22);
    trans.put((char)88, 22);
    trans.put((char)87, 22);
    trans.put((char)86, 22);
    trans.put((char)85, 22);
    trans.put((char)84, 22);
    trans.put((char)83, 22);
    trans.put((char)82, 22);
    trans.put((char)81, 22);
    trans.put((char)80, 22);
    trans.put((char)79, 22);
    trans.put((char)78, 22);
    trans.put((char)77, 22);
    trans.put((char)76, 22);
    trans.put((char)75, 22);
    trans.put((char)74, 22);
    trans.put((char)73, 22);
    trans.put((char)72, 22);
    trans.put((char)71, 22);
    trans.put((char)70, 22);
    trans.put((char)69, 22);
    trans.put((char)68, 22);
    trans.put((char)67, 22);
    trans.put((char)66, 22);
    trans.put((char)65, 22);
    trans.put((char)57, 21);
    trans.put((char)56, 21);
    trans.put((char)55, 21);
    trans.put((char)54, 21);
    trans.put((char)53, 21);
    trans.put((char)52, 21);
    trans.put((char)51, 21);
    trans.put((char)50, 21);
    trans.put((char)49, 21);
    trans.put((char)48, 21);
    trans.put((char)122, 22);
    trans.put((char)121, 22);
    trans.put((char)120, 22);
    trans.put((char)119, 22);
    trans.put((char)118, 22);
    trans.put((char)117, 22);
    trans.put((char)116, 22);
    trans.put((char)115, 22);
    trans.put((char)114, 22);
    trans.put((char)113, 22);
    trans.put((char)112, 22);
    trans.put((char)111, 22);
    trans.put((char)110, 22);
    trans.put((char)109, 22);
    trans.put((char)108, 22);
    trans.put((char)107, 22);
    trans.put((char)106, 22);
    trans.put((char)105, 22);
    trans.put((char)104, 22);
    trans.put((char)103, 22);
    trans.put((char)102, 22);
    trans.put((char)101, 22);
    trans.put((char)100, 22);
    trans.put((char)99, 22);
    trans.put((char)98, 22);
    trans.put((char)97, 22);
    trans.put((char)95, 22);

    // state 22
    trans = new Hashtable<Character, Integer>();
    DFA.put(22, trans);
    trans.put((char)90, 22);
    trans.put((char)89, 22);
    trans.put((char)88, 22);
    trans.put((char)87, 22);
    trans.put((char)86, 22);
    trans.put((char)85, 22);
    trans.put((char)84, 22);
    trans.put((char)83, 22);
    trans.put((char)82, 22);
    trans.put((char)81, 22);
    trans.put((char)80, 22);
    trans.put((char)79, 22);
    trans.put((char)78, 22);
    trans.put((char)77, 22);
    trans.put((char)76, 22);
    trans.put((char)75, 22);
    trans.put((char)74, 22);
    trans.put((char)73, 22);
    trans.put((char)72, 22);
    trans.put((char)71, 22);
    trans.put((char)70, 22);
    trans.put((char)69, 22);
    trans.put((char)68, 22);
    trans.put((char)67, 22);
    trans.put((char)66, 22);
    trans.put((char)65, 22);
    trans.put((char)57, 21);
    trans.put((char)56, 21);
    trans.put((char)55, 21);
    trans.put((char)54, 21);
    trans.put((char)53, 21);
    trans.put((char)52, 21);
    trans.put((char)51, 21);
    trans.put((char)50, 21);
    trans.put((char)49, 21);
    trans.put((char)48, 21);
    trans.put((char)122, 22);
    trans.put((char)121, 22);
    trans.put((char)120, 22);
    trans.put((char)119, 22);
    trans.put((char)118, 22);
    trans.put((char)117, 22);
    trans.put((char)116, 22);
    trans.put((char)115, 22);
    trans.put((char)114, 22);
    trans.put((char)113, 22);
    trans.put((char)112, 22);
    trans.put((char)111, 22);
    trans.put((char)110, 22);
    trans.put((char)109, 22);
    trans.put((char)108, 22);
    trans.put((char)107, 22);
    trans.put((char)106, 22);
    trans.put((char)105, 22);
    trans.put((char)104, 22);
    trans.put((char)103, 22);
    trans.put((char)102, 22);
    trans.put((char)101, 22);
    trans.put((char)100, 22);
    trans.put((char)99, 22);
    trans.put((char)98, 22);
    trans.put((char)97, 22);
    trans.put((char)95, 22);

    accepting.put(22, "id");
    accepting.put(21, "id");
    accepting.put(20, "multi_child");
    accepting.put(17, "sep");
    accepting.put(16, "skip");
    accepting.put(15, "skip");
    accepting.put(14, "skip");
    accepting.put(11, "eol");
    accepting.put(10, "eol");
    accepting.put(9, "eol");
    accepting.put(8, "skip");
    accepting.put(5, "lparen");
    accepting.put(3, "rparen");
    accepting.put(2, "op");
    accepting.put(1, "id");
  } //end buildDFA()

} // end GrammarTokenizer
