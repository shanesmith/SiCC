import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;


public class CMM {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		Reader r = null;
		if (args.length == 0) {
			r = new InputStreamReader(System.in);
		} else {
			try {
				r = new FileReader(args[0]);
			} catch (IOException e) {
				System.err.println("Error occurred while opening input file " + args[0]);
				System.err.println(e);
				System.exit(-1);
			}
		}
		CMMTokenizer t = new CMMTokenizer(r);
		CMMParser p = new CMMParser(t);
		CMMASTNode n = null;
		try {
			n = p.parse();
		} catch (CMMTokenizerException e) {
			System.err.println("A tokenizer exception occured:" + e);
			System.exit(-1);
		} catch (CMMParserException e) {
			System.err.println("A parse exception occured:" + e);
			System.exit(-1);
		}
		System.out.println(n);
		CMMInterpreterVisitor v = new CMMInterpreterVisitor();
		CMMData res = n.accept(v, null);
		System.out.println(res);
	}

}
