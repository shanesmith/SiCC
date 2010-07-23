import java.io.StringReader;


public class GrammarDefinitionTest {
	
	public static final String[] definitions = {
	
		/*"tok",			// Rule seperator -> not found after rule name
		"-> tok",		// Rule does not begin with a valid ID
		"9Rule -> def", // Rule does not begin with a valid ID
		"Ru le -> def", // Rule seperator (->) not found after rule name
		
		"Rule -> ",			// Empty rule definition
		"Rule -> #comment", // Empty rule definition
		"Rule -> ()",		// Empty rule definition
		"Rule -> def(*)",	// Missing operand for * operation
		"Rule -> def (abc|) cgh", // Missing operand for ALTERN (|) operation
		"Rule -> (def",		// Unbalanced sub-expression, could not find end )
		"Rule -> def)",		// Unbalanced sub-expression, could not find beginning )
		
		"Rule -> def -> def",	// Unexpected sep (->)
		"Rule -> [>1]",			// Empty rule definition
		"Rule -> def [>1] abc", // Expected eol after multi-child token
		"Rule -> * def",		// Missing operand for ZERO-PLUS (*) operation
		"Rule -> def (* abc)",	// Missing operand for ZERO-PLUS (*) operation
		"Rule -> d |",			// Missing operand for ALTERN (|) operation
		"Rule -> | d",			// Missing operand for ALTERN (|) operation
		"Rule -> abc (| def)",	// Missing operand for ALTERN (|) operation
		"Rule -> d | | f",		// Missing operand for ALTERN (|) operation
		"Rule -> d | f |",		// Missing operand for ALTERN (|) operation
		"Rule -> def \\*",		// Invalid: \
		"Rule -> [def]", 		// Invalid: [d
		
		"A -> A b",				// Left recursion detected at rule "A"
		"A -> B b \n B -> A c", // Left recursion detected at rule "A"
		"A -> c d | c e",		// Ambiguous grammar detected at rule "A" with token "c"
		"S -> i E t S | i E t S e S \n E -> a",				// Ambiguous grammar detected at rule "S" with token "i"
		"S -> i E t S R | a \n R -> e S | \\0 \n E -> b",	// Ambiguous grammar detected at rule "R" with token "e"*/
		
		"E -> T EP \n EP -> add T EP | \\0 \n T -> F TP \n TP -> multiply F TP | \\0 \n F -> ( E ) | number",
		
		// ACCEPTED
		"Rule -> abc () def",	// Empty sub-patterns are ignored
		"Rule -> def #comment",
		"Rule -> abc #comment \n Rule -> def",
		
	};
	
	public static void main(String[] args) {
		
		for (int i = 0; i < definitions.length; i++) {
			
			try {
				new GrammarDefinition(new StringReader(definitions[i]));
				System.out.print("ACCEPTED");
			}
			catch (GrammarDefinitionException e) {
				System.out.print("ERROR: " + e);
			}
			catch (Exception e) {
				System.out.print("UNEXPECTED: ");
				e.printStackTrace();
			}
			
			System.out.print(" => " + definitions[i].replace("\n", "\\n"));
			
			System.out.println();
			
		}
		
	}
	
}
