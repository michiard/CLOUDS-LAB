package fr.eurecom.dsg.mapreduce;
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
public class StringToIntAssociativeArray implements Writable {
  
  // TODO: add an internal field that is the real associative array

  @Override
  public void readFields(DataInput in) throws IOException {
    
    // TODO: implement serialization
  }

  @Override
  public void write(DataOutput out) throws IOException {

    // TODO: implement deserialization
  }
}
