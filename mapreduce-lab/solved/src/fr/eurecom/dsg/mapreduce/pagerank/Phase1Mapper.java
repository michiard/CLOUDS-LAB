package fr.eurecom.dsg.mapreduce.pagerank;

import java.io.IOException;

import org.apache.hadoop.io.FloatWritable;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.mapreduce.Mapper;

import fr.eurecom.dsg.mapreduce.pagerank.utils.NodeWritable;
import fr.eurecom.dsg.mapreduce.pagerank.utils.EitherWritable.EitherNodeFloatWritable;

public class Phase1Mapper
		extends
		Mapper<LongWritable, NodeWritable, LongWritable, EitherNodeFloatWritable> {

	EitherNodeFloatWritable value;
	FloatWritable content = new FloatWritable();
	private final static FloatWritable ONE = new FloatWritable(1f);
	boolean isCountNodes;

	@Override
	protected void setup(Context context) throws IOException,
			InterruptedException {
		super.setup(context);
		this.value = new EitherNodeFloatWritable();
		this.isCountNodes = context.getConfiguration().getBoolean(
				PageRank.MUSTCOUNTNODES, true);
	}

	@Override
	protected void map(LongWritable n, NodeWritable N, Context context)
			throws IOException, InterruptedException {

		assert !(n.equals(PageRank.COUNTNODES_ID) || n
				.equals(PageRank.MISSINGMASS_ID));

		/* Propagate the node structure to the reducer */
		this.value.setNode(N);
		context.write(n, this.value);

		/* Count one if this is also a count nodes job */
		if (this.isCountNodes) {
			this.value.setFloat(Phase1Mapper.ONE);
			context.write(PageRank.COUNTNODES_ID, this.value);
		}

		/*
		 * If the node doesn't have neighbours propagate its page rank as
		 * missing mass, else for every neighbour propagate its page rank
		 * divided by the neighbours number
		 */
		if (N.getAdjacencyList().getLength() == 0) {
			this.content.set(N.getPageRank());
			this.value.setFloat(this.content);
			context.write(PageRank.MISSINGMASS_ID, this.value);
			
		} else {
			this.content
					.set(N.getPageRank() / N.getAdjacencyList().getLength());
			this.value.setFloat(this.content);
			for (LongWritable neighbor : ((LongWritable[]) N.getAdjacencyList()
					.toArray())) {
				context.write(neighbor, this.value);
			}
		}

	}

}
