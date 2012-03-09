package fr.eurecom.dsg.mapreduce;

import java.io.IOException;
import java.net.URI;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.conf.Configured;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.hdfs.DistributedFileSystem;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.NullWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.util.Tool;
import org.apache.hadoop.util.ToolRunner;

import fr.eurecom.dsg.mapreduce.utils.LabConfigurator;


public class DistributedCacheJoin extends Configured implements Tool {

  @Override
  public int run(String[] args) throws Exception {
    Configuration conf = getConf();
    int numberReducers = conf.getInt("wc_numred", 1);
    Path bigFile = new Path(conf.get("wc_bigFile"));
    URI smallFile = new URI(conf.get("wc_smallFile"));
    Path outputPath = new Path(conf.get("wc_output"));

    // TODO: add the smallFile to the distributed cache
    
    Job job = null; // TODO: define new job instead of null using conf e setting a name
    
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
    Configuration conf = new Configuration();
    LabConfigurator.parseArgs(args, conf, 2);
    FileSystem dfs = DistributedFileSystem.get(conf);

    long size1 = dfs.getFileStatus(new Path(conf.get("wc_input1"))).getLen();
    long size2 = dfs.getFileStatus(new Path(conf.get("wc_input2"))).getLen();
    
    if (size1 < size2) {
      conf.set("wc_bigFile", conf.get("wc_input2"));
      conf.set("wc_smallFile", conf.get("wc_input1"));
    } else {
      conf.set("wc_bigFile", conf.get("wc_input1"));
      conf.set("wc_smallFile", conf.get("wc_input2"));
    }
    int res = ToolRunner.run(conf, new DistributedCacheJoin(), args);
    System.exit(res);
  }

}

class MSMap extends Mapper<LongWritable, Text, LongWritable, LongWritable> {
  
  @Override
  protected void setup(Context context) throws IOException,
      InterruptedException {
    super.setup(context);
    // TODO: load the vector from the small file cached
  }

  @Override
  protected void map(LongWritable key, Text value, Context context)
      throws IOException, InterruptedException {
    // TODO: implement the map method
  }
  
}

class MSReduce extends Reducer<LongWritable, LongWritable, NullWritable, Text> {

  @Override
  protected void reduce(LongWritable offset, Iterable<LongWritable> vals,
                        Context context) throws IOException,
                                             InterruptedException {
    // TODO: implement the reduce method
  }
  
}
