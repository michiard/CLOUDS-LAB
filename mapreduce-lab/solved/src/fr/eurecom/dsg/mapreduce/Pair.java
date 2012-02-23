package fr.eurecom.dsg.mapreduce;

import java.io.IOException;

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

import fr.eurecom.dsg.mapreduce.utils.TextPair;

public class Pair extends Configured implements Tool {

	public static class PairMapper extends
	Mapper<LongWritable, Text, TextPair, IntWritable> {

		@Override
		public void map(LongWritable key, Text value, Context context)
		throws java.io.IOException, InterruptedException {

			String line = value.toString();
			line = line.replaceAll("[^a-zA-Z0-9_]+", " ");
			line = line.replaceAll("^\\s+", "");
			String[] tokens = line.split("\\s+");
			for (int i = 0; i < tokens.length-1; i++) {
				for (int j = Math.max(0, i - 1); j < Math.min(tokens.length, i + 2); j++) {
					if (i == j)
						continue;
					context.write(new TextPair(tokens[i], tokens[j]),
							new IntWritable(1));
				}
			}
		}
	}

	public static class PairReducer extends
	Reducer<TextPair, IntWritable, TextPair, IntWritable> {

		@Override
		public void reduce(TextPair key, Iterable<IntWritable> values,
				Context context) throws IOException, InterruptedException {
			int s = 0;
			for (IntWritable value : values) {	
				s += value.get();
			}
			context.write(key, new IntWritable(s));
		}
	}

	@Override
	public int run(String[] args) throws Exception {
		if (args.length != 3) {
			System.err.printf("%s requires three arguments\n", getClass().getSimpleName());
			ToolRunner.printGenericCommandUsage(System.err);
			return -1;
		}

		int numreducer = Integer.parseInt(args[2]);

		Configuration conf = getConf();
		Job job = new Job(conf, "PairNewAPI");

		job.setJarByClass(Pair.class);

		job.setMapperClass(PairMapper.class);
		job.setReducerClass(PairReducer.class);

		job.setMapOutputKeyClass(TextPair.class);
		job.setMapOutputValueClass(IntWritable.class);

		job.setOutputKeyClass(TextPair.class);
		job.setOutputValueClass(IntWritable.class);

		TextInputFormat.addInputPath(job, new Path(args[0]));
		job.setInputFormatClass(TextInputFormat.class);

		FileOutputFormat.setOutputPath(job, new Path(args[1]));
		job.setOutputFormatClass(TextOutputFormat.class);

		job.setNumReduceTasks(numreducer);

		return job.waitForCompletion(true) ? 0 : 1;
	}

	public static void main(String[] args) throws Exception {
		int res = ToolRunner.run(new Configuration(), new Pair(), args);
		System.exit(res);
	}
}
