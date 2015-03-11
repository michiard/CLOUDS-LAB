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
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.input.TextInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.apache.hadoop.mapreduce.lib.output.TextOutputFormat;
import org.apache.hadoop.util.Tool;
import org.apache.hadoop.util.ToolRunner;

public class WordCount extends Configured implements Tool {

    static class WCMapper extends Mapper<LongWritable, Text, Text, IntWritable> {

        private IntWritable ONE = new IntWritable(1);
        private Text textValue = new Text();

        @Override
        protected void map(LongWritable key, Text value, Context context)
                throws IOException, InterruptedException {
            String line = value.toString();
            String[] words = line.split("\\s+");
            for(String word : words) {
                textValue.set(word);
                context.write(textValue, ONE);}
        }
    }

    static class WCReducer extends Reducer<Text, IntWritable, Text, IntWritable> {

        private IntWritable writableSum = new IntWritable();

        @Override
        protected void reduce(Text key, Iterable<IntWritable> values, Context context)
                throws IOException, InterruptedException {
            int sum = 0;
            for (IntWritable value : values)
                sum += value.get();

            writableSum.set(sum);
            context.write(key,writableSum);
        }
    }

    @Override
    public int run(String[] args) throws Exception {

        Configuration conf = this.getConf();

        Job job = new Job(conf,"Word Count");

        job.setInputFormatClass(TextInputFormat.class);

        job.setMapperClass(WCMapper.class);
        job.setMapOutputKeyClass(Text.class);
        job.setMapOutputValueClass(IntWritable.class);

        job.setReducerClass(WCReducer.class);
        job.setOutputKeyClass(Text.class);
        job.setOutputValueClass(IntWritable.class);

        job.setOutputFormatClass(TextOutputFormat.class);

        FileInputFormat.addInputPath(job, new Path(args[1]));
        FileOutputFormat.setOutputPath(job, new Path(args[2]));

        job.setNumReduceTasks(Integer.parseInt(args[0]));

        job.setJarByClass(WordCount.class);

        job.waitForCompletion(true);

        return 0;
    }

    public static void main(String args[]) throws Exception {
        ToolRunner.run(new Configuration(), new WordCount(), args);
    }
}

