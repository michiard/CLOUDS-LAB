package fr.eurecom.dsg.mapreduce;

import java.io.IOException;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.conf.Configured;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.util.Tool;
import org.apache.hadoop.util.ToolRunner;

import fr.eurecom.dsg.mapreduce.utils.LabConfigurator;

/**
 * Word Count example of MapReduce job. Given a plain text in input, this job
 * counts how many occurrences of each word there are in that text and writes
 * the result on HDFS.
 *
 */
public class WordCountCombiner extends Configured implements Tool {

	@Override
	public int run(String[] args) throws Exception {
		
		Configuration conf = this.getConf();
        int numberReducers = conf.getInt("wc_numred", 1);
        Path inputFile = new Path(conf.get("wc_input1"));
        Path outputPath = new Path(conf.get("wc_output"));
		
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
	
	public static void main(String args[]) throws Exception {
	  Configuration conf = new Configuration();
    LabConfigurator.parseArgs(args, conf, 1);
    int res = ToolRunner.run(conf, new WordCountCombiner(), args);
		System.exit(res);
	}
}

class WCCMapper extends Mapper<Object,   // TODO: change Object to input key type
                               Object,   // TODO: change Object to input value type
                               Object,   // TODO: change Object to output key type
                               Object> { // TODO: change Object to output value type

	@Override
	protected void map(Object key, // TODO: change Object to input key type 
	                   Object value, // TODO: change Object to input value type
	                   Context context)
			throws IOException, InterruptedException {
		
	  // TODO: implement the map method (use context.write to emit results)
	}

}

class WCCReducer extends Reducer<Object,   // TODO: change Object to input key type
                                 Object,   // TODO: change Object to input value type
                                 Object,   // TODO: change Object to output key type
                                 Object> { // TODO: change Object to output value type

	@Override
	protected void reduce(Object key, // TODO: change Object to input key type
	                      Iterable<Object> values, // TODO: change Object to input value type
	                      Context context)
			throws IOException, InterruptedException {
		
	  // TODO: implement the reduce method (use context.write to emit results)
	}
}
