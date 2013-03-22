package fr.eurecom.dsg.mapreduce;

import java.io.IOException;
import java.util.StringTokenizer;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.conf.Configured;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.Mapper.Context;
import org.apache.hadoop.util.Tool;
import org.apache.hadoop.util.ToolRunner;

/**
 * Word Count example of MapReduce job. Given a plain text in input, this job
 * counts how many occurrences of each word there are in that text and writes
 * the result on HDFS.
 *
 */
public class WordCountCombiner extends Configured implements Tool {

	private int numReducers;
  private Path inputPath;
  private Path outputDir;

  @Override
	public int run(String[] args) throws Exception {
    
		Job job = null; // TODO: define new job instead of null using conf e setting a name
		
		// TODO: set job input format
		// TODO: set map class and the map output key and value classes
		
		// * TODO: set the combiner class and the combiner output key and value classes
		
		// TODO: set reduce class and the reduce output key and value classes
		// TODO: set job output format
		// TODO: add the input file as job input (from HDFS)
		// TODO: set the output path for the job results (to HDFS)
		// TODO: set the number of reducers. This is optional and by default is 1
		// TODO: set the jar class

		return job.waitForCompletion(true) ? 0 : 1; // this will execute the job
	}
	
  public WordCountCombiner (String[] args) {
    if (args.length != 3) {
      System.out.println("Usage: WordCountCombiner <num_reducers> <input_path> <output_path>");
      System.exit(0);
    }
    this.numReducers = Integer.parseInt(args[0]);
    this.inputPath = new Path(args[1]);
    this.outputDir = new Path(args[2]);
  }
  
  public static void main(String args[]) throws Exception {
    int res = ToolRunner.run(new Configuration(), new WordCountCombiner(args), args);
    System.exit(res);
  }
}

class WCMapperCombiner extends Mapper<LongWritable, Text, Text, LongWritable> {

  private Text word = new Text();
  private final static LongWritable ONE = new LongWritable(1);

  @Override
  protected void map(LongWritable offset, Text text, Context context)
      throws IOException, InterruptedException {
    StringTokenizer iter = new StringTokenizer(text.toString());
    while (iter.hasMoreTokens()) {
      this.word.set(iter.nextToken());
      context.write(this.word , ONE);
    }
  }

}

class WCReducerCombiner extends Reducer<Text, LongWritable, Text, LongWritable> {

  @Override
  protected void reduce(Text word, Iterable<LongWritable> values, Context context)
      throws IOException, InterruptedException {
    long accumulator = 0;
    for (LongWritable value : values) {
      accumulator += value.get();
    }
    context.write(word, new LongWritable(accumulator));
  }
}
