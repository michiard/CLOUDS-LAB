package fr.eurecom.dsg.mapreduce;

import java.io.IOException;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.conf.Configured;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.util.Tool;
import org.apache.hadoop.util.ToolRunner;


public class Stripes extends Configured implements Tool {

  private int numReducers;
  private Path inputPath;
  private Path outputDir;

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

  public Stripes (String[] args) {
    if (args.length != 3) {
      System.out.println("Usage: Stripes <num_reducers> <input_path> <output_path>");
      System.exit(0);
    }
    this.numReducers = Integer.parseInt(args[0]);
    this.inputPath = new Path(args[1]);
    this.outputDir = new Path(args[2]);
  }
  
  public static void main(String[] args) throws Exception {
    int res = ToolRunner.run(new Configuration(), new Stripes(args), args);
    System.exit(res);
  }
}

class StripesMapper
extends Mapper<Object,   // TODO: change Object to input key type
               Object,   // TODO: change Object to input value type
               Object,   // TODO: change Object to output key type
               Object> { // TODO: change Object to output value type

  @Override
  public void map(Object key, // TODO: change Object to input key type
                  Object value, // TODO: change Object to input value type
                  Context context)
  throws java.io.IOException, InterruptedException {

    // TODO: implement map method
  }
}

class StripesReducer
extends Reducer<Object,   // TODO: change Object to input key type
                Object,   // TODO: change Object to input value type
                Object,   // TODO: change Object to output key type
                Object> { // TODO: change Object to output value type
  @Override
  public void reduce(Object key, // TODO: change Object to input key type
                     Iterable<Object> values, // TODO: change Object to input value type 
                     Context context) throws IOException, InterruptedException {

    // TODO: implement the reduce method
  }
}