
import java.io.*;

class App {

  public static void main(String args[]) {
    BOBToken token;
    try {
      BOBTokenizer mytok = new BOBTokenizer(new InputStreamReader(System.in));
	int i = 0;
      while ( (token=mytok.nextToken()) != null) {
	if ( ++i % 10 != 0) mytok.pushToken();
	System.out.println(token);
      }
    }
    catch (Exception e) { e.printStackTrace(); }
  }

}
