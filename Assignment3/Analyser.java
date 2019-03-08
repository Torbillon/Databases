import java.util.*;
import java.sql.*;
import java.text.DecimalFormat;


public class Analyser {
  private Scanner scan;
  private double divisor = 1.0;

  private Connection conn;
  private Connection conn1;
  private int splitCount = 0;



  public Analyser(Connection conn, Connection conn1) {
    this.conn = conn;
    this.conn1 = conn1;

  }

  public void analyze() {
    try {
      ArrayList<String> Industry = getIndustryList();

      for(int n = 0; n < Industry.size(); n++){
          ArrayList<String> tickNm = new ArrayList<String>();
          ArrayList<String> TransDate = new ArrayList<String>();
          ArrayList<String> intvs = new ArrayList<String>();




          // get time interval for specific industry
          PreparedStatement state = conn.prepareStatement("select max(minTransDate) as maxima, min(maxTransDate) as mininima " +
                                                          " from (select Ticker, min(TransDate) as minTransDate, max(TransDate) as maxTransDate, count(distinct TransDate) as TradingDays " +
                                                                  " from Company natural join PriceVolume " +
                                                                  " where Industry = ? " +
                                                                  " group by Ticker " +
                                                                  " having TradingDays >= 150 " +
                                                                  " order by Ticker) as tiempo;");

          state.setString(1, Industry.get(n));
          ResultSet rs1 = state.executeQuery();
          rs1.next();
          String min = rs1.getString("maxima");
          String max = rs1.getString("mininima");





          // get dates between min and max
          PreparedStatement state1 = conn.prepareStatement("select Ticker, count(distinct TransDate) as TradingDays " +
                                                           " from Company natural join PriceVolume " +
                                                           " where Industry = ? and TransDate >= ? and TransDate <= ? " +
                                                           " group by Ticker " +
                                                           " having TradingDays >= 150 " +
                                                           " order by Ticker;");

          state1.setString(1, Industry.get(n));
          state1.setString(2, min);
          state1.setString(3, max);
          ResultSet rs2 = state1.executeQuery();
          while(rs2.next()){
             tickNm.add(rs2.getString("Ticker"));
          }






          // do analysis and load up data
          if(tickNm.size() > 1){
             PreparedStatement state2 = conn.prepareStatement("select TransDate " +
                                                               " from PriceVolume " +
                                                               " where Ticker = ? and TransDate >= ? and TransDate <= ?");

             state2.setString(1, tickNm.get(0));
             state2.setString(2, min);
             state2.setString(3, max);
             ResultSet rs3 = state2.executeQuery();
             while(rs3.next()){
                TransDate.add(rs3.getString("TransDate"));
             }



             int zeta = 0;
             int count= 0;
             while(zeta < TransDate.size()){
                if((zeta + 60) < TransDate.size()){
                   intvs.add(TransDate.get(zeta));
                   count++;
                }
                zeta = zeta + 60;
             }






             intvs.add(TransDate.get(zeta-61));
             ArrayList<String> rnam = new ArrayList<String>();
             ArrayList<String> std = new ArrayList<String>();
             ArrayList<String> endd = new ArrayList<String>();
             ArrayList<Double> result = new ArrayList<Double>();
             ArrayList<Double> result1 = new ArrayList<Double>();
             ArrayList<Double> result2 = new ArrayList<Double>();

             for(int i = 0; i < intvs.size() - 1; i++){
                for(int j = 0; j < tickNm.size(); j++){
                   result.add(split(tickNm.get(j), min, intvs.get(i), intvs.get(i+1)));
                   result2.add(split(tickNm.get(j), min, intvs.get(i), intvs.get(i+1)));
                   rnam.add(tickNm.get(j));
                   std.add(intvs.get(i));
                   if((result.size()%(tickNm.size())) == 0){
                      double pi = 0.0;
                      for(int x = 0; x < result.size(); x ++){
                         pi = pi + result.get(x);
                      }
                      for(int z = 0; z < result.size(); z ++){
                         double lo = 0.0;
                         lo = pi - result.get(z);
                         lo = lo + (tickNm.size()-1);
                         lo = lo / (tickNm.size()-1);
                         lo = lo - 1;
                         result1.add(lo);


                      }
                      result.clear();
                   }
                }
             }





             for(int z = 0; z < intvs.size() -1; z++) {
                for(int y = 0; y < tickNm.size(); y++) {
                   PreparedStatement state7 = conn.prepareStatement("select TransDate " + " from PriceVolume " + " where Ticker = ? and TransDate >= ? and TransDate <= ? " + " order by TransDate;");
                   state7.setString(1, tickNm.get(y));
                   state7.setString(2, intvs.get(z));
                   state7.setString(3, intvs.get(z+1));
                   ResultSet right = state7.executeQuery();
                   ArrayList<String> Trading = new ArrayList<String>();
                   while(right.next()) {
                      Trading.add(right.getString("TransDate"));
                   }
                   endd.add(Trading.get(Trading.size()-2));
                }
             }





             // insert into username(barragb) database
             for(int y = 0; y < result2.size(); y++) {
                PreparedStatement state6 = conn1.prepareStatement("insert into Performance(Industry, Ticker, StartDate, EndDate, TickerReturn, IndustryReturn) values(?,?,?,?,?,?);");
                state6.setString(1, Industry.get(n));
                state6.setString(2, rnam.get(y));
                state6.setString(3, std.get(y));
                state6.setString(4, endd.get(y));
                DecimalFormat df = new DecimalFormat("#.#######");
                state6.setString(5, df.format(result2.get(y)));
                state6.setString(6, df.format(result1.get(y)));
                state6.executeUpdate();
             }
          }
       }


    } catch(SQLException ex) {
       System.out.printf("SQLException: %s%nSQLState: %s%nVendorError: %s%n", ex.getMessage(), ex.getSQLState(), ex.getErrorCode());
    }



    DataBase.close();
  }


  // get the Industry List
  private ArrayList<String> getIndustryList() {
    try {
      ArrayList<String> Industry = new ArrayList<String>();
      PreparedStatement st = conn.prepareStatement("select Industry " + " from Company "+ "  group by Industry " + " order by Industry;");
      ResultSet rt = st.executeQuery();
      while(rt.next()){
         Industry.add(rt.getString("Industry"));
      }
      return Industry;
    } catch(Exception e) {
      return null;
    }
  }





  private void println(Object v) {
    System.out.println(v);
  }

  private void print(Object v) {
    System.out.print(v);
  }

  // perform split procedure
  private double split(String ticker, String minimum, String Sdate, String Sdate1) throws SQLException {
      ArrayList<Double> opnAdj = new ArrayList<Double>();
      ArrayList<Double> cs = new ArrayList<Double>();
      ArrayList<Double> cls = new ArrayList<Double>();
      ArrayList<String> trds = new ArrayList<String>();
      ArrayList<Double> opn = new ArrayList<Double>();

      // execute query
      PreparedStatement pstmt = conn.prepareStatement("select Ticker, TransDate, OpenPrice, ClosePrice " +
        " from PriceVolume " +
         " where Ticker = ? and TransDate >= ? and TransDate <= ? " +
          " order by TransDate DESC");
      pstmt.setString(1, ticker);
      pstmt.setString(2, minimum);
      pstmt.setString(3, Sdate1);
      ResultSet rs = pstmt.executeQuery();




      // split the rest
      int b = 0;
      double x = 1.0;
      while (rs.next()) {
         trds.add(rs.getString(2));
         opn.add(rs.getDouble(3));
         opnAdj.add(rs.getDouble(3));
         cs.add(rs.getDouble(4));
         cls.add(rs.getDouble(4));


         if(b > 0){
            if(rs.getDouble(4) != opn.get(b-1)){
               if(Math.abs(rs.getDouble(4)/opn.get(b-1) - 2.0) < 0.20){
                  x = x * 2.0;
               }else if(Math.abs(rs.getDouble(4)/opn.get(b-1) - 3.0) < 0.30){
                  x = x * 3.0;
               }else if(Math.abs(rs.getDouble(4)/opn.get(b-1) - 1.5) < 0.15){
                  x = x * 1.5;
               }
            }
            if(x > 1.0){
               opnAdj.set(b, rs.getDouble(3)/x);
               cls.set(b, rs.getDouble(4)/x);
            }
         }
         b++;
      }






      double z = 0.0;
      double y = 0.0;
      for(int a =  trds.size() - 1; a >= 0; a --){
        if(trds.get(a).equals(Sdate)){
           z = opnAdj.get(a);
        }
        else if(trds.get(a).equals(Sdate1)){
           y = cls.get(a+1);
        }
      }
      double result = 0.0;



      pstmt.close();
      return (y/z) - 1;
  }
}
