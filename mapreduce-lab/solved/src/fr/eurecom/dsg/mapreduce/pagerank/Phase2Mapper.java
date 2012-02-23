package fr.eurecom.dsg.mapreduce.pagerank;

import java.io.IOException;

import org.apache.hadoop.io.BooleanWritable;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.mapreduce.Mapper;

import fr.eurecom.dsg.mapreduce.pagerank.utils.NodeWritable;
import fr.eurecom.dsg.mapreduce.pagerank.utils.EitherWritable.EitherNodeBoolWritable;

public class Phase2Mapper
		extends
		Mapper<LongWritable, NodeWritable, LongWritable, EitherNodeBoolWritable> {

	private EitherNodeBoolWritable value;
	private BooleanWritable isConverged;
	private long nodeCount;
	private float missingMass;
	private float alpha;
	private float convergeThreshold;

	@Override
	protected void setup(Context context) throws IOException,
			InterruptedException {
		super.setup(context);
		
		this.value = new EitherNodeBoolWritable();
		this.isConverged = new BooleanWritable(false);
		
		this.nodeCount = context.getConfiguration().getLong(
				PageRank.COUNTNODES, 0);
		this.missingMass = context.getConfiguration().getFloat(
				PageRank.MISSINGMASS, 0f);
		this.alpha = context.getConfiguration().getFloat(PageRank.ALPHA, 0.85f);
		this.convergeThreshold = context.getConfiguration().getFloat(
				PageRank.CONVERGENCE, 0.01f);
	}

	@Override
	protected void map(LongWritable nid, NodeWritable node, Context context)
			throws IOException, InterruptedException {

		System.out.print("missing mass: " + this.missingMass);

		if (this.missingMass != 0f)
			this.missingMass /= this.nodeCount;

		System.out.println("\talpha contrib.: "
				+ (this.alpha * (node.getPageRank() + this.missingMass))
				+ "\t(1-alpha) contrib.: "
				+ ((1 - this.alpha) / this.nodeCount));

		/* Calculate the page rank for node */
		node.setPageRank(this.alpha * (node.getPageRank() + this.missingMass)
				+ (1 - this.alpha) / this.nodeCount);

		/* Propagate the node updated to the correct page rank to the reducer */
		this.value.setNode(node);
		context.write(nid, this.value);

		/* Check this node convergence and send it to the reducer */
		this.isConverged.set(node.isConverged(this.convergeThreshold));
		this.value.setBoolean(this.isConverged);
		context.write(PageRank.CONVERGENCE_ID, this.value);
	}

}
