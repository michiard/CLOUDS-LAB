package fr.eurecom.dsg.mapreduce.pagerank.utils;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.ArrayList;

import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Writable;

public class NodeWritable implements Writable {

	// private long id;
	private float pageRank, oldPageRank;
	private LongArrayWritable adjacencyList;

	public NodeWritable() {
	}

	public float getOldPageRank() {
		return oldPageRank;
	}

	public NodeWritable(float pageRank, long[] adjacencyList) {
		// this.id = id;
		this.oldPageRank = 0;
		this.pageRank = pageRank;
		this.adjacencyList = new LongArrayWritable();
		LongWritable[] tmp = new LongWritable[adjacencyList.length];
		for (int i = 0; i < adjacencyList.length; i++)
			tmp[i] = new LongWritable(adjacencyList[i]);
		this.adjacencyList.set(tmp);
	}

	public NodeWritable(float pageRank, ArrayList<Long> adjacencyList) {
		// this.id = id;
		this.oldPageRank = 0;
		this.pageRank = pageRank;
		this.adjacencyList = new LongArrayWritable();

		LongWritable[] tmp = new LongWritable[adjacencyList.size()];
		for (int i = 0; i < adjacencyList.size(); i++)
			tmp[i] = new LongWritable(adjacencyList.get(i));
		this.adjacencyList.set(tmp);
	}

	public NodeWritable(float pageRank, float oldPageRank,
			ArrayList<Long> adjacencyList) {
		this(pageRank, adjacencyList);
		this.oldPageRank = oldPageRank;
	}

	public float getPageRank() {
		return this.pageRank;
	}

	public void setPageRank(float pageRank) {
		// this.oldPageRank = this.pageRank;
		this.pageRank = pageRank;
	}

	public void setOldPageRank(float oldPageRank) {
		this.oldPageRank = oldPageRank;
	}

	public LongArrayWritable getAdjacencyList() {
		return this.adjacencyList;
	}

	public boolean isConverged(float threshold) {
		float diff = Math.abs(this.pageRank - this.oldPageRank);
//		System.out.println("oldPageRank:" + this.oldPageRank + " pageRank:"
//				+ this.pageRank + " diff:" + diff + " threshold:" + threshold
//				+ " (diff <= threshold):" + (diff <= threshold));
		return diff <= threshold;
	}

	@Override
	public void readFields(DataInput in) throws IOException {
		this.pageRank = in.readFloat();
		this.oldPageRank = in.readFloat();
		this.adjacencyList = new LongArrayWritable();
		this.adjacencyList.readFields(in);

	}

	@Override
	public String toString() {
		String adjlist = "";
		for (Writable neighbor : this.adjacencyList.get())
			adjlist += (adjlist == "" ? "" : ",")
					+ ((LongWritable) neighbor).get();

		return "(" + this.pageRank + "," + this.oldPageRank + "):" + adjlist;
	}

	@Override
	public void write(DataOutput out) throws IOException {
		// out.writeLong(this.id);
		out.writeFloat(this.pageRank);
		out.writeFloat(this.oldPageRank);
		this.adjacencyList.write(out);
	}

	public static void main(String[] args) {
		ArrayList<Long> adjlist = new ArrayList<Long>();
		adjlist.add(new Long(3));
		adjlist.add(new Long(4));
		NodeWritable node = new NodeWritable(0, 0.1f, adjlist);
		System.out.println(node);
	}
}
