import java.util.*;

public class Analyser {
  private static final double THRESHOLD= 0.15;
  private static final double SECONDARY_THRESHOLD = 0.05;
  private Scanner scan;
  private int crazyCount = -1;
  private int splitCount = -1;

  private String craziestDay = null;
  private double craziestPer = -1.0;


  public Analyser(Scanner scan) {
    this.scan = scan;
  }


  // iterate through data and identify crazy days and splits, as well as outputing this information
  public void analyze() {
    // name of previous company
    String name = "";

    // string containing all splits
    String splits = "";

    // used for detection of splits
    double price = 0;

    while(scan.hasNextLine()) {

      // tokenize strings
      String str = scan.nextLine();
      String[] tokens = str.split("\\s+");


      // token[0] (name of company) equals "name" (previous company name)?
      if(tokens[0].equals(name)) {

        // crazy day?
        double delta = isCrazy(tokens);
        if(delta > 0) {
          print("Crazy day: " + tokens[1] + "\t" + String.format("%.2f",delta*100) + "\n");
          crazyCount++;
        }

        // build up String "splits" for printing it later on
        splits += computeSplits(tokens[1],Double.parseDouble(tokens[5]),price);


      } else {

        //print craziest day and splits
        printCraziest();
        printSplits(splits);

        // Reset everything for next company
        crazyCount = 0;
        craziestDay = null;
        craziestPer = -1.0;
        splits = "";
        splitCount = 0;

        // Print and store name of company that is being processed
        name = tokens[0];
        print("Processing " + name + "\n====================\n");

      }


      // save closing price for computeSplits() function call
      price = Double.parseDouble(tokens[2]);
    }

    printCraziest();
    printSplits(splits);
  }



  // Prints out total number of crazy days and the craziest day in that order.
  private void printCraziest() {

    String s = "";
    if(crazyCount >= 0)
      s += "Total crazy days = " + crazyCount + "\n";
    if(craziestDay != null)
      s += "The craziest day: " + craziestDay + " " + String.format("%.2f",craziestPer*100) + "\n\n";
    else
      s += "\n";

    print(s);
  }


  // Return true when a day is "crazy", otherwise false
  private double isCrazy(String[] tokens) {
    // high price
    double x = Double.parseDouble(tokens[3]);
    // low price
    double y = Double.parseDouble(tokens[4]);
    // result
    double z = (x - y)/x;

    boolean flag = (z >= THRESHOLD);

    // update craziestDay if needed
    if (z > this.craziestPer && flag) {
      this.craziestPer = z;
      this.craziestDay = tokens[1];
    }

    if(flag)
      return z;
    return 0;
  }


  // If there is a split, then splitCount is incremented and we return the desired output string
  private String computeSplits(String day, double close, double open) {
    double z = Math.abs(close/open - 2.0);

    if(Math.abs(close/open - 2.0) < SECONDARY_THRESHOLD) {
      this.splitCount++;
      return "2:1 split on " + day + "\t" + String.format("%.2f",close) + " --> " + String.format("%.2f",open) + "\n";
    } else if(Math.abs(close/open - 3.0) < SECONDARY_THRESHOLD) {
      this.splitCount++;
      return "3:1 split on " + day + "\t" + String.format("%.2f",close) + " --> " + String.format("%.2f",open) + "\n";
    } else if(Math.abs(close/open - 1.5) < SECONDARY_THRESHOLD) {
      this.splitCount++;
      return "3:2 split on " + day + "\t" + String.format("%.2f",close) + " --> " + String.format("%.2f",open) + "\n";
    }
    else
      return "";
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
