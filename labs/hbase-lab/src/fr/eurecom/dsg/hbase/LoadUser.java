package fr.eurecom.dsg.hbase;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.InputStreamReader;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.HBaseConfiguration;
import org.apache.hadoop.hbase.client.HTable;
import org.apache.hadoop.hbase.client.Put;
import org.apache.hadoop.hbase.util.Bytes;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

/* This is a simple example program that loads the data from user.json into a HBase table.
 * user.json contains, in each line, the JSON representation of a user object.
 * In particular, it is an unordered collection of key:value pairs with the ':' character
 * separating the key and the value, comma-separated and enclosed in curly braces
 * 
 * Ex. {"username": "bluestangle", "credentials": "paRRezA1", "role": "user", "email": "byBalsam@embarqmail.com", "lastname": "RIGGS"}
 * */

public class LoadUser {

	public static final byte[] DATACF = Bytes.toBytes("data");
	
	public static final byte[] TIMESTAMP = Bytes.toBytes("timestamp");
	public static final byte[] LASTNAME = Bytes.toBytes("lastname");
	public static final byte[] CREDENTIALS = Bytes.toBytes("credentials");
	public static final byte[] ROLE = Bytes.toBytes("role");
	public static final byte[] EMAIL = Bytes.toBytes("email");

	public static void main(String[] args) throws Exception {
		if (args.length !=2) {
			System.err.println("Usage: LoadUser user.json tablename");
			
		}
		
		/* This is the java boilerplate used to read a textual file 
		 */		
		FileInputStream fstream = new FileInputStream(args[0]);
		DataInputStream in = new DataInputStream(fstream);
		BufferedReader br = new BufferedReader(new InputStreamReader(in));
		
		//We need to create a new HBaseConfiguration. 		
		Configuration conf = HBaseConfiguration.create();
		//If you add the hbase configuration directory to the classpath, you do not need to specify the following properties
		conf.set("hbase.zookeeper.quorum", "10-10-12-7.openstacklocal");
//		conf.set("hbase.zookeeper.property.clientPort", "2222");
		
		//We need to instantiate a new HTable object, in order to communicate with a HBase table.
		HTable table = new HTable(conf, Bytes.toBytes(args[1]));
		
		//Since data is formatted using JSON, we need to load a new JSONParser, to parse each line.
		JSONParser parser=new JSONParser();
		
		/* We are reading the input file, user.json, line by line, parsing each line with the JSON parser and putting the data in HBASE
		 * Note that the following code reads the whole file: exercise 1, question 3 requires you to read only 100 lines
		 * */
		String strLine;
		while ((strLine = br.readLine()) != null)   {
			JSONObject obj=(JSONObject)parser.parse(strLine);
			//{"lastname": "HOLLAND", "password": "TnhAgHtI", "role": "user", "user": "bluestangle", "email": "bluestangle@keithtv.com"}

			//This code is used to grab the values of each field in the user object:
			String username = (String)obj.get("username");
			String lastname = (String)obj.get("lastname");
			String password = (String)obj.get("credentials");
			String role = (String)obj.get("role");
			String email = (String)obj.get("email");
			
			/* In order to insert data into HBase, we need to create a new Put object.
			 * It is instantiated using the row-key, then we add all the needed field
			 */
			Put put = new Put(Bytes.toBytes(username));
			put.add(DATACF, LASTNAME, Bytes.toBytes(lastname));
			put.add(DATACF, CREDENTIALS, Bytes.toBytes(password));
			put.add(DATACF, ROLE, Bytes.toBytes(role));
			put.add(DATACF, EMAIL, Bytes.toBytes(email));
			// Finally the data is loaded into HBase
			table.put(put);
		}
		table.close();
		
		in.close();
		
	}
}

