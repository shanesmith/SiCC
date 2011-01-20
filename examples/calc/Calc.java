import java.io.InputStreamReader;
import java.io.Reader;


public class Calc {
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		Reader r = new InputStreamReader(System.in);
		CalcTokenizer t = new CalcTokenizer(r);
		CalcParser p = new CalcParser(t);
		CalcASTNode n = null;
		try {
			n = p.parse();
		} catch (CalcTokenizerException e) {
			System.err.println("A tokenizer exception occured:" + e);
			System.exit(-1);
		} catch (CalcParserException e) {
			System.err.println("A parse exception occured:" + e);
			System.exit(-1);
		}
		System.out.println(n.accept(new CalcEvalVisitor(), null));
	}

}
