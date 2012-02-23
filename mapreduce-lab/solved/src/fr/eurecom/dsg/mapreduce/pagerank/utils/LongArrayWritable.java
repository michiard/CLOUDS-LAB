package fr.eurecom.dsg.mapreduce.pagerank.utils;

import org.apache.hadoop.io.ArrayWritable;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Writable;

public class LongArrayWritable extends ArrayWritable {
	
	private int length;

	public LongArrayWritable() {
		super(LongWritable.class);
	}
	
    public LongArrayWritable(LongWritable[] values) {
        super(LongWritable.class, values);
    }

	public int getLength() {
		return length;
	}

	@Override
	public void set(Writable[] values) {
		super.set(values);
		this.length = values.length;
	}

	@Override
	public String toString() {
		LongWritable[] elems = (LongWritable[])this.get();
		String res = "[";
		for (int i = 0; i < this.length; i++) {
			if (res.length() != 1)
				res += ',';
			res += elems[i].get();
		}
		res += ']';
		return res;
	}
}
