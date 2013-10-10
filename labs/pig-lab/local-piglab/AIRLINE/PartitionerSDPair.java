package mypartitioners;
import java.util.HashMap;
import java.util.Map.Entry;
import org.apache.hadoop.mapreduce.Partitioner;
import org.apache.pig.data.Tuple;
import org.apache.pig.impl.io.NullableTuple;
import org.apache.hadoop.io.Writable;
/**
 * 
 */

public class PartitionerSDPair extends Partitioner<NullableTuple, Writable> {
	public int getPartition(NullableTuple key, Writable value, int numPartitions) {
		try {
			Tuple t = (Tuple)key.getValueAsPigType();
			String s = (String)t.get(0);
			return s.hashCode() % numPartitions;
		} catch(Exception e) {
			throw new RuntimeException();
		}
	}
}

