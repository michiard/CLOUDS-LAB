package fr.eurecom.dsg.mapreduce.utils;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.util.ToolRunner;

public class LabConfigurator {

  /**
   * Word count configuration utility
   * 
   * @param args arguments to parse
   * @param conf configuration to fill with arguments
   */
  public final static void parseArgs(String[] args,
                                     Configuration conf,
                                     int numInputs) {
    if (args.length != 2+numInputs) {
      System.err.println("You must provide "
          + String.valueOf(numInputs + 2)
          + " arguments to this program: "
          + " the number of reducers (should be an integer),"
          + " the input file and the output path");
      ToolRunner.printGenericCommandUsage(System.err);
      System.exit(-1);
    }
    conf.setInt("wc_numred", Integer.parseInt(args[0]));
    for (int i=1; i<=numInputs; i++) {
      conf.set("wc_input" + String.valueOf(i), args[i]);
    }
    conf.set("wc_output",args[numInputs+1]);
  }
}
