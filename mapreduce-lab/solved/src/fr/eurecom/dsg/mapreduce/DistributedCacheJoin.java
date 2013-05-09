/**
 *  Authors:
 *              Antonio Uccio Verardi   <verardia@eurecom.fr>
 *              Enrico Canzonieri       <canzonie@eurecom.fr>
 **/
 
package fr.eurecom.dsg.mapreduce;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import java.util.StringTokenizer;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.conf.Configured;
import org.apache.hadoop.filecache.DistributedCache;
import org.apache.hadoop.fs.Path;
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

		// TODO: define new job instead of null using conf e setting a name
		Configuration conf = this.getConf();

		// TODO: add the smallFile to the distributed cache
		DistributedCache.addCacheFile(inputTinyFile.toUri(), conf);

		Job job = new Job (conf);
		job.setJobName("DistributedCacheJoin");  

		// TODO: set job input format
		job.setInputFormatClass(TextInputFormat.class);

		// TODO: set map class and the map output key and value classes
		job.setMapperClass(MSMap.class);
		job.setMapOutputKeyClass(Text.class);
		job.setMapOutputValueClass(LongWritable.class);

		// set combiner class
		job.setCombinerClass(MSReduce.class);

		// TODO: set reduce class and the reduce output key and value classes
		job.setReducerClass(MSReduce.class);
		job.setOutputKeyClass(Text.class);
		job.setOutputValueClass(LongWritable.class);

		// TODO: set job output format
		job.setOutputFormatClass(TextOutputFormat.class);

		// TODO: add the input file as job input (from HDFS) to the variable
		//       inputFile
		FileInputFormat.setInputPaths(job, inputFile);

		// TODO: set the output path for the job results (to HDFS) to the variable
		//       outputPath
		FileOutputFormat.setOutputPath(job, outputDir);

		// TODO: set the number of reducers using variable numberReducers
		job.setNumReduceTasks(numReducers);

		// TODO: set the jar class
		job.setJarByClass(DistributedCacheJoin.class);

		return job.waitForCompletion(true) ? 0 : 1;
	}

	public static void main(String[] args) throws Exception {
		int res = ToolRunner.run(new Configuration(),
				new DistributedCacheJoin(args),
				args);
		System.exit(res);
	}

}

class MSMap extends Mapper<LongWritable, Text, Text, LongWritable> {

	Set<String> exclude = new HashSet<String>();
	private Text word = new Text();
	static final private LongWritable ONE = new LongWritable(1);

	@Override
	protected void setup(Context context) throws IOException,
	InterruptedException {
		// TODO: load the vector from the small file cached
		Path [] cachePaths = DistributedCache.getLocalCacheFiles(context.getConfiguration());
		if (cachePaths.length>0) {
			exclude.clear();
			String line;
			BufferedReader in = new BufferedReader( new FileReader(cachePaths[0].toString()));
			while ((line = in.readLine()) != null) {
				String splitted[]=line.split("\\s+");
				for(String s : splitted) {
					exclude.add(s);    		
				}
			}
			in.close();
		}
	}

	@Override
	protected void map(LongWritable key, Text value, Context context)
			throws IOException, InterruptedException {
		// TODO: implement the map method
		StringTokenizer st= new StringTokenizer(value.toString());
		while (st.hasMoreTokens()) {
			String tmp = st.nextToken();
			if (!exclude.contains(tmp)) {
				this.word.set(tmp);
				context.write(this.word,ONE);
			}
		}
	}
}

class MSReduce extends Reducer<Text, LongWritable, Text, LongWritable> {

	@Override
	protected void reduce(Text word, Iterable<LongWritable> vals,
			Context context) throws IOException, InterruptedException {
		// TODO: implement the reduce method
		long accumulator = 0;
		for (LongWritable value : vals) {
			accumulator += value.get();
		}
		context.write(word, new LongWritable(accumulator));
	}
}

