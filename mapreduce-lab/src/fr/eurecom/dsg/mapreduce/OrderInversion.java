package fr.eurecom.dsg.mapreduce;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.conf.Configured;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.DoubleWritable;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Partitioner;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.lib.input.TextInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.apache.hadoop.mapreduce.lib.output.TextOutputFormat;
import org.apache.hadoop.util.Tool;
import org.apache.hadoop.util.ToolRunner;

import fr.eurecom.dsg.mapreduce.utils.LabConfigurator;


public class OrderInversion extends Configured implements Tool {

  private final static String ASTERISK = "\0";

  public static class PartitionerTextPair extends
  Partitioner<TextPair, IntWritable> {
    @Override
    public int getPartition(TextPair key, IntWritable value,
        int numPartitions) {
      // TODO: implement getPartition such that pairs with the same first element
      //       will go to the same reducer. You can use toUnished as utility.
      return 0;
    }
    
    /**
     * toUnsigned(10) = 10
     * toUnsigned(-1) = 2147483647
     * 
     * @param val Value to convert
     * @return the unsigned number with the same bits of val 
     * */
    public static int toUnsigned(int val) {
      return val & Integer.MAX_VALUE;
    }
  }

  public static class PairMapper extends
  Mapper<LongWritable, Text, TextPair, IntWritable> {

    @Override
    public void map(LongWritable key, Text value, Context context)
    throws java.io.IOException, InterruptedException {

      // TODO: implement the map method
    }
  }

  public static class PairReducer extends
  Reducer<TextPair, IntWritable, TextPair, DoubleWritable> {

    // TODO: implement the reduce method
  }

  @Override
  public int run(String[] args) throws Exception {
    Configuration conf = this.getConf();
    int numberReducers = conf.getInt("wc_numred", 1);
    Path inputFile = new Path(conf.get("wc_input1"));
    Path outputPath = new Path(conf.get("wc_output"));

    Job job = new Job(conf, "Pair Relative");

    job.setJarByClass(Pair.class);

    job.setMapperClass(PairMapper.class);
    job.setReducerClass(PairReducer.class);

    job.setMapOutputKeyClass(TextPair.class);
    job.setMapOutputValueClass(IntWritable.class);

    job.setOutputKeyClass(TextPair.class);
    job.setOutputValueClass(DoubleWritable.class);

    TextInputFormat.addInputPath(job, inputFile);
    job.setInputFormatClass(TextInputFormat.class);

    FileOutputFormat.setOutputPath(job, outputPath);
    job.setOutputFormatClass(TextOutputFormat.class);

    job.setPartitionerClass(PartitionerTextPair.class);

    job.setSortComparatorClass(TextPair.Comparator.class);

    job.setNumReduceTasks(numberReducers);

    return job.waitForCompletion(true) ? 0 : 1;
  }

  public static void main(String[] args) throws Exception {
    Configuration conf = new Configuration();
    LabConfigurator.parseArgs(args, conf, 1);
    int res = ToolRunner.run(conf, new OrderInversion(), args);
    System.exit(res);
  }
}
