# TSTAT Trace Analysis with Pig

This series of exercises is inspired by "real-world" problems related to the analysis of TCP flows captured on an operational network. In particular, we focus on TSTAT as the tool to capture network traffic, which is described below.

## TSTAT DATA
Exercises are based on traces in Tstat format (see http://tstat.tlc.polito.it/index.shtml).

Tstat produces two files, "log_tcp_complete" and "log_tcp_nocomplete" files which log every TCP connection that has been tracked by a measurement probe. A TCP connection is identified when the first SYN segment is observed, and is ended when either:
* the FIN/ACK or RST segments are observer;
* no data packet has been observed (from both sides) for a default timeout of 10 s after the three-way handshake or 5 min after the last data packet.

Tstat discards all the connections for which the three way handshake is not properly seen. Then, in case a connection is correctly closed, it is stored in log_tcp_complete, otherwise in log_tcp_nocomplete.

In the following exercises we will use two files called **tstat-big.txt** and **tstat-sample.txt**, which contain only correctly closed connections. For EURECOM students, such files are located in HDFS under the usual ```/laboratory/input/``` directory.

Each file consists of a line per each TCP connection; each line consists of fields, separated by spaces. Columns are grouped according to C2S - Client-to-Server and S2C - Server-to-Client traffic directions. The exact TSTAT file format is reported in the table immediately below.

Note that the ''tstat-analysis'' folder contains a pig file with the LOAD command used to load the TSTAT file with the appropriate schema, "*load.pig*".

Below, a description of the "schema" of a TSTAT file:

________________

| C2S | S2C | Short Description | Unit   | Long Description  |
|:---:|-----|:-----------------:|--------|------------------:|
|1|45|Client/Server IP addr|-|IP addresses of the client/server|
|2|46|Client/Server TCP port|-|TCP port addresses for the client/server|
|3|47|packets|-|total number of packets observed form the client/server|
|4|48|RST sent|0/1|0 = no RST segment has been sent by the client/server|
|5|49|ACK sent|-|number of segments with the ACK field set to 1|
|6|50|PURE ACK sent|-|number of segments with ACK field set to 1 and no data|
|7|51|unique bytes|bytes|number of bytes sent in the payload|
|8|52|data pkts|-|number of segments with payload|
|9|53|data bytes|bytes|number of bytes transmitted in the payload, including retransmissions|
|10|54|rexmit pkts|-|number of retransmitted segments|
|11|55|rexmit bytes|bytes|number of retransmitted bytes|
|12|56|out seq pkts|-|number of segments observed out of sequence|
|13|57|SYN count|-|number of SYN segments observed (including rtx)|
|14|58|FIN count|-|number of FIN segments observed (including rtx)|
|15|59|RFC1323 ws|0/1|Window scale option sent|
|16|60|RFC1323 ts|0/1|Timestamp option sent|
|17|61|window scale|-|Scaling values negotiated [scale factor]|
|18|62|SACK req|0/1|SACK option set|
|19|63|SACK sent|-|number of SACK messages sent|
|20|64|MSS|bytes|MSS declared|
|21|65|max seg size|bytes|Maximum segment size observed|
|22|66|min seg size|bytes|Minimum segment size observed|
|23|67|win max|bytes|Maximum receiver window announced (already scale by the window scale factor)|
|24|68|win min|bytes|Maximum receiver windows announced (already scale by the window scale factor)|
|25|69|win zero|-|Total number of segments declaring zero as receiver window|
|26|70|cwin max|bytes|Maximum in-flight-size computed as the difference between the largest sequence number so far, and the corresponding last ACK message on the reverse path. It is an estimate of the congestion window|
|27|71|cwin min|bytes|Minimum in-flight-size|
|28|72|initial cwin|bytes|First in-flight size, or total number of unack-ed bytes sent before receiving the first ACK segment|
|29|73|Average rtt|ms|Average RTT computed measuring the time elapsed between the data segment and the corresponding ACK|
|30|74|rtt min|ms|Minimum RTT observed during connection lifetime|
|31|75|rtt max|ms|Maximum RTT observed during connection lifetime|
|32|76|Stdev rtt|ms|Standard deviation of the RTT|
|33|77|rtt count|-|Number of valid RTT observation|
|34|78|ttl_min|-|Minimum Time To Live|
|35|79|ttl_max|-|Maximum Time To Live|
|36|80|rtx RTO|-|Number of retransmitted segments due to timeout expiration|
|37|81|rtx FR|-|Number of retransmitted segments due to Fast Retransmit (three dup-ack)|
|38|82|reordering|-|Number of packet reordering observed|
|39|83|net dup|-|Number of network duplicates observed|
|40|84|unknown|-|Number of segments not in sequence or duplicate which are not classified as specific events|
|41|85|flow control|-|Number of retransmitted segments to probe the receiver window|
|42|86|unnece rtx RTO|-|Number of unnecessary transmissions following a timeout expiration|
|43|87|unnece rtx FR|-|Number of unnecessary transmissions following a fast retransmit|
|44|88|!= SYN seqno|0/1|1 = retransmitted SYN segments have different initial seqno|
|89||Completion time|ms|Flow duration since first packet to last packet|
|90||First time|ms|Flow first packet since first segment ever|
|91||Last time|ms|Flow last segment since first segment ever|
|92||C first payload|ms|Client first segment with payload since the first flow segment|
|93||S first payload|ms|Server first segment with payload since the first flow segment|
|94||C last payload|ms|Client last segment with payload since the first flow segment|
|95||S last payload|ms|Server last segment with payload since the first flow segment|
|96||C first ack|ms|Client first ACK segment (without SYN) since the first flow segment|
|97||S first ack|ms|Server first ACK segment (without SYN) since the first flow segment|
|98||First time abs|ms|Flow first packet absolute time (epoch)|
|99||C Internal|0/1|1 = client has internal IP, 0 = client has external IP|
|100||S Internal|0/1|1 = server has internal IP, 0 = server has external IP|
|101||Connection type|-|Bitmask stating the connection type as identified by TCPL7 inspection engine (see protocol.h)|
||||||
|102||P2P type|-|Type of P2P protocol, as identified by the IPP2P engine (see ipp2p_tstat.h)|
|103||P2P subtype|-|P2P protocol message type, as identified by the IPP2P engine (see ipp2p_tstat.c)|
|104||ED2K Data|-|For P2P ED2K flows, the number of data messages|
|105||ED2K Signaling|-|For P2P ED2K flows, the number of signaling (not data) messages|
|106||ED2K C2S|-|For P2P ED2K flows, the number of client<->server messages|
|107||ED2K C2C|-|For P2P ED2K flows, the number of client<->client messages|
|108||ED2K Chat|-|For P2P ED2K flows, the number of chat messages|
||||||
|109||HTTP type|-|For HTTP flows, the identified Web2.0 content (see the http_content enum in struct.h)|
||||||
|110||SSL Client Hello|-|For SSL flows, the server name indicated by the client in the Hello message extensions|
|111||SSL Server Hello|-|For SSL flows, the subject CN name indicated by the server in its certificate|
|112||Dropbox ID|-|Dropbox identifier of user device|
|113||FQDN|-|Full Qualified Domain Name contacted|

## Exercise 1: A “Network” Word Count
**Problem statement:** count the number of TCP connections per each **client** IP.

The problem is very similar to the Word Count problem of the MapReduce lab and Pig lab: it has been conceived to get you familiar with the TSTAT data.

### Sample code

The following lines of code can be used in the interactive pig shell (grunt) with some minor modification:


 ```pig
-- Load input data from local input directory
A = LOAD 'tstat-sample.txt' using PigStorage(' ') AS (ip_c:chararray, ….);


-- Group by client IP
B = GROUP A BY ip_c;


-- Generate the output data
C = FOREACH B GENERATE group, COUNT(A);


-- Store the output (and start to execute the script)
STORE C INTO 'output/ex1';
 ```


### Executing the script
Note that the pig script you wrote for local execution requires some modifications to be run on the cluster (in ```-x mapreduce``` mode):
* Change the input path to the appropriate one. You can browse the local HDFS filesystem in order to find the tstat input files.
* Change the output path: note that Hadoop refuses to overwrite any local directory when you define the path, so, remember to use a different name or to delete the output directory for each run of your script.
* Set parallelism where appropriate, using the **PARALLEL** keyword. This is intentionally left for the student to complete.
You can also use another troubleshooting instrument to understand the logical, physical and execution plans produced by Pig:
* **EXPLAIN** relation: note that you can generate output files in the "dot" format for better rendering
How-to inspect your results and check your job in the cluster
* Inspect your job status on the cluster: you can identify your job by name and check its status using the Web UI of the JobTracker so far.

### Exercise 1/B
**Problem statement:** count the number of TCP connection per each IP.

**Hint:** each TCP connection has a client IP and a server IP. For this exercise you need to count the number of connection per each IP, irrespective if client or server IP.


**Questions:**

1. How is this exercise different from Ex. 1?
2. How would you write it in Java?
3. Elaborate on the differences between Ex.1 and Ex.1/b

### Exercise 1/C
**Problem statement:** Assuming you have a very long trace, or -- better -- a large number of traces collected each day, count the number of TCP connection per each client IP, at each of the following time granularities: hour, day, week, month, year...

**Hint:** note that recent versions of Pig expose ```CUBE``` and ```ROLLUP``` operators. You may want to try writing your own script, and then try using such operators.


## Exercise 2
**Problem statement:** count the total number of TCP connection having, e.g., “google.it” in the FQDN (Fully Qualified Domain Name) field (field #113 in the tstat data).

**Hint:** You need to modify the code of the previous exercise, filtering the loaded data, and applying a different grouping.

## Exercise 3
**Problem statement:**  for each client IP, compute the sum of uploaded, downloaded and total (up+down) transmitted bytes.


## Exercise 4
**Problem statement:** find the top 100 users per uploaded bytes.


**Hint:** there are different ways of solving this exercise, in particular, in recent PIG versions there is a function called TOP, that returns the top-n tuples from a bag of tuples (see http://pig.apache.org/docs/r0.11.1/func.html#topx).


**Questions:**

1. Is this job map-only? Why? Why not?
2. Where did you apply the TOP function?
3. Can you explain how does the TOP function work?
4. The **TOP** function was introduced since PIG v.0.8. How, in your opinion, and based on your understanding of PIG, was the query answered before the TOP command was available? Do you think that it was less efficient than the current implementation?


## Exercise 5
**Problem statement:** For each IP in the top 100 list previously computed, find the number of bytes uploaded by the largest TCP connection and the percentage of the bytes uploaded by this connection over the total number of uploaded bytes.


**Hint:** for this exercise, you need to join two datasets: the output of the previous exercise with the original tstat dataset.


** Questions:**

1. How many jobs were generated?
2. Describe how the join is executed.

## Exercise 6
**Problem statement:** find the minimum value of client Maximum Segment Size MSS (mss_c).

> The maximum segment size (MSS) is a parameter of the TCP protocol that specifies the largest amount of data, specified in octets, that a computer or communications device can receive in a single TCP segment, and therefore in a single IP datagram. It does not count the TCP header or the IP header.[1] The IP datagram containing a TCP segment may be self-contained within a single packet, or it may be reconstructed from several fragmented pieces; either way, the MSS limit applies to the total amount of data contained in the final, reconstructed TCP segment. Therefore: Headers + MSS ≤ MTU. (wikipedia.org)

**Questions:**

1. Did you obtain any ''"strange"'' values?
2. What did you learn from this exercise? Is your data generally ''"clean"''?


## Exercise 7
**Problem statement:** find the percentage of TCP connections with minimum client window (win_min_c) == 1460 over the total number of connections.

**Questions:**

1. How many MR jobs were generated by PIG?
2. How many reducers were launched per each job? (did you use the PARALLEL keyword?)


## Exercise 8
**Problem statement:** calculate the percentage of bytes received by each distinct server port over the total number of bytes received by all the server ports.

**Questions:**

1. How many reducers were launched? Which degree of parallelism did you choose? Why?

## Exercise 9
**Problem statement:** Find the percentage of flows directed to port 80 over the total number of flows.

**Questions:**

1. Using the result of this exercise and the previous one, what can you say about the distribution of server ports?
2. Refer to exercise 9:
   1. Using the MR web interface (or, alternatively, the log files generated by Hadoop), find the number of keys processed by each reducer. Do you expect to have a sensible difference in the number of processed distinct keys?
   2. Is the reducers load unbalanced?
   3. How would you avoid an eventual skew?


