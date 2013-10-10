package fr.eurecom.dsg.hbase;
import org.apache.commons.lang.ArrayUtils;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.HBaseConfiguration;
import org.apache.hadoop.hbase.client.Get;
import org.apache.hadoop.hbase.client.HTable;
import org.apache.hadoop.hbase.client.Result;
import org.apache.hadoop.hbase.client.ResultScanner;
import org.apache.hadoop.hbase.client.Scan;
import org.apache.hadoop.hbase.util.Bytes;



public class QueryTwo {
	public static final byte[] DATACF = Bytes.toBytes("data");
	
	public static final byte[] URL = Bytes.toBytes("url");	
	public static final byte[] TOTALCLICKS = Bytes.toBytes("totalClicks");
	
	public static final byte[] ZERO = new byte[] { 0 };
	public static final byte[] ONE = new byte[] { 1 };
	
	public static final String shorturltable = "shorturl-test";
	public static final String usershorturltable = "user-shorturl-test";
	
	
	/* This function is not used in this code, but it is included in order to understand how the
	 * row keys for the user-shorturl table are created.
	 */
	private static byte[] getUserShorturlKey(String username, String shorturl, Long timestamp) {
		/* .The row key for user-shorturl table is a composed row key, made by the username, a 
		 * byte (0) as separetor, concatendated with the reversed timestamp (Long.MAX_LONG - timestamp), 
		 * which is zero left padded, concated with the shortId:
		 *
		 * KEY = USERNAME \0 REVERSEDTIMESTAMP SHORTID 
		 * 
		 * 
		 * This composed key allow use to look up for all the shortids created by a given user,
		 * since a partial match on a part of the key is possible. Moreover, since keys are ordered
		 * alphabetically, the reversed timestamp (Long.MAX_LONG - timestamp) allows to save the 
		 * data ordered by timestamp from the newest to the oldest. The function that is used to 
		 * generate the key is included in the source (getUserShorturlKey), even if it is never used.
		 */
		
		byte[] reverseTimestamp = Bytes.toBytes((Long.MAX_VALUE - timestamp));//I want timestamp sorted from newest to oldest.		
		reverseTimestamp = Bytes.padHead(reverseTimestamp, 8-reverseTimestamp.length); //I could need to scan through timestamp ranges, so I need it left-padded with zeros.		
		byte[] key = Bytes.add(Bytes.toBytes(username), ZERO); //I do not need to scan through usernames ranges, so it is useless right-padding it
		key = Bytes.add(key, reverseTimestamp);
		key = Bytes.add(key,  Bytes.toBytes(shorturl));
		
		return key;
	}

	public static void main(String[] args) throws Exception {
		String username = "cainarachi";
		if(args.length == 1) {
			username = args[0];
		}
		
		//We need to create a new HBaseConfiguration
		Configuration conf = HBaseConfiguration.create();
		//If you add the hbase configuration directory to the classpath, you do not need to specify the following properties
		conf.set("hbase.zookeeper.quorum", "10-12-12-7.openstacklocal");
//		conf.set("hbase.zookeeper.property.clientPort", "2222");
		
		//We need to instantiate a new HTable object for each table we want to communicate
		HTable shorturlTable = new HTable(conf, shorturltable);
		HTable userShorturlTable = new HTable(conf, usershorturltable);
		
		/* In order to find all shorturls belonging to a given user, we need to use the
		 * table usershorturl. It is composed by a single column familiy and a 
		 * composed row key, made by:
		 * KEY = USERNAME \0 REVERSEDTIMESTAMP SHORTURL 
		 * 
		 * This composed key allow use to look up for all the shorturls created by a given user,
		 * since a partial match on a part of the key is possible. Moreover, since keys are ordered
		 * alphabetically, the reversed timestamp (Long.MAX_LONG - timestamp) allows to save the 
		 * data ordered by timestamp. The function that is used to generate the key is included 
		 * in the source (getUserShorturlKey), even if it is never used.
		 * 
		 * In order to iterate on all the key/values for each user, we need to use a Scanner, that is
		 * similar to a SQL cursors. Scanners allow to start from a partial match of the key.
		 * It will start from the first row key that is equal to or larger than the given start row key.
		 * The scanner will have the username we are looking for as starting row.
		 * Since usernames are separated by a \0 byte from the remaining part of the row key, the scanner
		 * will have the "username\1" as the ending row key.
		 * 
		 * In the following code we will iterate on the scanner's results, and:
		 * 1. we will extract the timestamp and shortId from the row key
		 * 2. we will get the url from the corresponding value
		 * 3. we will issue a second query on shorturl table in order to get the total
		 *    number of clicks.
		 * */
		
	    byte[] startRow = Bytes.toBytes(username);
	    byte[] stopRow = Bytes.add(startRow, ONE);

	    Scan scan = new Scan(startRow, stopRow);
		scan.addFamily(DATACF);
		
	    ResultScanner resscan = userShorturlTable.getScanner(scan);
	    
	    byte[] username_bytes = Bytes.toBytes(username);
	    
	    for (Result res : resscan) {
	    	byte[] rowKey = res.getRow();
	    	
	    	/* Extract the timestamp.
	    	 * The timestamp starts after the username and the \0 byte, and it is a long, 
	    	 * which occupies 8 bytes.
	    	 * The value stored in HBase is (Long.MAX_VALUE - timestamp), so we need to do
	    	 * the reverse operation to get the original one.
	    	 * java.util.Date is a class that is used to represent the timestamp in a human
	    	 * readeable way.
	    	 */
	    	
	    	long timestamp = Bytes.toLong(rowKey, username_bytes.length + 1, 8);
	    	timestamp = Long.MAX_VALUE - timestamp;
	    	java.util.Date date = new java.util.Date(timestamp*1000);//timestamp is in seconds, Date requires milliseconds
	    	
	    	// The shortId starts after the timestamp and stops at the end of the row key. 
	    	byte[] shortId_bytes = ArrayUtils.subarray(rowKey, username_bytes.length + 8 + 1, rowKey.length);
	    	String shortId = Bytes.toString(shortId_bytes);
	    	
	    	// The url is the value contained in the URL field.
	    	byte[] url_byte = res.getValue(DATACF, URL);
	    	String url = Bytes.toString(url_byte);
	    	
	    	/* The total number of clicks is saved in the shorturl table, so, using the shortId, we can
	    	 * issue a query to it to get the number of clicks. This query is similar to the one in QueryOne.java.
	    	 */
	    	
	    	// TODO: write the code to issue a get request and retrieve the total number of clicks
	    	// In case the shorturl is never accessed, no totalClicks field exists for it
	    	
	    	System.out.println("shortUrl " + shortId + " --> " + url + "  -  Created on: " + date + " - Total number of clicks: " + totalClicks);
	    }
	    
	    shorturlTable.close();
	    userShorturlTable.close();
	}
}
