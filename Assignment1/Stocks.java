import java.util.*;
import java.nio.file.*;

public class Stocks {

  public static void println(Object v) {
    System.out.println(v);
  }
  public static void print(Object v) {
    System.out.print(v);
  }

  public static void main(String[] args) {

    // check argument
    if (args.length != 1) {
      println("Expected one and only one argument\n");
      System.exit(0);
    }

    // try to open file
    try {
      Path fileP = Paths.get(args[0]);
      Scanner scan = new Scanner(fileP);

      // Perform analysis on data
      Analyser analyst = new Analyser(scan);
      analyst.analyze();

    // throw exception if not possible
    } catch (Exception e) {
      println("File could not be opened.");
      System.exit(0);
    }
  }
}
