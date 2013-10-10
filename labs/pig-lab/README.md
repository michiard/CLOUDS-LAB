# Hadoop Pig Laboratory

This laboratory is dedicated to Hadoop Pig and consists of a series of exercises: some of them somewhat mimic those in the MapReduce laboratory, others are inspired by "real-world" problems. There are two main goals for this laboratory:
* The first is to gain familiarity with the Pig Latin language to analyze data in many different ways. In other words, to focus on "what to do" with your big data: to perform some statistics, to mine out useful information, or to implement some simple algorithms, etc... This is a typical work for a "data scientist".
* The second is to understand the details of Hadoop Pig internals by inspecting the process of turning a Pig Latin script into a runnable, optimized underlying implementation in MapReduce. This means that students should examine what the Pig compiler generates from a Pig Latin script (using explain command), and reason about Hadoop Job performance by analyzing Hadoop logs and statistics.


The EXPLAIN command can generate .dot files that illustrate the DAG (directed acyclic graph) of the MapReduce jobs produced by Pig, and can be visualized by some graph-chart tools, such as GraphViz. This is very useful to grab an idea of what is going on under the hood. For those who are geek enough and want to play with some cool project, it is suggested to have a look at Twitter Ambrose (https://github.com/twitter/ambrose).
### Additional documentation for the laboratory
The underlying assumption is that students taking part to this laboratory are familiar with MapReduce and Pig/Pig Latin. Additional documentation that is useful for the exercises is available here: http://pig.apache.org/docs/r0.11.0/. Note that we will use Hadoop Pig 0.11.0, included in the Cloudera distribution of Hadoop, CDH 4.4.0.
## Exercises and Rules
The general rule when using a real cluster is the following:
* First work locally: pig -x local: you can use both the interactive shell or directly work on pig scripts, to operate on data residing in the local filesystem
* Then submit job to the cluster: pig -x mapreduce NOTE : remember that a script that works locally may require some minor modifications when submitted to the Hadoop cluster. For example, you may want to explicitly set the degree of parallelism for the "reduce" phase, using the PARALLEL clause.
* If you're using the virtual machine prepared for the class, note that, each VM has its own pseudo-local installation of Hadoop, so you do not strictly need to use the “local” pig environment.


## Exercise 1:: Word Count

Problem statement: Count the occurrences of each word in a text file.

The problem is exactly the same as the one in the MapReduce laboratory. In this exercise we will write a Pig Latin script to handle the problem, and let Pig do its work.

### Writing your first Pig Latin script: **LOCAL EXECUTION**
It is important to run this Pig Latin script in local execution mode: ```pig -x local```. The following lines of code can also be submitted to the interactive pig shell (grunt) with some minor modification. Use your favorite editor/IDE and open the file ```pig-lab/local-piglab/WORD_COUNT/word_count.pig```. For convenience, the code for this exercise is reported below:

```
-- Load input data from local input directory
A = LOAD './local-input/WORD_COUNT/sample.txt';

-- Parse and clean input data
B = FOREACH A GENERATE FLATTEN(TOKENIZE((chararray)$0)) AS word;
C = FILTER B BY word MATCHES '\\\\w+';

-- Explicit the GROUP-BY
D = GROUP C BY word;

-- Generate output data in the form: <word, counts>
E = FOREACH D GENERATE group, COUNT(C);

-- Store output data in local output directory
STORE E INTO './local-output/WORD_COUNT/';
```

As you can notice, this exercise is solved (to be precise, this is one possible solution). You have the freedom to develop your own solutions. In this exercise, what we are going to do is have you get familiar with Pig and Pig Latin through inpsecting the above script. Using all the information we have provided so far, you will have to play with Pig and answer several questions at the end of this exercise (remember: Google may be a helpful friend, and so are we)

Some of the useful debugging commands are:

+ ```DESCRIBE relation```: this is very useful to understand the schema applied to each relation. Note that understanding schema propagation in Pig requires some time.
+ ```DUMP relation```: this command is similar to the ```STORE``` command, except that it outputs on stdout the selected relation.
+ ```ILLUSTRATE relation```: this command is useful to get a sample of the data in a relation.

For example, the student may want to understand why a parsing/cleanup phase is necessary just after data loading, and how this is achieved. Besides looking up for the relevant documentation to the ```FLATTEN``` and the ```TOKENIZE``` operators, it is informative to ```DESCRIBE B``` and ```DUMP B``` as well as ```DESCRIBE C``` and ```DUMP C```. The student should focus here on what is produced when first loading data with the default operator. Students are invited to experiment with the command ```LOAD '...' USING PigStorage()```: see [Link][pig-load]

[pig-load]: http://pig.apache.org/docs/r0.9.2/basic.html#load "Pig Load"


### Executing the word_count Pig Latin script: **CLUSTER EXECUTION**

Now that you are ready to submit you first pig script to the cluster, you need to specify the execution mode: ```pig -x mapreduce```. When you interact with HDFS (e.g., when you create an output file) you will see a directory corresponding to your unix credentials (login) under the ```/user/``` directory.


Note that the pig script you wrote for local execution requires some modifications to be run on the cluster:

+ Change input path: ```/data/mumak.log``` :: this is the same file used for the MapReduce lab, exercise 1
+ Change output path: ```/user/ - your unix login - /output/PIG_WORD_COUNT```
+ Set parallelism where appropriate: this is intentionally left for the student

You can also use another troubleshooting instrument to understand the logical, physical and execution plans produced by Pig:

+ ```EXPLAIN relation```: note that you can generate output files in the "dot" format for better rendering

### How-to inspect your results and check your job in the cluster

+ Local execution: there is no mystery here, you can inspect output files in your local output directory

+ Cluster execution: you can use ```hdfs dfs``` from the command line

+ Inspecting your job status on the cluster: you can identify your job by name (try to use a original/unique name for the pig script you submit, and also check for your unix login) and check its status using the Web UI of the JobTracker so far. Make sure to obtain iuseful information, for example, what optimization Pig brings in to help reducing the I/O throughput.


### Questions:

+ Q1: Compare between Pig and Hadoop, including their pros and cons
+ Q2: What does a ```GROUP BY``` command do? At which phase of MapReduce is ```GROUP BY``` performed in this exercise and in general?
+ Q3: What does a ```FOREACH``` command do? At which phase of MapReduce is ```FOREACH``` performed in this exercise and in general?
+ Q4: Explain very briefly how Pig works (i.e. the process of Pig turning a Pig Latin script into runnable MapReduce job(s))
+ Q5: Explain how you come to a conclusion that your results are correct

## Exercise 2:: Working with Online Social Networks data
In this exercise we will work on a Twitter dataset that was obtained from this project: [Link][tw-data]. For convenience, an example of the twitter dataset is available in the ```local-input directory```. A larger dataset is available in the private HDFS deployment in the laboratory, under the directory ```/data/TWITTER/twitter_graph.txt```

The format of the dataset (both local and cluster) is the following:

```
USER_ID \t FOLLOWER_ID \n 
```


+ USER_ID and FOLLOWER_ID are represented by numeric ID (integer). 
+ These numeric IDs are the same as numeric IDs Twitter managed.
+ Therefore, you can access a profile of user 12 via http://api.twitter.com/1/users/show.xml?user_id=12.

Example:


```
12   	13
12   	14
12   	15
16   	17
```


+ Users 13, 14 and 15 are followers of user 12.
+ User 17 is a follower of user 16.

### Counting the number of "friends" per Twitter user

Problem statement: for each user, calculate the total number of followers of that user

Open the pig script ```./pig-lab/local-piglab/OSN/tw-count.pig``` in your favorite editor. Your goal is to fill-in the TODOs and produce the desire output: in the example above, we would like to have that user 12 has 3 followers and user 16 has 1 follower.
The output format should be like:

```
USER_ID \t No. of FOLLOWERs \n
```

Example:

```
12  3
16  1
```

First, work **locally**; when your script is working as you expect, you can move to the cluster execution, by specifying the appropriate input and output directories.

[tw-data]: http://an.kaist.ac.kr/traces/WWW2010.html "Twitter datasets" 

### Sub-exercises:

+ E2.1: For each user ID, count the number of users whom he followed
+ E2.2: "Data mining stuff": find outlier users (users that has the number of followers below an arbitrary threshold (which you have to manually set))

### Questions:
+ Q1: Are the output sorted? Why?
+ Q2: Are we able to pick the order: ascending or descending? How?
+ Q3: Related to job performance, what kinds of optimization does Pig provide in this exercise? Are they useful? Can we disable them? Should we?
+ Q4: What should we do when the input has some noises? for example: some lines in the dataset only contain USER_ID but the FOLLOWER_ID is unavailable or null

**IMPORTANT NOTE**: despite the twitter graph stored in the laboratory HDFS deployment is not huge, it is strongly adviced to be 'gentle' and try to avoid running your Pig Latin program using that large file. The main problem is disk space: we cannot guarantee that all the output generated by your script will fit the space we granted to HDFS.


### Find the number of two-hop paths in the Twitter network
This exercise is related to the JOIN example discussed during the class on relational algebra and Hadoop Pig. You goal is to find all two-hop social relations in the Twitter dataset: for example, if user 12 is followed by user 13 and user 13 is followed by user 19, then there is a two-hop relation between user 12 and user 19. Open the pig script ```./pig-lab/local-piglab/OSN/tw-join.pig``` and fill-in the TODOs to produce the desired output.

The output format should be like:
```
USER_1_ID \t USER_2_ID
```

**Warning**: Remember to set your parallel to at least 20, or you will have to wait for a very long time... If you forgot to do this, or in any case you have been waiting for more than half an hour, please kill your job (by pressing Ctrl + C) and go back to your script to check it through.

First, work **locally**; when your script is working as you expect, you can move to the cluster execution, by specifying the appropriate input and output directories. This time the input is ```/data/TWITTER/twitter_graph2.txt```

Questions:
+ What is the size of the input data? In your opinion, is it considered Big Data? Why? How long does the job run? What is your comment here?
+ Try the parallelism with different numbers. Observe the behavior of reducers. What can you say about the load balancing between reducers? 
+ Explain briefly how Pig JOIN works in your opinion.
+ Have you verified your results? Does your result contain duplicate tuple? Or any tuple that points from one user to him again? What operations do you use to solve these two problems?
+ How many MapReduce jobs does your Pig script generate? Explain why

## Exercise 3:: Working with Network Data
The goal of this exercise is two-fold:

+ In the first part, we will pretend to have data generated by ```tcpdump``` (use ```./local-input/TCP_DUMP/sample.txt```) and our goal will be simply to count the traffic generated by Internet hosts traced in the dataset. Precisely, the goal of this exercise is to work with the ```REGEX_EXTRACT_ALL``` [Link][pig-regex] built-in string function of Hadoop Pig. This is very useful when you are not in charge (or you can't control) the format of your input data. **NOTE**: this exercise can be done using local execution alone.

+ The second part is more involved: you are given two datasets (two to work on your local machine, available in ```./local-input/NETWORK_TRAFFIC/sample.txt``` and ```./local-input/NETWORK_TRAFFIC/100-linee.txt```, one stored in HDFS available in ```/data/NETWORK_DATA/wide-20M.txt```) which contain pre-processed tcpdump data in a TSV format (tab separated values). The data format (or schema) is available in your local input directory (```./local-input/NETWORK_TRAFFIC/record_format.txt```). With this data at hand, you will focus on computing traffic statistics and to find outliers among the users that have been traced in the dataset.

[pig-regex]: http://pig.apache.org/docs/r0.9.2/func.html#regex-extract-all "Regular expressions"

### Working on tcpdump data
Use the pig script located in ```./local-piglab/TCP_DUMP/tcp_count.pig```, and complete the TODOs that you will find in the code. It is recommended to work in an interactive mode (in any case, this is a **local** exercise): for simplicity, you can find next the source code available in the script. Your goal is to play with regular expressions (the one provided below is not necessarily the best one to use) and to understand where operations related to data preparation take place in the underlying MapReduce implementation.

```
-- Load raw data generated by tcpdump
RAW_LOGS = LOAD './local-input/TCP_DUMP/sample.txt' AS (line:chararray);

-- Apply a schema to raw data
LOGS_BASE = FOREACH RAW_LOGS GENERATE 
	  FLATTEN( (tuple(CHARARRAY,CHARARRAY,CHARARRAY,LONG))
	  REGEX_EXTRACT_ALL(line, '(\\d+-\\d+-\\d+).+\\s(\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}\\.\\d{1,5}).+\\s(\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}\\.\\d{1,5}).+length\\s+(\\d+)')) 
	  AS (date:chararray, IPS:chararray, IPD:chararray, S:long);

--DUMP LOGS_BASE;

-- TODO: Group traffic information by source IP addresses
--FLOW = GROUP ...

-- TODO: Count the number of bytes uploaded by each IP address
--TRAFFIC = ...

-- TODO: Store output data in ./local-output/TCP_DUMP/
```

### Working on Network data
This exercise is inspired by the work described here: [Link][ladis11]. First, you should focus on "Job 1", that is described as follows: For each IP address in the dataset, compute the number of bytes uploaded, downloaded and the total traffic generated by that host or server. This has to be done with three different time granularities: per hour, per day and per week (due to the size of the dataset, we omit monthly statistics). Note that this exercise can be difficult in that your goal is to help Pig and the optimizer to produce a compact MapReduce job: note also that for this exercise you are only given the load statement, while you have to write everything else (suggestions can be given during the lab session).

```
-- This is the raw input data
RAW_DATA = LOAD './local-input/NETWORK_TRAFFIC/sample.txt'
        AS (ts:long, sport, dport, sip, dip,
                l3proto, l4proto, flags,
                phypkt, netpkt, overhead,
                phybyte, netbyte:long);
```

**IMPORTANT NOTE**: the timestamp (ts) format is UNIX time, that is the number of seconds since January 1, 1970. This means you'll have to work out a solution to extract hour, day and week information.

Optional exercises: exceptionally, advanced students can work on the implementation of all the Jobs described in [Link][ladis11]. For example, it is interesting to focus on studying outliers in the dataset: find the top 10 IP addresses that generate more traffic (uplink, downlink and total), per hour, day and week.

**POSSIBLE SOLUTIONS**: for this exercise, you will notice a folder called "solved", in which you can find two possible approaches to "Job 1". Study them, and improve them.

[ladis11]:http://www.eurecom.fr/~michiard/downloads/ladis2011.pdf "Ladis 2011"


## Exercise 4:: Implementing PageRank in Pig

The goal of this exercise is to understand how to embed Pig Latin in Python. This exercise was conceived as a coding example by Julien Le Dem (Data Systems Engineer, Twitter) to illustrate Pig embedding. In short, Pig natively lacks support of control flow statements: if/else, while loop, for loop, etc. Starting with Pig 0.9 it is now possible to write a python (other languages are available as well) program and embed Pig scripts, leveraging all language features provided by Python, including control flow. This is especially important as it simplifies the implementation of **iterative algorithms**.

The original source for this exercise, plus a related post on how to implement *k-*means in Pig are available here:

+ PageRank: http://techblug.wordpress.com/2011/07/29/pagerank-implementation-in-pig/
+ *k*-means: http://hortonworks.com/blog/new-apache-pig-features-part-2-embedding/

The goal of this exercise is to study the PageRank algorithm, and compare implementation and execution details of two approaches: a native MapReduce implementation and the Pig implementation. A brief (and simplified) introduction to PageRank is available in the last section of the laboratory notes for the MapReduce laboratory [Link][mr-lab]. 

[mr-lab]: http://www.eurecom.fr/~michiard/teaching/clouds/mr-lab.pdf "Algorithm Design"

### MapReduce implementation
Following the procedure detailed in [Link][mr-lab-git], create an Eclipse project and import the MapReduce implementation of PageRank. Source code is available here: [Link][mr-pr]. Note that this is a "naive" implementation of PageRank. Students are invited to inspect the code, including the "driver" program to handle iterations. Note also the input format expected by this implementation of PageRank. Input datasets for this exercise are available upon request.

[mr-lab-git]: https://github.com/michiard/CLOUDS-LAB/tree/master/mapreduce-lab "MapReduce Lab"
[mr-pr]: https://github.com/michiard/CLOUDS-LAB/tree/master/mapreduce-lab/solved/src/fr/eurecom/dsg/mapreduce/pagerank "PageRank"


### Pig implementation
Official documentation explaining all the details behind embedding is available here: [Link][pig-embedding]. Students are invited to open (in their favorite IDE) the first version of the Python/Pig PageRank implementation, namely ```pg_v1.py```.

With reference to the official documentation, this implementation first ```compile()``` the pig script, then pass parameters using ```bind(params)``` and ```runSingle()``` for each iteration of the PageRank algorithm. The output of each iteration becomes the input of the previous one. Students are invited to first work **locally**, then submit the job to the cluster:

+ Local execution: use the ```./local-input/PAGE_RANK/pg_simple.txt``` input file.

+ Cluster execution: use the ```/pig-lab/input/PAGE_RANK/web_graph.txt``` input file located in HDFS. Please note that this file is about 6.6 GB.

### Optional exercises
The following is a list of optional exercises:

+ Study a slightly improved version of PageRank, by inspecting ```pg_v2.py```

+ Modify the MapReduce implementation of PageRank described above such that it can accept as input the same format used for the Python/Pig implementation

+ Proceed with an alternative implementation of PageRank in MapReduce, following Chapter 5 of the book **Mining of Massive Datasets**, by *Anand Rajaraman and Jeff Ullman*, Cambridge University Press.

+ Implement the *k*-means algorithm whether in MapReduce or in Python/Pig (use http://hortonworks.com/blog/new-apache-pig-features-part-2-embedding/)


[pig-embedding]: http://pig.apache.org/docs/r0.9.2/cont.html#embed-python "Pig Embedding"


## Exercise 5: Working with an Airline dataset

+ This exercise is inspired by http://www.datadr.org/doc/airline.html
+ Full information on datasets (optional datasets), and general documentation available here: http://stat-computing.org/dataexpo/2009/

*NOTE*: This is work in progress. Once all queries will be specified, the work in progress flag in the exercise title will disappear. Currently, you can find a directory with some preliminary versions of the queries below (Q1 - Q5). Not all of them have been fully implemented. Feel free to comment, improve and test them as they are: this series of queries will be complemented with appropriate questions to understand the impact of design choices on the underlying MapReduce execution engine.

Before we start, here's a description of the dataset "schema". We will work on data that can be downloaded from here: http://stat-computing.org/dataexpo/2009/the-data.html

Note that there is a single CSV file per year, hence the first field below is somehow redundant, although you can imagine to concatenate all files and work on them as a whole (which by the way would make sense when using Hadoop MapReduce / Pig). In summary, there are 29 fields which provide enough information to build Pig scripts that cover Queries 1-5. For the advanced analysis subsection, you need other data, which can be downloaded from the links below.

```
1	 Year	1987-2008
2	 Month	1-12
3	 DayofMonth	1-31
4	 DayOfWeek	1 (Monday) - 7 (Sunday)
5	 DepTime	actual departure time (local, hhmm)
6	 CRSDepTime	scheduled departure time (local, hhmm)
7	 ArrTime	actual arrival time (local, hhmm)
8	 CRSArrTime	scheduled arrival time (local, hhmm)
9	 UniqueCarrier	unique carrier code
10	 FlightNum	flight number
11	 TailNum	plane tail number
12	 ActualElapsedTime	in minutes
13	 CRSElapsedTime	in minutes
14	 AirTime	in minutes
15	 ArrDelay	arrival delay, in minutes
16	 DepDelay	departure delay, in minutes
17	 Origin	origin IATA airport code
18	 Dest	destination IATA airport code
19	 Distance	in miles
20	 TaxiIn	taxi in time, in minutes
21	 TaxiOut	taxi out time in minutes
22	 Cancelled	was the flight cancelled?
23	 CancellationCode	reason for cancellation (A = carrier, B = weather, C = NAS, D = security)
24	 Diverted	1 = yes, 0 = no
25	 CarrierDelay	in minutes
26	 WeatherDelay	in minutes
27	 NASDelay	in minutes
28	 SecurityDelay	in minutes
29	 LateAircraftDelay	in minutes
```

Other sources of data come from here: http://stat-computing.org/dataexpo/2009/supplemental-data.html. Precisely, we are interested in:

+ Airport IATA Codes to City names and Coordinates mapping: http://stat-computing.org/dataexpo/2009/airports.csv
+ Carrier codes to Full name mapping: http://stat-computing.org/dataexpo/2009/carriers.csv
+ Information about individual planes: http://stat-computing.org/dataexpo/2009/plane-data.csv
+ Weather information: http://www.wunderground.com/weather/api/. You can subscribe for free to the developers API and obtain (at a limited rate) hystorical weather information in many different formats. Also, to get an idea of the kind of information is available, you can use this link: http://www.wunderground.com/history/

### *Query 1:* Top 20 cities by total volume of flights 

What are the busiest cities by total flight traffic. JFK will feature, but what are the others? For each airport code compute the number of inbound, outbound and all flights. Variation on the theme: compute the above by day, week, month, and over the years.

### *Query 2:* Carrier Popularity 

Some carriers come and go, others demonstrate regular growth. Compute the (log base 10) volume -- total flights -- over each year, by carrier. The carriers are ranked by their median volume (over the 10 year span).

### *Query 3:* Proportion of Flights Delayed 

A flight is delayed if the delay is greater than 15 minutes. Compute the fraction of delayed flights per different time granularities (hour, day, week, month, year).

### *Query 4:* Carrier Delays

Is there a difference in carrier delays? Compute the proportion of delayed flights by carrier, ranked by carrier, at different time granularities (hour, day, week, month year). Again, a flight is delayed if the delay is greater than 15 minutes.

### *Query 5:* Busy Routes

Which are busy the routes? A simple first approach is to create a frequency table for the unordered pair (i,j) where i and j are distinct airport codes.

### Advanced analyses

+ When is the best time of day/day of week/time of year to fly to minimise delays?
+ Do older planes suffer more delays?
+ How does the number of people flying between different locations change over time?
+ How well does weather predict plane delays?
+ Can you detect cascading failures as delays in one airport create delays in others? Are there critical links in the system?

