/**
 * Usage example:
 * hadoop jar WordCount.jar fr.eurecom.webtech.WordCountDriver -mapper map /user/hadoop/README.txt /user/hadoop/output
 */
package fr.eurecom.dsg.mapreduce;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.StringTokenizer;
import java.util.Map.Entry;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.conf.Configured;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.input.TextInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.apache.hadoop.mapreduce.lib.output.TextOutputFormat;
import org.apache.hadoop.util.Tool;
import org.apache.hadoop.util.ToolRunner;

public class WordCountComplex extends Configured implements Tool {

	public final static IntWritable ONE = new IntWritable(1);
	
	/**
	 * Utility to split the input argument in words
	 * 
	 * @param text what we want to split
	 * @return words in text
	 */
	public static String[] words(String text) {
	     StringTokenizer st = new StringTokenizer(text);
	     ArrayList<String> result = new ArrayList<String>();
	     while (st.hasMoreTokens())
	         result.add(st.nextToken());
	     return Arrays.copyOf(result.toArray(),result.size(),String[].class);
	}

	/**
	 * Simplest mapper, for every words found emit the pair (word,1)
	 *
	 */
	public static class WCMapper extends Mapper<LongWritable, Text, Text, IntWritable> {

		@Override
		protected void map(LongWritable key, Text value, Context context)
				throws IOException, InterruptedException {
			
			for (String word : WordCountComplex.words(value.toString()))
				context.write(new Text(word),WordCountComplex.ONE);
		}
	}
	
	/**
	 * In map aggregation using an hashmap: for every map aggregate the results and emit the
	 * pair (word,num_for_map)
	 */
	public static class WCMapperInMapAggregator1 extends Mapper<LongWritable, Text, Text, IntWritable> {

		private HashMap<String, Integer> partialResults;
		
		@Override
		protected void setup(Context context) throws IOException,
				InterruptedException {
			super.setup(context);
			this.partialResults = new HashMap<String, Integer>();
		}
		
		@Override
		protected void map(LongWritable key, Text value, Context context)
				throws IOException, InterruptedException {
			
			this.partialResults.clear();
			
			for (String word : WordCountComplex.words(value.toString()))
				if (this.partialResults.containsKey(word))
					this.partialResults.put(word, this.partialResults.get(word)+1);
				else
					this.partialResults.put(word, 1);
			
			for (Entry<String, Integer> entry : this.partialResults.entrySet())
				context.write(new Text(entry.getKey()), new IntWritable(entry.getValue()));
		}
	}

	/**
	 * Full aggregation, aggregate using an hashmap the results of every map call on this node
	 * and emit the pair (word,tot_num)
	 *
	 */
	public static class WCMapperInMapAggregator2 extends Mapper<LongWritable, Text, Text, IntWritable> {
		
		private HashMap<String, Integer> partialResults;
		
		@Override
		protected void setup(Context context) throws IOException,
				InterruptedException {
			super.setup(context);
			this.partialResults = new HashMap<String, Integer>();
		}

		@Override
		protected void map(LongWritable key, Text value, Context context)
				throws IOException, InterruptedException {
			
			for (String word : WordCountComplex.words(value.toString()))
				if (this.partialResults.containsKey(word))
					this.partialResults.put(word, this.partialResults.get(word)+1);
				else
					this.partialResults.put(word, 1);
		}
		
		@Override
		protected void cleanup(Context context) throws IOException,
				InterruptedException {
		
			for (Entry<String, Integer> entry : this.partialResults.entrySet())
				context.write(new Text(entry.getKey()), new IntWritable(entry.getValue()));
			
			super.cleanup(context);
		}
	}
	
	/**
	 * The word count reducer sums results for a given word
	 *
	 */
	public static class WCReducer extends Reducer<Text, IntWritable, Text, IntWritable> {

		@Override
		protected void reduce(Text text, Iterable<IntWritable> iterable, Context context)
				throws IOException, InterruptedException {
			
			int result = 0;
			
			for (IntWritable iterator : iterable)
				result += iterator.get();
			
			context.write(text, new IntWritable(result));
		}
	}
	
	private final static HashMap<String, Class<? extends Mapper<LongWritable, Text, Text, IntWritable>>> MAPPERCLASSES;
	static {
		MAPPERCLASSES = new HashMap<String, Class<? extends Mapper<LongWritable, Text, Text, IntWritable>>>();
		MAPPERCLASSES.put("simple", WCMapper.class);
		MAPPERCLASSES.put("map", WCMapperInMapAggregator1.class);
		MAPPERCLASSES.put("end", WCMapperInMapAggregator2.class);
	};
	
	/**
	 * Validate arguments setting the configuration
	 * 
	 * @param args
	 * @param conf
	 * @return
	 */
	public static boolean validateAndParseArgs (String[] args, Configuration conf) {
		
		if (args.length < 2)
			return false;
		
		conf.set("-if", args[args.length-2]);
		conf.set("-of", args[args.length-1]);
		
		for (int index = 0; index < args.length-3; index++) {
			
			if (args[index] == "-mapper")
				if (MAPPERCLASSES.containsKey(args[index+1]))
					conf.set(args[index], args[index++]);
				else
					return false;
		}
		
		if (conf.get("-mapper") == null)
			conf.set("-mapper", "simple");
		
		return true;
	}

	@Override
	public int run(String[] args) throws Exception {

		Configuration conf = this.getConf();
		
		if (!WordCountComplex.validateAndParseArgs(args, conf)) {
			System.out.println("Usage: WordCount [-mapper (simple,map,end)] <filepath> <outputpath>");
			System.exit(0);
		}
		
		Path filePath = new Path(conf.get("-if"));
		Path outputPath = new Path(conf.get("-of"));
		
		// Start configuring the job
		Job job = new Job(conf,"Word Count");
		
		job.setJarByClass(WordCountComplex.class);

		job.setInputFormatClass(TextInputFormat.class);
		job.setOutputFormatClass(TextOutputFormat.class);
		
		job.setMapOutputKeyClass(Text.class);
		job.setMapOutputValueClass(IntWritable.class);
		job.setOutputKeyClass(Text.class);
		job.setOutputValueClass(IntWritable.class);
		
		job.setMapperClass(MAPPERCLASSES.get(conf.get("-mapper")));
		job.setCombinerClass(WCReducer.class);
		job.setReducerClass(WCReducer.class);
		
		FileInputFormat.addInputPath(job, filePath);
		FileOutputFormat.setOutputPath(job, outputPath);
		
		job.waitForCompletion(false);
		
		return 0;
	}
	
	public static void main(String[] args) throws Exception {
		int res = ToolRunner.run(new Configuration(), new WordCountComplex(), args);
		System.exit(res);
	}
}
