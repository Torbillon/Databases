import java.util.Scanner;
import java.sql.*;

public class Assign2 {
  static Connection con = null;
  public static void main(String args[]) {
    String paramsFile = "ConnectionParameters.txt";

    // 1) connect to database
    if (args.length >= 1) {
      paramsFile= args[0];
    }
    try {
      con = DataBase.connect(paramsFile);
    } catch(Exception e) {
      System.out.println("Failure");
      System.exit(0);
    }

    Analyser analyst = new Analyser(con);
    analyst.analyze();

  }
}
