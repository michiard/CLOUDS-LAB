package fr.eurecom.dsg.mapreduce;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.conf.Configured;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.util.Tool;
import org.apache.hadoop.util.ToolRunner;


public class ReduceSideJoin extends Configured implements Tool {

  private Path outputDir;
  private Path inputPath;
  private int numReducers;

  @Override
  public int run(String[] args) throws Exception {
    Configuration conf = getConf();
        
    return 0; // TODO: implement all the job components andconfigurations
  }

  public ReduceSideJoin(String[] args) {
    if (args.length != 3) {
      System.out.println("Usage: ReduceSideJoin <num_reducers> <input_file> <output_dir>");
      System.exit(0);
    }
    this.numReducers = Integer.parseInt(args[0]);
    this.inputPath = new Path(args[1]);
    this.outputDir = new Path(args[2]);
  }
  
  public static void main(String[] args) throws Exception {
    int res = ToolRunner.run(new Configuration(), new ReduceSideJoin(args), args);
    System.exit(res);
  }
  
}