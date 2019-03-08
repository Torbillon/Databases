import java.util.*;
import java.io.FileInputStream;
import java.sql.*;

// DONE
public class DataBase {
  public static String url;
  public static final String driverName = "com.mysql.jdbc.Driver";
  public static String username;
  public static Connection con;
  public static Connection con1;

  public static void connect() throws Exception {

    //Connect to baeks2 Database
    String paramsFile1 = "writerparams.txt";
    Properties connectprops1 = new Properties();
    connectprops1.load(new FileInputStream(paramsFile1));
    try{
       Class.forName("com.mysql.jdbc.Driver");
       String dburl1 = connectprops1.getProperty("dburl") + "?autoReconnect=true&useSSL=false";
       String username1 = connectprops1.getProperty("user");
       Connection conn1 = DriverManager.getConnection(dburl1, connectprops1);
       System.out.printf("Database connection %s %s established. %n", dburl1, username1);
       Assignment3.con1 = conn1;

       PreparedStatement stmt4 = conn1.prepareStatement("drop table if exists Performance;");
       stmt4.executeUpdate();

       PreparedStatement stmt5 = conn1.prepareStatement("create table Performance(Industry char(30), Ticker char(6), StartDate char(10), EndDate char(10), TickerReturn char(12), IndustryReturn char(12));");
       stmt5.executeUpdate();

    } catch(SQLException ex){
       System.out.printf("SQLException: %s%nSQLState: %s%nVendorError: %s%n", ex.getMessage(), ex.getSQLState(), ex.getErrorCode());
    }


    //Connect to johnson330 Database
    String paramsFile = "readerparams.txt";
    Properties connectprops = new Properties();
    connectprops.load(new FileInputStream(paramsFile));

    try{
       Class.forName("com.mysql.jdbc.Driver");
       String dburl = connectprops.getProperty("dburl") + "?autoReconnect=true&useSSL=false";
       String username = connectprops.getProperty("user");
       Connection conn = DriverManager.getConnection(dburl, connectprops);
       Assignment3.con = conn;
       System.out.printf("Database connection %s %s established. %n", dburl, username);
    } catch(SQLException ex){
       System.out.printf("SQLException: %s%nSQLState: %s%nVendorError: %s%n", ex.getMessage(), ex.getSQLState(), ex.getErrorCode());
    }
  }

  public static void close() {

    try {
      Assignment3.con.close();
      Assignment3.con1.close();
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
