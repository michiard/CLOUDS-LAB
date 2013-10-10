package fr.eurecom.dsg.mapreduce;

import java.io.IOException;
import java.util.StringTokenizer;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.conf.Configured;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.lib.input.TextInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.apache.hadoop.mapreduce.lib.output.TextOutputFormat;
import org.apache.hadoop.util.Tool;
import org.apache.hadoop.util.ToolRunner;


public class Pair extends Configured implements Tool {

  public static class PairMapper 
   extends Mapper<Object, // TODO: change Object to input key type
                  Object, // TODO: change Object to input value type
                  Object, // TODO: change Object to output key type
                  Object> { // TODO: change Object to output value type
    // TODO: implement mapper
  }

  public static class PairReducer
    extends Reducer<Object, // TODO: change Object to input key type
                    Object, // TODO: change Object to input value type
                    Object, // TODO: change Object to output key type
                    Object> { // TODO: change Object to output value type
    // TODO: implement reducer
  }

  private int numReducers;
  private Path inputPath;
  private Path outputDir;

  public Pair(String[] args) {
    if (args.length != 3) {
      System.out.println("Usage: Pair <num_reducers> <input_path> <output_path>");
      System.exit(0);
    }
    this.numReducers = Integer.parseInt(args[0]);
    this.inputPath = new Path(args[1]);
    this.outputDir = new Path(args[2]);
  }
  

  @Override
  public int run(String[] args) throws Exception {

    Configuration conf = this.getConf();
    Job job = null;  // TODO: define new job instead of null using conf e setting a name
    
    // TODO: set job input format
    // TODO: set map class and the map output key and value classes
    // TODO: set reduce class and the reduce output key and value classes
    // TODO: set job output format
    // TODO: add the input file as job input (from HDFS) to the variable inputFile
    // TODO: set the output path for the job results (to HDFS) to the variable outputPath
    // TODO: set the number of reducers using variable numberReducers
    // TODO: set the jar class

    return job.waitForCompletion(true) ? 0 : 1;
  }

  public static void main(String[] args) throws Exception {
    int res = ToolRunner.run(new Configuration(), new Pair(args), args);
    System.exit(res);
  }
}
