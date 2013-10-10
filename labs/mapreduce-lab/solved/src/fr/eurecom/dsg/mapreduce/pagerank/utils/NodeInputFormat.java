package fr.eurecom.dsg.mapreduce.pagerank.utils;

import java.io.IOException;
import java.util.ArrayList;

import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.mapreduce.InputSplit;
import org.apache.hadoop.mapreduce.RecordReader;
import org.apache.hadoop.mapreduce.TaskAttemptContext;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.input.LineRecordReader;

/** 
 * Read the outgoing link list from a file. Every line should be formatted
 * as
 *  
 *    nodeid(pagerank[,oldpagerank]):[node1,node2,..]
 *    
 */
public class NodeInputFormat extends
		FileInputFormat<LongWritable, NodeWritable> {

	@Override
	public RecordReader<LongWritable, NodeWritable> createRecordReader(
			InputSplit split, TaskAttemptContext context) throws IOException,
			InterruptedException {

		return new AdjacencyListRecordReader();
	}

	public static class AdjacencyListRecordReader extends
			RecordReader<LongWritable, NodeWritable> {

		protected LineRecordReader recordReader;

		public AdjacencyListRecordReader() {
			this.recordReader = new LineRecordReader();
		}

		@Override
		public void close() throws IOException {
			this.recordReader.close();
		}

		@Override
		public LongWritable getCurrentKey() throws IOException,
				InterruptedException {
			String text = this.recordReader.getCurrentValue().toString();
			int sepindex = text.indexOf("(");
			if (sepindex == -1)
				throw new IOException("Token ( not found");

			return new LongWritable(Long.valueOf(text.substring(0, sepindex)));
		}

		@Override
		public NodeWritable getCurrentValue() throws IOException,
				InterruptedException {
			String text = this.recordReader.getCurrentValue().toString();
			// System.out.println("text:"+text);

			int openPar = text.indexOf('(');
			if (openPar == -1)
				throw new IOException("Token ( not found");
			int closePar = text.indexOf(')', openPar);
			if (closePar == -1)
				throw new IOException("Token ) not found");
			int sepindex = text.indexOf(":", closePar);
			int optionalComma = text.substring(openPar, closePar).indexOf(",");
			if (optionalComma != -1)
				optionalComma += openPar;
			if (sepindex == -1)
				throw new IOException("Token : not found");
//
//			System.out.println("text: '" + text + "'\topenPar:" + openPar
//					+ " optionalComma:" + optionalComma + " closePar:"
//					+ closePar);

			this.getCurrentKey(); // jump over the key

			float pageRank = Float.valueOf(text.substring(openPar + 1,
					optionalComma == -1 ? closePar : optionalComma));

			Float oldPageRank = null;
			if (optionalComma != -1)
				oldPageRank = Float.valueOf(text.substring(optionalComma + 1,
						closePar));

			String[] sneighbors = text.substring(sepindex + 1).split(",");
			ArrayList<Long> neighbors = new ArrayList<Long>();

			for (int i = 0; i < sneighbors.length; i++) {
				String neighbor = sneighbors[i].trim();
				for (Character c : neighbor.toCharArray())
					if (!Character.isDigit(c))
						continue;
				if (!neighbor.isEmpty())
					neighbors.add(Long.valueOf(neighbor).longValue());
			}

			if (oldPageRank == null)
				return new NodeWritable(pageRank, neighbors);
			else
				return new NodeWritable(pageRank, oldPageRank,
						neighbors);
		}

		@Override
		public float getProgress() throws IOException, InterruptedException {
			return this.recordReader.getProgress();
		}

		@Override
		public void initialize(InputSplit split, TaskAttemptContext context)
				throws IOException, InterruptedException {
			this.recordReader.initialize(split, context);
		}

		@Override
		public boolean nextKeyValue() throws IOException, InterruptedException {
			return this.recordReader.nextKeyValue();
		}

	}
}
