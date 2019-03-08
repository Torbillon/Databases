import java.util.Scanner;
import java.sql.*;

public class Assignment3 {
  static Connection con = null;
  static Connection con1 = null;
  public static void main(String args[]) {
    String paramsFile = "readerparams.txt";
    String paramsFile2 = "writerparams.txt";

    // 1) connect to database
    if (args.length >= 1) {
      paramsFile= args[0];
    }
    try {
      DataBase.connect();
    } catch(Exception e) {
      System.out.println("Failure");
      System.exit(0);
    }

    Analyser analyst = new Analyser(con, con1);
    analyst.analyze();

  }
}
