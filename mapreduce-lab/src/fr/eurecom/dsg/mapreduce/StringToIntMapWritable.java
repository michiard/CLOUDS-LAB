package fr.eurecom.dsg.mapreduce;
import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map.Entry;

import org.apache.hadoop.io.Writable;

import fr.eurecom.dsg.mapreduce.utils.StringUtils;
/*
 * Very simple (and scholastic) implementation of a Writable associative array for String to Int 
 *
 **/
public class StringToIntMapWritable implements Writable {

  HashMap<String, Integer> internalMap = new HashMap<String, Integer>();
  long internalMapSize = 0l;
 
  public void addOrIncr(String string) {
    if (!this.internalMap.containsKey(string)) {
      this.internalMap.put(string, 1);
      long bytes = StringUtils.getByteSize(string) + 4;
      bytes += 8 - (bytes % 8);
      this.internalMapSize += bytes;
    }
    else {
      this.internalMap.put(string, 1 + this.internalMap.get(string));
    }
  }
  
  @Override
  public void readFields(DataInput in) throws IOException {
    this.internalMap.clear();
    this.internalMapSize = 0l;
    int size = in.readInt();
    for (int i = 0; i < size; i++) {
      String key = in.readUTF();
      int counter = in.readInt();
      long bytes = StringUtils.getByteSize(key) + 4;
      bytes += 8 - (bytes % 8);
      this.internalMapSize += bytes;
      this.internalMap.put(key, counter);
    }
  }

  @Override
  public void write(DataOutput out) throws IOException {
    out.writeInt(this.internalMap.size());
    for (Entry<String, Integer> entry : this.internalMap.entrySet()) {
      out.writeUTF(entry.getKey());
      out.writeInt(entry.getValue());
    }
  }
}
