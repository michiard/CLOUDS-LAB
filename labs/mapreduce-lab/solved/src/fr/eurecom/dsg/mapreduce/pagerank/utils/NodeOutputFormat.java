package fr.eurecom.dsg.mapreduce.pagerank.utils;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FSDataOutputStream;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.io.compress.CompressionCodec;
import org.apache.hadoop.io.compress.GzipCodec;
import org.apache.hadoop.mapreduce.RecordWriter;
import org.apache.hadoop.mapreduce.TaskAttemptContext;
import org.apache.hadoop.mapreduce.lib.output.TextOutputFormat;
import org.apache.hadoop.util.ReflectionUtils;

public class NodeOutputFormat extends
		TextOutputFormat<LongWritable, NodeWritable> {

	public static class NodeRecordWriter extends
			LineRecordWriter<LongWritable, NodeWritable> {

		public NodeRecordWriter(DataOutputStream out) {
			super(out);
		}

		public NodeRecordWriter(FSDataOutputStream fileOut,
				String keyValueSeparator) {
			super(fileOut, keyValueSeparator);
		}

		public NodeRecordWriter(DataOutputStream dataOutputStream,
				String keyValueSeparator) {
			super(dataOutputStream, keyValueSeparator);
		}

		private static final String utf8 = "UTF-8";
		private static final byte[] newline;
		static {
			try {
				newline = "\n".getBytes(utf8);
			} catch (UnsupportedEncodingException uee) {
				throw new IllegalArgumentException("can't find " + utf8
						+ " encoding");
			}
		}

		/**
		 * Write the object to the byte stream, handling Text as a special case.
		 * 
		 * @param o
		 *            the object to print
		 * @throws IOException
		 *             if the write throws, we pass it on
		 */
		private void writeObject(Object o) throws IOException {
			if (o instanceof Text) {
				Text to = (Text) o;
				out.write(to.getBytes(), 0, to.getLength());
			} else {
				out.write(o.toString().getBytes(utf8));
			}
		}

		@Override
		public synchronized void write(LongWritable key, NodeWritable value)
				throws IOException {
			out.write(String.valueOf(key.get()).getBytes(utf8));
			writeObject(value);
			out.write(newline);
		}

	}

	@Override
	public RecordWriter<LongWritable, NodeWritable> getRecordWriter(
			TaskAttemptContext job) throws IOException, InterruptedException {

		Configuration conf = job.getConfiguration();
		boolean isCompressed = getCompressOutput(job);
		String keyValueSeparator = conf.get(
				"mapred.textoutputformat.separator", "\t");
		CompressionCodec codec = null;
		String extension = "";
		if (isCompressed) {
			Class<? extends CompressionCodec> codecClass = getOutputCompressorClass(
					job, GzipCodec.class);
			codec = (CompressionCodec) ReflectionUtils.newInstance(codecClass,
					conf);
			extension = codec.getDefaultExtension();
		}
		Path file = getDefaultWorkFile(job, extension);
		FileSystem fs = file.getFileSystem(conf);
		if (!isCompressed) {
			FSDataOutputStream fileOut = fs.create(file, false);
			return new NodeRecordWriter(fileOut, keyValueSeparator);
		} else {
			FSDataOutputStream fileOut = fs.create(file, false);
			return new NodeRecordWriter(new DataOutputStream(codec
					.createOutputStream(fileOut)), keyValueSeparator);
		}
	}

}
