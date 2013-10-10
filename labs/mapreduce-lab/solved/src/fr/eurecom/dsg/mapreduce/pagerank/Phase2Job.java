package fr.eurecom.dsg.mapreduce.pagerank;

import java.io.IOException;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.mapreduce.Job;

import fr.eurecom.dsg.mapreduce.pagerank.utils.NodeInputFormat;
import fr.eurecom.dsg.mapreduce.pagerank.utils.NodeOutputFormat;
import fr.eurecom.dsg.mapreduce.pagerank.utils.NodeWritable;
import fr.eurecom.dsg.mapreduce.pagerank.utils.EitherWritable.EitherNodeBoolWritable;

public class Phase2Job extends Job {

	public Phase2Job(Configuration conf) throws IOException {
		super(conf,"Page Rank phase 2");
		
		this.setInputFormatClass(NodeInputFormat.class);
		
		this.setMapperClass(Phase2Mapper.class);
		this.setMapOutputKeyClass(LongWritable.class);
		this.setMapOutputValueClass(EitherNodeBoolWritable.class);
		
		this.setReducerClass(Phase2Reducer.class);
		this.setOutputValueClass(NodeWritable.class);
		this.setOutputKeyClass(LongWritable.class);
		
		this.setOutputFormatClass(NodeOutputFormat.class);

	}

}
