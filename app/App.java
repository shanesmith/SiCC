
import java.io.*;

class App {

  public static void main(String args[]) {

    try {
      Tokenizer mytok = new Tokenizer(new InputStreamReader(System.in));
      
      Parser myparser = new Parser(mytok);
      
      ASTProgramNode program = myparser.parse();
      
      System.out.println(program);
      
      for (ASTNode sub : program.getChildren()) {
      	System.out.print(sub + " | ");
      }
      System.out.println();
      
    }
    catch (Exception e) { e.printStackTrace(); }
  }

}
