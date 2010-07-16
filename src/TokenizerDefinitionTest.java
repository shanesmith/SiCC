
import java.io.*;

public class TokenizerDefinitionTest {
	
	/**
	 * Each item is a broken definition to test,
	 * expected error message are in the comments
	 */
	public static final String[] definitions = {
		
		// invalid name
		"n@me: def", 	// Invalid token name "@name"
		"na me: def",	// Invalid token name "na me"
		"1name: def",	// Invalid token name "1name"
		
		 // no definition
		":: def",	// Token name not defined
		": def",	// Missing separator : after token name
		"name",		// Missing separator : after token name
		":name",	// Missing separator : after token name
		"name:",	// Regular expression not defined for token "name"
		":name:",	// Regular expression not defined for token "name"
		
		// invalid definition
		"name: :embed",		// Could not find end ':' of embed token name
		"name: embed:",		// Could not find end ':' of embed token name
		"name: :embed:",	// Cannot find token "embed" for embedding
		":name1: :name2: \n :name2: def",	// Cannot find token "name2" for embedding
		":name1: def \n :name2: :name 1:",  // Cannot find token "name 1" for embedding
		
		"name: def)",	// Could not find beggining subpattern (
		"name: (def",	// Could not find end subpattern )
		"name: ()",		// Empty subpattern
		"name: def(*)", // Empty subpattern
		"name: def]",		// Character class has not been opened with [
		"name: [def",		// Character class has not been ended with a ]
		"name: ^def]",		// Operator ^ must be in a character class
		"name: [^def",		// Character class has not been ended with a ]
		"name: []",			// Character classes cannot be empty
		"name: [^]",		// Character classes cannot be empty
		"name: [d+ef]", 	// Operator + needs escaping in character class
		"name: [d(ef]", 	// Operator ( needs escaping in character class
		"name: [d:e:f]",	// Operator : needs escaping in character class
		"name: [d^ef]",		// Character class negation operator ^ needs to be first after opening [, or escaped if literal
		"name: [def#comment]",	// Character class has not been ended with a ]
		
		"name: *def",	// Missing operand for ZERO-PLUS (*) operation
		"name: |def",	// Missing operand for ALTERN (|) operation
		"name: d||ef",	// Missing operand for ALTERN (|) operation
		
		"name: def\\",		// Nothing following the escape character
		"name: de\\f",		// Invalid escaping of character f
		"name: def\\ n", 	// Invalid escaping of character  
		
		// !!!!!!! ACCEPTED
		"name: [A-Z]", // accepted as (A or - or Z), not in the usual regex sense (A to Z)
		
		
	};
	
	public static void main(String[] args) {
		
		for (int i = 0; i < definitions.length; i++) {
			
			try {
				new TokenizerDefinition(new StringReader(definitions[i]));
				System.out.print("ACCEPTED");
			}
			catch (TokenizerDefinitionException e) {
				System.out.print("ERROR: " + e);
			}
			catch (Exception e) {
				System.out.print("UNEXPECTED: ");
				e.printStackTrace();
				break;
			}
			
			System.out.print(" => " + definitions[i].replace("\n", "\\n"));
			
			System.out.println();
			
		}
		
		/*
		try {
			new TokenizerDefinition(new FileReader("app/tokens.def"));
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		*/
		
	}

}
