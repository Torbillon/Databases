import java.util.*;
import java.io.FileInputStream;
import java.sql.*;

// DONE
public class DataBase {
  public static String url;
  public static final String driverName = "com.mysql.jdbc.Driver";
  public static String username;
  public static Connection con;

  public static Connection connect(String paramsFile) throws Exception {

    Properties connectprops = new Properties();
    connectprops.load(new FileInputStream(paramsFile));


    Class.forName("com.mysql.jdbc.Driver");
    url = connectprops.getProperty("dburl");
    String dburl =  url + "?autoReconnect=true&useSSL=false";
    username = connectprops.getProperty("user");


    con = DriverManager.getConnection(dburl, connectprops);
    System.out.printf("Database connection %s %s established.%n", url, username);


    return con;
  }

  public static void close() {

    try {
      con.close();
      println("Database connection closed.");


    }
    catch(SQLException e) {
      println("SQLException: Unable to close Database connection.");
    }

  }

  // get Name of company associated with the given ticker
  public static boolean printFullName(String ticker) {


    try {

      PreparedStatement pstmt = con.prepareStatement("select distinct Name " +
                     "from Company " +
                     "where Ticker = ?");
      pstmt.setString(1, ticker);

      ResultSet rs = pstmt.executeQuery();
      if(rs.next()) {
        println(rs.getString("Name"));
        return true;
      } else {
        println(ticker + " not found in datebase\n");
      }


    }
    catch(SQLException e) {
      println("SQLException");
    }

    return false;
  }

  // get Table from executed query
  // x is guaranteed to be a valid date or emtpy string and cannot be modified to perform an
  // SQL injection attack, which can be proven by the function isValidDate() in Analyser.java
  public static List<List<String>> getList(String ticker, String x) {

    List<List<String>> rowList = null;


    try {
      PreparedStatement pstmt = con.prepareStatement(
                     "select * " + "from PriceVolume " +
                     "where Ticker = ? " + x + " order by TransDate " +
                     "DESC ");

      pstmt.setString(1, ticker);


      rowList = new LinkedList<List<String>>();
      ResultSet rs = pstmt.executeQuery();
      if(!rs.next())
        return null;


      do {

        List<String> columnList = new LinkedList<String>();
        rowList.add(columnList);


        columnList.add(rs.getString("Ticker"));
        columnList.add(rs.getString("TransDate"));
        columnList.add(rs.getString("OpenPrice"));
        columnList.add(rs.getString("HighPrice"));


        columnList.add(rs.getString("LowPrice"));
        columnList.add(rs.getString("ClosePrice"));
        columnList.add(rs.getString("Volume"));
        columnList.add(rs.getString("AdjustedClose"));
      } while(rs.next());


    } catch(SQLException e) {
      rowList = null;
      print("Exception\n");
    }

    return rowList;
  }


  private static void println(Object v) {
    System.out.println(v);
  }


  private static void print(Object v) {
    System.out.print(v);
  }
}
