package fr.eurecom.dsg.mapreduce;

import java.io.IOException;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.conf.Configured;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.NullWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.util.Tool;
import org.apache.hadoop.util.ToolRunner;

public class DistributedCacheJoin extends Configured implements Tool {

  private Path outputDir;
  private Path inputFile;
  private Path inputTinyFile;
  private int numReducers;

  public DistributedCacheJoin(String[] args) {
    if (args.length != 4) {
      System.out.println("Usage: DistributedCacheJoin <num_reducers> " +
      		"<input_tiny_file> <input_file> <output_dir>");
      System.exit(0);
    }
    this.numReducers = Integer.parseInt(args[0]);
    this.inputTinyFile = new Path(args[1]);
    this.inputFile = new Path(args[2]);
    this.outputDir = new Path(args[3]);
  }

  @Override
  public int run(String[] args) throws Exception {
    Configuration conf = getConf();

    // TODO: add the smallFile to the distributed cache

    Job job = null; // TODO: define new job instead of null using conf e setting
                    // a name

    // TODO: set job input format
    // TODO: set map class and the map output key and value classes
    // TODO: set reduce class and the reduce output key and value classes
    // TODO: set job output format
    // TODO: add the input file as job input (from HDFS) to the variable
    // inputFile
    // TODO: set the output path for the job results (to HDFS) to the variable
    // outputPath
    // TODO: set the number of reducers using variable numberReducers
    // TODO: set the jar class

    return job.waitForCompletion(true) ? 0 : 1;
  }

  public static void main(String[] args) throws Exception {
    int res = ToolRunner.run(new Configuration(),
                             new DistributedCacheJoin(args),
                             args);
    System.exit(res);
  }

}

// TODO: implement mapper
// TODO: implement reducer
