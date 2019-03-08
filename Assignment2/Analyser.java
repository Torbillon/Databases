import java.util.*;
import java.sql.*;

public class Analyser {
  private static final double THRESHOLD= 0.20;
  private static final double SECONDARY_THRESHOLD = 0.30;
  private static final double TRI_THRESHOLD = 0.15;
  private Scanner scan;
  private double divisor = 1.0;

  private Connection con;
  private int splitCount = 0;



  public Analyser(Connection con) {
    this.con = con;

  }

  public void analyze() {

    String[] tokens= tokens = lexicalAnalyzer();
    while(!tokens[0].equals("")) {
      // check if input makes sense. If so compute.
      if(semanticAnalyzer(tokens))
        compute(tokens);
      // get new company name (and dates if given)
      tokens = lexicalAnalyzer();
    }


    DataBase.close();
  }

  // Parses input from user
  private String[] lexicalAnalyzer() {

    String[] tokens = scanToken();
    while(tokens.length > 3) {
      println("Can only take at most 3 strings");
      tokens = scanToken();
    }

    return tokens;
  }


  // Determine whether "args" contains valid arguments
  private boolean semanticAnalyzer(String[] args) {

    if(args.length == 1)
      return true;
    else if (args.length == 2)
      return isValidDate(args[1]);
    else if (args.length == 3)
      return isValidDate(args[1]) && isValidDate(args[2]);
    else
      return false;

  }


  private boolean isValidDate(String s) {

    String[] v = s.split("\\.");
    if(v.length != 3) {
      print("Need 3 digits to specify date\n");
      return false;
    } else {
      return (v[0].matches("\\d\\d\\d\\d") && v[1].matches("\\d\\d") && v[2].matches("\\d\\d"));
    }
  }


  // precondition: 0 < args.length < 4,
  // iterate through data and identify crazy days and splits, as well as outputing this information
  public void compute(String[] args) {
    String name = args[0];

    if(DataBase.printFullName(name))
      calculate(args);
  }

  // start buying and selling stocks
  public void calculate(String[] args) {

    // get rows of table
    List<List<String>> list = DataBase.getList(args[0], dates(args));


    if(list != null) {
      int count = list.toArray().length;
      String alpha = "";
      // used for detection of splits
      double price = Double.parseDouble(list.get(0).get(2));


      for(int i = 0; i < count; i++) {
        // tokenize strings
        List<String> tokens = list.get(i);
        // build up String "alpha" for printing it later on
        alpha += computeSplits(tokens.get(1),Double.parseDouble(tokens.get(5)),price, tokens);
        // save closing price for computeSplits() function call
        price = Double.parseDouble(tokens.get(2));
        // update row i
        update(tokens,this.divisor);
      }

      // print all the split information
      print(alpha);
      println(this.splitCount + " splits in " + count + " trading days");

      // perfor investment algorithm
      investStrategy(list, count);
      // reset divisor and split count
      this.divisor = 1.0;
      this.splitCount = 0;
    }
  }


  private String dates(String[] args) {
    String x = "and TransDate > '";
    String y = "and TransDate < '";
    if(args.length == 2)
      return x + args[1] + "' ";
    else if(args.length == 3)
      return x + args[1] + "' " + y + args[2] + "' ";
    else
      return "";
  }


  private void investStrategy(List<List<String>> table, int count) {
    double sum = 0.0;
    double cash = 0.0;
    int transactions = 0;
    int shares = 0;


    for(int i = count - 1; i > 0; i--) {
      // get row i
      List<String> list = table.get(i);


      if(i <= count - 51) {
        // adjust moving sum for moving 50-day average
        if(i <= count - 52)
          sum -= Double.parseDouble(table.get(i + 51).get(5));

        // get avg(d), close(d), open(d)
        double avg = sum / 50;
        double close = Double.parseDouble(list.get(5));
        double open = Double.parseDouble(list.get(2));


        // Buy criterion
        if(close < avg && close / open < 0.97000001) {
          List<String> temp = table.get(i-1);
          shares += 100;
          cash -= Double.parseDouble(temp.get(2)) * 100;
          cash -= 8.0;
          transactions++;


        // Sell criterion
        } else if(shares >= 100 && open > avg) {
          List<String> temp = table.get(i+1);
          double pastClose = Double.parseDouble(temp.get(5));

          if(open/pastClose > 1.00999999) {
            shares -= 100;
            cash += (open + close) * 50;
            cash -= 8.0;
            transactions++;


          }
        }
      }

      sum += Double.parseDouble(list.get(5));
    }


    List<String> list = table.get(0);
    if(shares > 0)
      cash += Double.parseDouble(list.get(2)) * shares;


    println("\nExecuting Investment Strategy \nTransactions executed " +
            transactions + "\nNet Cash: " + String.format("%.2f", cash) + "\n");
  }


  public void update(List<String> tokens, double x) {

    double a = Double.parseDouble(tokens.get(2));
    double b = Double.parseDouble(tokens.get(3));
    double c = Double.parseDouble(tokens.get(4));
    double d = Double.parseDouble(tokens.get(5));


    tokens.set(2, "" + a / x);
    tokens.set(3, "" + b / x);
    tokens.set(4, "" + c / x);
    tokens.set(5, "" + d / x);

  }


  private String[] scanToken() {
    Scanner s = new Scanner(System.in);
    print("Enter a ticker symbol [start/end dates]: ");

    return s.nextLine().split("\\s+");
  }


  // If there is a split, then splitCount is incremented and we return the desired output string
  private String computeSplits(String day, double close, double open, List<String> tokens) {

    if(Math.abs(close/open - 2.0) < THRESHOLD) {
      this.divisor *= 2.0;
      this.splitCount++;
      return "2:1 split on " + day + "\t" + String.format("%.2f",close) + " --> " + String.format("%.2f",open) + "\n";


    } else if(Math.abs(close/open - 3.0) < SECONDARY_THRESHOLD) {
      this.divisor *= 3.0;
      this.splitCount++;
      return "3:1 split on " + day + "\t" + String.format("%.2f",close) + " --> " + String.format("%.2f",open) + "\n";


    } else if(Math.abs(close/open - 1.5) < TRI_THRESHOLD) {
      this.divisor *= 1.5;
      this.splitCount++;
      return "3:2 split on " + day + "\t" + String.format("%.2f",close) + " --> " + String.format("%.2f",open) + "\n";


    }
    else {
      return "";
    }
  }



  // Print the "split" string
  private void printSplits(String str) {
    // str is not empty?
    if(splitCount >= 0)
      print(str + "Total number of splits: " + splitCount + "\n\n");
  }


  private void println(Object v) {
    System.out.println(v);
  }


  private void print(Object v) {
    System.out.print(v);
  }
}
