package fr.eurecom.dsg.mapreduce;

import java.io.IOException;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.conf.Configured;
import org.apache.hadoop.fs.Path;
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

import fr.eurecom.dsg.mapreduce.utils.StringToIntAssociativeArrayWritable;

public class Stripes extends Configured implements Tool {

	public static class StripesMapper
	extends
	Mapper<LongWritable, Text, Text, StringToIntAssociativeArrayWritable> {

		@Override
		public void map(LongWritable key, Text value, Context context)
		throws java.io.IOException, InterruptedException {

			String line = value.toString();
			line = line.replaceAll("[^a-zA-Z0-9_]+", " ");
			line = line.replaceAll("^\\s+", "");
			String[] tokens = line.split("\\s+");
			StringToIntAssociativeArrayWritable h = new StringToIntAssociativeArrayWritable();
			for (int i = 0; i < tokens.length-1; i++) {
				h.clear();
				for (int j = Math.max(0, i - 1); j < Math.min(tokens.length,
						i + 2); j++) {
					if (i == j)
						continue;
					h.increment(tokens[j]);
				}
				context.write(new Text(tokens[i]), h);
			}
		}
	}

	public static class StripesReducer
	extends
	Reducer<Text, StringToIntAssociativeArrayWritable, Text, StringToIntAssociativeArrayWritable> {

		@Override
		public void reduce(Text key,
				Iterable<StringToIntAssociativeArrayWritable> values,
				Context context) throws IOException, InterruptedException {

			StringToIntAssociativeArrayWritable hf = new StringToIntAssociativeArrayWritable();
			for (StringToIntAssociativeArrayWritable value : values) {
				hf.sum(value);
			}
			context.write(key, hf);
		}
	}

	@Override
	public int run(String[] args) throws Exception {
		if (args.length != 3) {

			System.err.printf("%s requires two arguments\n", getClass()
					.getSimpleName());

			ToolRunner.printGenericCommandUsage(System.err);
			return -1;
		}

		int numreducer = Integer.parseInt(args[2]);

		Configuration conf = getConf();
		Job job = new Job(conf, "PairNewAPI");

		job.setJarByClass(Stripes.class);

		job.setMapperClass(StripesMapper.class);
		job.setReducerClass(StripesReducer.class);

		job.setMapOutputKeyClass(Text.class);
		job.setMapOutputValueClass(StringToIntAssociativeArrayWritable.class);

		job.setOutputKeyClass(Text.class);
		job.setOutputValueClass(StringToIntAssociativeArrayWritable.class);

		TextInputFormat.addInputPath(job, new Path(args[0]));
		job.setInputFormatClass(TextInputFormat.class);

		FileOutputFormat.setOutputPath(job, new Path(args[1]));
		job.setOutputFormatClass(TextOutputFormat.class);

		job.setNumReduceTasks(numreducer);

		return job.waitForCompletion(true) ? 0 : 1;
	}

	public static void main(String[] args) throws Exception {
		int res = ToolRunner.run(new Configuration(), new Stripes(), args);
		System.exit(res);
	}
}
