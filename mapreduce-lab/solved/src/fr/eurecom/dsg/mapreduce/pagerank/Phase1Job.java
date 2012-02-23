package fr.eurecom.dsg.mapreduce.pagerank;

import java.io.IOException;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.mapreduce.Job;

import fr.eurecom.dsg.mapreduce.pagerank.utils.NodeInputFormat;
import fr.eurecom.dsg.mapreduce.pagerank.utils.NodeOutputFormat;
import fr.eurecom.dsg.mapreduce.pagerank.utils.NodeWritable;
import fr.eurecom.dsg.mapreduce.pagerank.utils.EitherWritable.EitherNodeFloatWritable;

public class Phase1Job extends Job {

	public Phase1Job(Configuration conf) throws IOException {
		super(conf,"Page Rank phase 1");
		
		this.setInputFormatClass(NodeInputFormat.class);

		this.setMapperClass(Phase1Mapper.class);
		this.setMapOutputKeyClass(LongWritable.class);
		this.setMapOutputValueClass(EitherNodeFloatWritable.class);
		
		this.setReducerClass(Phase1Reducer.class);
		this.setOutputKeyClass(LongWritable.class);
		this.setOutputValueClass(NodeWritable.class);

		this.setOutputFormatClass(NodeOutputFormat.class);
	}

}
