package fr.eurecom.dsg.mapreduce.pagerank;

import java.io.IOException;
import java.util.ArrayList;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.conf.Configured;
import org.apache.hadoop.fs.FSDataInputStream;
import org.apache.hadoop.fs.FileStatus;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.apache.hadoop.util.Tool;
import org.apache.hadoop.util.ToolRunner;

/**
 * Page Rank implementation for a graph with node number < Long.MAXVALUE -1
 * 
 * @author Mario Pastorelli
 * 
 */
public class PageRank extends Configured implements Tool {

	/** Key used to count nodes */
	public final static LongWritable COUNTNODES_ID = new LongWritable(
			Long.MAX_VALUE - 1);

	/** Key used to accumulate missing mass and propagate to other nodes */
	public final static LongWritable MISSINGMASS_ID = new LongWritable(
			Long.MAX_VALUE);

	/** Key used to check if page rank is converged */
	public final static LongWritable CONVERGENCE_ID = MISSINGMASS_ID;

	/** Keywords to be used in the configuration object */
	public final static String ALPHA = "-a";
	public final static String CONVERGENCE = "-c";
	public final static String ITERATIONS = "-i";
	public final static String INPUTFILE = "-if";
	public final static String OUTPUTPATH = "-op";
	public final static String MUSTCOUNTNODES = "-cn";
	public final static String COUNTNODES = "countnodes";
	public final static String COUNTNODES_FILENAME = "countnodes_file";
	public final static String MISSINGMASS = "missingmass";
	public final static String MISSINGMASS_FILENAME = "missingmass_file";
	public final static String CONVERGE_FILE = "converge_file";
	public final static String REMOVE_OUTPUT = "-r";

	/**
	 * Validate command line arguments to configure the job
	 * 
	 * @TODO rewrite this using java and hadoop included structures
	 * 
	 * @throws IOException
	 *             see
	 *             {@link org.apache.hadoop.fs.FileSystem#get(Configuration)}
	 */
	public static boolean validateAndParseArgs(String[] args, Configuration conf)
			throws IOException {

		if (args.length < 2)
			return false;

		FileSystem fs = FileSystem.get(conf);

		boolean input = true;
		boolean remove = false;

		for (int index = 0; index < args.length; index++) {
			if (args[index].equals(PageRank.ALPHA)) {
				if (args.length < index + 1)
					return false;
				conf.setFloat(PageRank.ALPHA, Float.valueOf(args[index + 1]));
				index += 1;

			} else if (args[index].equals(PageRank.ITERATIONS)) {
				if (args.length < index + 1)
					return false;
				conf.setInt(PageRank.ITERATIONS, Integer
						.valueOf(args[index + 1]));
				index += 1;

			} else if (args[index].equals(PageRank.CONVERGENCE)) {
				if (args.length < index + 1)
					return false;
				conf.setFloat(PageRank.CONVERGENCE, Float
						.valueOf(args[index + 1]));
				index += 1;

			} else if (args[index].equals(PageRank.REMOVE_OUTPUT)) {
				remove = true;

			} else if (input) {
				conf.set(PageRank.INPUTFILE, args[index]);
				input = false;

			} else {
				conf.set(PageRank.OUTPUTPATH, args[index]);
				if (remove)
					try {
						fs.delete(new Path(args[index]), true);
					} catch (IOException e) {
					}

			}
		}

		if (conf.get(PageRank.OUTPUTPATH) == null
				|| conf.get(PageRank.INPUTFILE) == null)
			return false;

		conf.set(PageRank.COUNTNODES_FILENAME, conf.get(PageRank.OUTPUTPATH)
				+ ".countnodes");

		conf.setBoolean(PageRank.MUSTCOUNTNODES, true);
		
		conf.set(PageRank.CONVERGE_FILE, conf.get(PageRank.OUTPUTPATH) + ".converge");

		fs.close();

		return true;
	}

	/**
	 * Returns the list of part files in the basedir
	 * 
	 * @param conf
	 *            the Hadoop configuration
	 * @param basedir
	 *            the directory that contains part files
	 * @return an array of paths
	 * @throws IOException
	 *             see
	 *             {@link org.apache.hadoop.fs.FileSystem#get(Configuration)}
	 */
	private static Path[] getPartFiles(Configuration conf, Path basedir)
			throws IOException {

		ArrayList<Path> results = new ArrayList<Path>(0);
		FileStatus[] files = FileSystem.get(conf).listStatus(basedir);

		for (FileStatus file : files)
			if (file.getPath().getName().startsWith("part"))
				results.add(file.getPath());

		Path[] paths = new Path[results.size()];
		for (int i = 0; i != results.size(); i++)
			paths[i] = results.get(i);

		return paths;
	}

	/**
	 * The pagerank main method that control algorithm iterations.
	 * 
	 * @param args
	 *            contains the pagerank configuration
	 */
	@Override
	public int run(String[] args) throws Exception {

		/* Get the standard configuration */
		Configuration conf = this.getConf();
		if (conf == null)
			conf = new Configuration();

		/* Check arguments and set conf with custom configurations */
		if (!PageRank.validateAndParseArgs(args, conf)) {
			System.out
					.println("Usage: PageRank [-a <alpha> | -c <convergence> | -i <iterations_num> | -r]* <input_file> <output_path>");
			return 0;
		}

		/* Configure variables for iterations */
		int counter = 0;
		int iterations = conf.getInt(PageRank.ITERATIONS, -1);
		Phase1Job job1 = null;
		Phase2Job job2;
		Path job1Output;
		Path job2Output = null;
		FSDataInputStream is;
		FileSystem fs = FileSystem.get(conf);
		Float currmissingmass;
		
		/* Start iterations */
		System.out.println("Start iterations"
				+ (iterations > 0 ? " (iterations: " + iterations + ")" : ""));

		while ((iterations == -1 || iterations > 0) && !this.isConverged(conf, fs)) {

			System.out.print("\t" + counter + ":\tstart phase 1 ... ");

			conf.set(PageRank.MISSINGMASS_FILENAME, conf
					.get(PageRank.OUTPUTPATH)
					+ "/" + String.valueOf(counter) + ".missingmass");

			conf.set(PageRank.CONVERGE_FILE, conf.get(PageRank.OUTPUTPATH)
					+ "/" + String.valueOf(counter) + ".converge");
			
			Path missingmasspath = new Path(conf
					.get(PageRank.MISSINGMASS_FILENAME));
			
			/* Start algorithm phase 1 */
			job1 = new Phase1Job(conf);
			job1.setJarByClass(PageRank.class);
			if (counter == 0)
				FileInputFormat.addInputPath(job1, new Path(conf
						.get(PageRank.INPUTFILE)));
			else
				FileInputFormat.setInputPaths(job1, PageRank.getPartFiles(conf,
						job2Output));

			job1Output = new Path(conf.get(PageRank.OUTPUTPATH) + "/"
					+ String.valueOf(counter) + ".phase1");
			FileOutputFormat.setOutputPath(job1, job1Output);
			
			if (!job1.waitForCompletion(false))
				throw new Exception("job 1 failed!");

			/* Read the missingmass and (eventually) the node number */
			if (fs.exists(missingmasspath)) {
				is = fs.open(missingmasspath);
				currmissingmass = is.readFloat();
				is.close();
			} else
				currmissingmass = 0f;
			conf.setFloat(PageRank.MISSINGMASS, currmissingmass);

			if (counter == 0) {
				is = fs.open(new Path(conf.get(PageRank.COUNTNODES_FILENAME)));
				conf.setLong(PageRank.COUNTNODES, is.readLong());
				is.close();

				conf.setBoolean(PageRank.MUSTCOUNTNODES, false);
			}

			System.out.print("done (missingmass: "
					+ conf.getFloat(PageRank.MISSINGMASS, -1)
					+ (counter == 0 ? " nodenum: "
							+ conf.getLong(PageRank.COUNTNODES, -1) : "")
					+ ")\n\t\tstart phase 2 ... ");

			/* Start algorithm phase 2 */
			job2 = new Phase2Job(conf);
			job2.setJarByClass(PageRank.class);

			FileInputFormat.setInputPaths(job2, PageRank.getPartFiles(conf,
					job1Output));
			job2Output = new Path(conf.get(PageRank.OUTPUTPATH) + "/"
					+ String.valueOf(counter) + ".phase2");
			FileOutputFormat.setOutputPath(job2, job2Output);
			
			if (!job2.waitForCompletion(false))
				throw new Exception("job 2 failed!");

			System.out.println("done");

			if (iterations > 0)
				iterations -= 1;

			counter += 1;
		}
		System.out.println("End iterations");

		return 0;
	}

	/**
	 * Check if the algorithm is converged
	 * 
	 * @param fs
	 * @throws IOException 
	 */
	private boolean isConverged(Configuration conf, FileSystem fs) throws IOException {
		FSDataInputStream converged;
		Path path = new Path(conf.get(PageRank.CONVERGE_FILE));
		if (fs.exists(path)) {
			converged = fs.open(path);
			boolean result = converged.readBoolean();
			//System.out.println("is converged:"+result);
			converged.close();
			return result;
		}
		else {
			//System.out.println("No converge file found");
			return false;
		}

	}

	public static void main(String[] args) throws Exception {
		ToolRunner.run(new PageRank(), args);
	}
}
