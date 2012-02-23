package fr.eurecom.dsg.mapreduce.pagerank;

import java.io.IOException;

import org.apache.hadoop.fs.FSDataOutputStream;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.mapreduce.Reducer;

import fr.eurecom.dsg.mapreduce.pagerank.utils.NodeWritable;
import fr.eurecom.dsg.mapreduce.pagerank.utils.EitherWritable.EitherNodeBoolWritable;

public class Phase2Reducer
		extends
		Reducer<LongWritable, EitherNodeBoolWritable, LongWritable, NodeWritable> {

	private FSDataOutputStream convergedFile;

	@Override
	protected void reduce(LongWritable key,
			Iterable<EitherNodeBoolWritable> iterable, Context context)
			throws IOException, InterruptedException {

		/* Check if the algorithm is converged */
		if (key.equals(PageRank.CONVERGENCE_ID)) {
			boolean isConverged = true;
			for (EitherNodeBoolWritable b : iterable)
				if (!(isConverged = b.Boolean().get()))
					break;
			this.convergedFile = FileSystem.get(context.getConfiguration())
					.create(
							new Path(context.getConfiguration().get(
									PageRank.CONVERGE_FILE)));
			this.convergedFile.writeBoolean(isConverged);
			this.convergedFile.close();
		}
		/* Save the node structure */
		else {
			NodeWritable value = iterable.iterator().next().node();
			System.out.println("Writing " + value);
			context.write(key, value);
		}

	}

}
