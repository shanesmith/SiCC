
// OUTDATED?

import java.io.LineNumberReader;
import java.io.Reader;
import java.util.Stack;


public class TokenizerRunner {

	public static final String SKIP = "skip";
	
	private TokenDFA DFA;
	
	private LineNumberReader input;
	
	/*
	 * Keep track of pushed characters (see getChar(), pushChar())
	 */
	private Stack<Integer> pushedChars = new Stack<Integer>();
	
	public TokenizerRunner(TokenizerDefinition tokendef, Reader reader) throws Exception {
		this.DFA = tokendef.getMasterTokenDFA();
		this.input = new LineNumberReader(reader);
	}
	
	public Token nextToken() throws Exception {
		
		int c, line;
		
		String value;
		
		TokenizerDFAState curState;
			
	tokenLoop:
		while (true) {
		
			line = input.getLineNumber();
			
			value = "";
			
			curState = DFA.getStartState();
			
			// read in a character (-1 indicates EOF)
			while ( (c=getChar()) != -1 ) {
				
				if (!curState.transitionExists((char)c)) break;
					
				value += (char)c;
				
				curState = curState.doTransition((char)c);
				
			}
			
			if (c == -1 && value.isEmpty()) {
				// TODO proper token type
				return new Token(0, "eof", null, line);
			}
			
			if (curState.isAccepting()) {
				pushChar(c);
				
				TokenDFA tokdfa = curState.getOwners().firstElement();
				
				if (tokdfa.name.equals(SKIP)) {
					continue tokenLoop;
				} else {
					// TODO proper token type
					return new Token(0, tokdfa.name, value, line);
				}
				
			} else {
				value += (char)c;
				
				throw new TokenizerException("No such token for string \"" + value + "\"");
			}
		}
	}

	/*
	 * Push a character back to the stream.
	 * 
	 * The next call to getChar() will return this character.
	 */
	public void pushChar(Integer c) {
		pushedChars.push(c);
	}
	
	/*
	 * Returns the next character from the stream.
	 * 
	 * A character of value -1 indicates the end of the stream
	 */
	public int getChar() throws Exception {
		if (!pushedChars.empty()) {
			return pushedChars.pop();
		} else {
			int charcode = input.read();
			
			if (isInvalidCharCode(charcode)) throw new TokenizerException("invalid character! (" + charcode + ")");
			
			return charcode;
		}
	}
	
	/*
	 * Only allow characters that make sense (including -1 for EOF) 
	 */
	public static boolean isInvalidCharCode(int c) { return c < -1 || (c > -1 && c < 10) || (c > 13 && c < 32) || c > 126; }
	
}
