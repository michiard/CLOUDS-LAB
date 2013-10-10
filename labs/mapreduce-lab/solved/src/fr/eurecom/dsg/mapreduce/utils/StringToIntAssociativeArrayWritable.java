package fr.eurecom.dsg.mapreduce.utils;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.hadoop.io.Writable;
/*
 * Very simple (and scholastic) implementation of a Writable associative array for String to Int 
 *
 **/
public class StringToIntAssociativeArrayWritable implements Writable {
	private HashMap<String, Integer> hm = new HashMap<String, Integer>();

	public void clear() {
		hm.clear();
	}

	public String toString() {
		return hm.toString();
	}

	@Override
	public void readFields(DataInput in) throws IOException {
		int len = in.readInt();
		hm.clear();
		for(int i=0; i<len; i++) {
			int l = in.readInt();
			byte[] ba = new byte[l];
			in.readFully(ba);
			String key = new String(ba);
			Integer value = in.readInt();
			hm.put(key, value);
		}
	}

	@Override
	public void write(DataOutput out) throws IOException {
		out.writeInt(hm.size());
		Iterator<Entry<String, Integer>> it = hm.entrySet().iterator();

		while (it.hasNext()) {
			Map.Entry<String,Integer> pairs = (Map.Entry<String,Integer>)it.next();
			String k = (String) pairs.getKey();
			Integer v = (Integer)pairs.getValue();	        
			out.writeInt(k.length());
			out.writeBytes(k);
			out.writeInt(v);
		}
	}

	public void increment(String t) {
		int count = 1;
		if(hm.containsKey(t)) {
			count = hm.get(t) + count;
		}
		hm.put(t, count);
	}

	public void increment(String t, int value) {
		int count = value;
		if(hm.containsKey(t)) {
			count = hm.get(t) + count;
		}
		hm.put(t, count);
	}

	public void sum(StringToIntAssociativeArrayWritable h) {
		Iterator<Entry<String, Integer>> it = h.hm.entrySet().iterator();
		while (it.hasNext()) {
			Map.Entry<String,Integer> pairs = (Map.Entry<String,Integer>)it.next();
			String k = (String) pairs.getKey();
			Integer v = (Integer)pairs.getValue();
			increment(k, v);
		}
	}
}
