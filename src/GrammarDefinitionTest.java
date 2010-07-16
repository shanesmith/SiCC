import java.io.StringReader;


public class GrammarDefinitionTest {
	
	public static final String[] definitions = {
	
		/*"tok",			// Rule seperator -> not found after rule name
		"-> tok",		// Rule does not begin with a valid ID
		"9Rule -> def", // Rule does not begin with a valid ID
		"Ru le -> def",
		
		"Rule -> ()",
		"Rule -> def(*)",
		"Rule -> (def",
		"Rule -> def)",
		
		"Rule -> def -> def",	// Unexpected sep (->)
		"Rule -> [>1]",
		"Rule -> def [>1] abc",
		"Rule -> * def",
		"Rule -> def (* abc)",
		"Rule -> d |",
		"Rule -> | d",
		"Rule -> abc (| def)",
		"Rule -> d | | f",
		"Rule -> d | f |",
		"Rule -> def \\*",
		
		// ACCEPTED
		"Rule -> abc () def",	// Empty sub-patterns are ignored
		"Rule -> def #comment",*/
		"#comment \n Rule -> def",
		
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
