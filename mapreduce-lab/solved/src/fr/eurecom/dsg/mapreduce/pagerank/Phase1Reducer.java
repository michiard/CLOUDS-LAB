package fr.eurecom.dsg.mapreduce.pagerank;

import java.io.IOException;

import org.apache.hadoop.fs.FSDataOutputStream;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.mapreduce.Reducer;

import fr.eurecom.dsg.mapreduce.pagerank.utils.NodeWritable;
import fr.eurecom.dsg.mapreduce.pagerank.utils.EitherWritable.EitherNodeFloatWritable;

public class Phase1Reducer
		extends
		Reducer<LongWritable, EitherNodeFloatWritable, LongWritable, NodeWritable> {

	/** Where the job will put the missing mass */
	private FSDataOutputStream missingMass;
	/** Where the job will (eventually) put the node number */
	private FSDataOutputStream countNodes;

	/* Containers for job variables */
	private long nodeCounter;
	private float incomingLinkContrib;
	private NodeWritable node;
	private boolean isCountNodes;
	private FileSystem hdfs = null;

	@Override
	protected void setup(Context context) throws IOException,
			InterruptedException {
		super.setup(context);
		
		this.hdfs = FileSystem.get(context.getConfiguration());
		
		this.isCountNodes = context.getConfiguration().getBoolean(
				PageRank.MUSTCOUNTNODES, true);
		
		if (this.isCountNodes)
			this.nodeCounter = 0;
	}

	@Override
	protected void reduce(LongWritable m, Iterable<EitherNodeFloatWritable> ps,
			Context context) throws IOException, InterruptedException {
		
		/* Count nodes */
		if (m.equals(PageRank.COUNTNODES_ID)) {

			this.countNodes = this.hdfs.create(new Path(context.getConfiguration().get(
					PageRank.COUNTNODES_FILENAME)));
			
			for (@SuppressWarnings("unused")
			EitherNodeFloatWritable p : ps)
				this.nodeCounter += 1;

			if (this.isCountNodes)
				this.countNodes.writeLong(this.nodeCounter);
			
			this.countNodes.close();

		} else {

			this.incomingLinkContrib = 0f;

			/* Missing mass accumulator */
			if (m.equals(PageRank.MISSINGMASS_ID)) {

				this.missingMass = this.hdfs.create(new Path(context.getConfiguration().get(
						PageRank.MISSINGMASS_FILENAME)));
				
				for (EitherNodeFloatWritable p : ps)
					this.incomingLinkContrib += p.right().get();

				System.out.println("Processing missing mass:"
						+ incomingLinkContrib);

				this.missingMass.writeFloat(incomingLinkContrib);
				this.missingMass.close();

			} else {
				/* Page rank of incoming links accumulator */

				this.node = null;

				for (EitherNodeFloatWritable p : ps) {
					if (p.isLeft())
						this.node = p.left();
					else
						this.incomingLinkContrib += p.right().get();
				}
				
				System.out.println("Processing node: " + this.node);

				node.setOldPageRank(this.node.getPageRank());
				node.setPageRank(this.incomingLinkContrib);
				context.write(m, this.node);
			}
		}
	}
}
