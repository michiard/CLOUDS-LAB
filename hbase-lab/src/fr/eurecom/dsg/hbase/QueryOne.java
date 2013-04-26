package fr.eurecom.dsg.hbase;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.HBaseConfiguration;
import org.apache.hadoop.hbase.client.Get;
import org.apache.hadoop.hbase.client.HTable;
import org.apache.hadoop.hbase.client.Result;
import org.apache.hadoop.hbase.util.Bytes;

/* This simple java program shows how to lookup the url associated to a given shortID
 * 
 * */

public class QueryOne {
	
	public static final byte[] DATACF = Bytes.toBytes("data");	
	public static final byte[] URL = Bytes.toBytes("url");		
	public static final String shorturltable = "shorturl-test";
	
	public static void main(String[] args) throws Exception {
		String shortId = "6PMQ";
		if(args.length == 1) {
			shortId = args[0];
		}

		//We need to create a new HBaseConfiguration.
		Configuration conf = HBaseConfiguration.create();
		//If you add the hbase configuration directory to the classpath, you do not need to specify the following properties
		conf.set("hbase.zookeeper.quorum", "10-10-12-7.openstacklocal");
//		conf.set("hbase.zookeeper.property.clientPort", "8164");
		
		//We need to instantiate a new HTable object, in order to communicate with HBase table 'shorturl-test'.
		HTable shorturlTable = new HTable(conf, shorturltable);		
		
		/* In order to perform Get operations on a single row, we need a Get object.
		 * It is instantiated using the exact row-key corresponding to the shortId 
		 * we are looking for.
		 * Since we need just the URL field, we specify it using addColumn 
		 * */
		Get get = new Get(Bytes.toBytes(shortId));
		get.addColumn(DATACF, URL);
		
		// We issue the query to the shorturl table. Results are contained in result.
		Result result = shorturlTable.get(get);
		
		//We need to extract the required fields from the result object, and to convert them into
		//the appropriate type, since they are just byte[]
		byte[] url_bytes = result.getValue(DATACF, URL);
		String url = Bytes.toString(url_bytes);
		
		System.out.println("ShortID " + shortId + " --> "+ url);
		
		shorturlTable.close();
	}
}
