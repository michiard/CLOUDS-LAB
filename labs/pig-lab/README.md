# Hadoop Pig Laboratory

This lab is dedicated to Hadoop Pig and consists of a series of exercises: some of them mimic those in the MapReduce lab, others are inspired by "real-world" problems. There are two main goals for this laboratory:
* The first is to gain familiarity with the Pig Latin language to analyze data in many different ways. In other words, to focus on "what to do" with your data: to perform some simple statistics, to mine useful information, or to implement some simple algorithms.
* The second is to understand the details of Hadoop Pig internals by inspecting the process of turning a Pig Latin script into a runnable, optimized, underlying implementation in MapReduce. This means that you should examine what the Pig compiler generates from a Pig Latin script, and reason about Hadoop Job performance by analyzing Hadoop logs and statistics.
 
## Additional useful resources for Pig

* [Pig Eye for the SQL Guy][mortar-1]
* [Pig vs. MapReduce: When, Why, and How][mortar-2]
 
[mortar-1]: http://blog.mortardata.com/post/79987678239/pig-eye-for-the-sql-guy-redux 
[mortar-2]: http://blog.mortardata.com/post/60274287605/pig-vs-mapreduce

### Useful tools for "debugging":
* **DESCRIBE** relation: this is very useful to understand the schema applied to each relation. Note that understanding schema propagation in Pig requires some time. 
* **DUMP** relation: this command is similar to the STORE command, except that it outputs on ```stdout``` the selected relation.
* **ILLUSTRATE** relation: this command is useful to get a sample of the data in a relation.
* **EXPLAIN** generates (text and .dot) files that illustrate the DAG (directed acyclic graph) of the MapReduce jobs produced by Pig, and can be visualized by some graph-chart tools, such as GraphViz. This is very useful to grab an idea of what is going on under the hood.

### Additional documentation for the laboratory
The underlying assumption is that students taking part to this laboratory are familiar with MapReduce and Pig/Pig Latin. Additional documentation that is useful for the exercises is available here: http://pig.apache.org/docs/r0.11.0/. Note that we will use Hadoop Pig 0.11.0, included in the Cloudera distribution of Hadoop, CDH 4.4.0.

## Exercises and Rules
The general rule when using a real cluster is the following:
* First work locally: pig -x local: you can use both the interactive shell or directly work on pig scripts, to operate on data residing in the local file-system. **For EURECOM students**: note that you need to log to the "gateway" machine to run pig, even in what we call the "local execution mode". This means that you need to copy sample datasets in your group/home directory on the gateway machine to run your scripts "locally". Also, note that for an interactive use of the pig shell, you need to connect to the gateway machine.
* Then, submit job to the cluster: pig -x mapreduce. **NOTE**: remember that a script that works locally may require some minor modifications when submitted to the Hadoop cluster. For example, you may want to explicitly set the degree of parallelism for the "reduce" phase, using the PARALLEL clause.


## Exercise 1:: Word Count

Problem statement: Count the occurrences of each word in a text file.

The problem is exactly the same as the one in the MapReduce laboratory. In this exercise we will write a Pig Latin script to handle the problem, and let Pig do its work.

### Writing your first Pig Latin script
It is important to run this Pig Latin script in local execution mode: ```pig -x local```. The following lines of code can also be submitted to the interactive pig shell (grunt) with some minor modification. Use your favorite editor/IDE and copy the code for this exercise, which is reported below:

```
-- Load input data from local input directory
A = LOAD './sample-input/WORD_COUNT/sample.txt';

-- Parse and clean input data
B = FOREACH A GENERATE FLATTEN(TOKENIZE((chararray)$0)) AS word;
C = FILTER B BY word MATCHES '\\w+';

-- Explicit the GROUP-BY
D = GROUP C BY word;

-- Generate output data in the form: <word, counts>
E = FOREACH D GENERATE group, COUNT(C);

-- Store output data in local output directory
STORE E INTO './local-output/WORD_COUNT/';
```

As you can notice, this exercise is solved (to be precise, this is one possible solution). You have the freedom to develop your own solution. In this exercise, what we are going to do is have you get familiar with Pig and Pig Latin through inspecting the above script. Using all the information we have provided so far, you will have to play with Pig and answer several questions at the end of this exercise.

### How-to inspect your results and check your job in the cluster

+ Local execution: there is no mystery here, you can inspect output files in your local output directory

+ Cluster execution: you can use ```hdfs dfs``` from the command line

+ Inspecting your job status on the cluster: you can identify your job by name (try to use a original/unique name for the pig script you submit, and also check for your unix login) and check its status using the Web UI of the JobTracker so far. Make sure to obtain useful information, for example, what optimization Pig brings in to help reducing the I/O throughput.


### Questions:

+ Q1: Compare between Pig and Hadoop, including their pros and cons
+ Q2: What does a ```GROUP BY``` command do? In which phase of MapReduce is ```GROUP BY``` performed in this exercise and in general?
+ Q3: What does a ```FOREACH``` command do? In which phase of MapReduce is ```FOREACH``` performed in this exercise and in general?
+ Q4: Explain very briefly how Pig works (i.e. the process of Pig turning a Pig Latin script into runnable MapReduce job(s))



## Exercise 2:: Working with Online Social Networks data
In this exercise we will work on a Twitter dataset that was obtained from this project: [Link][tw-data]. For convenience, an example of the twitter dataset is available in the ```sample-input directory```. A larger dataset is available in the private HDFS deployment in the laboratory: look up in the directory ```\laboratory\input\twitter_big.txt```.

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

Open the pig script ```./pig-lab/sample-solutions/OSN/tw-count.pig``` in your favorite editor. Your goal is to fill-in the TODOs and produce the desired output: in the example above, we would like to have that user 12 has 3 followers and user 16 has 1 follower.
The output format should be like:

```
USER_ID \t No. of FOLLOWERs \n
```

Example:

```
12  3
16  1
```

[tw-data]: http://an.kaist.ac.kr/traces/WWW2010.html "Twitter datasets" 

**IMPORTANT NOTE**: This applies for EURECOM students. Despite the twitter graph stored in the laboratory HDFS deployment is not huge, it is strongly advised to be 'gentle' and try to avoid running your Pig Latin program using that large file. The main problem is disk space: we cannot guarantee that all the output generated by your script will fit the space we granted to HDFS.


#### Sub-exercises:

+ E2.1: **Follower distribution**: For each user ID, count the number of users whom he followed
+ E2.2: **Outliers Detection**: find outliers (users that have a number of followers above an arbitrary threshold -- which you have to manually set)

#### Questions:
+ Q1: Is the output sorted? Why?
+ Q2: Can we impose an output order, ascending or descending? How?
+ Q3: Related to job performance, what kinds of optimization does Pig provide in this exercise? Are they useful? Can we disable them? Should we?
+ Q4: What should we do when the input has some noise? for example: some lines in the dataset only contain USER_ID but the FOLLOWER_ID is unavailable or null


### Find the number of two-hop paths in the Twitter network
This exercise is related to the JOIN example discussed during the class on relational algebra and Hadoop Pig. You goal is to find all two-hop social relations in the Twitter dataset: for example, if user 12 is followed by user 13 and user 13 is followed by user 19, then there is a two-hop relation between user 12 and user 19. Open the pig script ```./pig-lab/sample-solutions/OSN/tw-join.pig``` and fill-in the TODOs to produce the desired output.

The output format should be like:
```
USER_1_ID \t USER_2_ID
```

**Warning**: Remember to set your parallel to at least 20, or you will have to wait for a very long time... If you forgot to do this, or in any case you have been waiting for more than half an hour, please kill your job and go back to your script to check it through.

Questions:
+ What is the size of the input data? In your opinion, is it considered Big Data? Why? How long does it take for the job to complete in your case? What are the cause of a poor performance of a job?
+ Try to set the parallelism with different number of reducers.What can you say about the load balancing between reducers? 
+ Explain briefly how Pig JOIN works in your opinion.
+ Have you verified your results? Does your result contain duplicate tuples? Do you have loops (tuples that points from one user to the same user)? What operations do you use to remove duplicates and
+ How many MapReduce jobs does your Pig script generate? Explain why



## Use-case:: Working with Network Traffic Data
Please, follow this link [TSTAT Trace Analysis with Pig][tstat] for this exercise.

**IMPORTANT**: For EURECOM students, although you can work on all exercises, for evaluation purposes you only need to complete exercises 1-5.

[tstat]: tstat-analysis/README.md "TSTAT"


## Use-case: Working with an Airline dataset
Please, go to [AIRLINE Traffic Analysis with Pig][airlines] for this exercise.

**IMPORTANT**: For EURECOM students, although you can work on all exercises, for evaluation purposes you only need to complete exercises queries 1-5.


[airlines]: airtraffic-analysis/README.md "AIRLINES"


## Optional Exercises:: Iterative Algorithms with Pig

The goal of this exercise is to understand how to embed Pig Latin in Python. This exercise was conceived as a coding example by Julien Le Dem (Data Systems Engineer, Twitter) to illustrate Pig embedding. In short, Pig natively lacks support of control flow statements: if/else, while loop, for loop, etc. Starting with Pig 0.9 it is now possible to write a python (other languages are available as well) program and embed Pig scripts, leveraging all language features provided by Python, including control flow. This is especially important as it simplifies the implementation of **iterative algorithms**.

The original source for this exercise, plus a related post on how to implement *k-*means in Pig are available here:

+ PageRank: http://techblug.wordpress.com/2011/07/29/pagerank-implementation-in-pig/
+ *k*-means: http://hortonworks.com/blog/new-apache-pig-features-part-2-embedding/

The goal of this exercise is to study the PageRank algorithm, and compare implementation and execution details of two approaches: a native MapReduce implementation and the Pig implementation. 


### Pig implementation
Official documentation explaining all the details behind embedding is available here: [Link][pig-embedding]. Students are invited to open (in their favorite IDE) the first version of the Python/Pig PageRank implementation, namely ```pg_v1.py```.

With reference to the official documentation, this implementation first ```compile()``` the pig script, then pass parameters using ```bind(params)``` and ```runSingle()``` for each iteration of the PageRank algorithm. The output of each iteration becomes the input of the previous one. Students are invited to first work **locally**, then submit the job to the cluster:

+ Local execution: use the ```./sample-input/PAGE_RANK/pg_simple.txt``` input file.

+ Cluster execution: use the ```/pig-lab/input/PAGE_RANK/web_graph.txt``` input file located in HDFS. Please note that this file is about 6.6 GB.

### Optional exercises
The following is a list of optional exercises:

+ Study a slightly improved version of PageRank, by inspecting ```pg_v2.py```

+ Modify the MapReduce implementation of PageRank described above such that it can accept as input the same format used for the Python/Pig implementation

+ Proceed with an alternative implementation of PageRank in MapReduce, following Chapter 5 of the book **Mining of Massive Datasets**, by *Anand Rajaraman and Jeff Ullman*, Cambridge University Press.

+ Implement the *k*-means algorithm whether in MapReduce or in Python/Pig (use http://hortonworks.com/blog/new-apache-pig-features-part-2-embedding/). **NOTE**: Thanks to Jun Chen (https://github.com/titaniumrain) for contributing this exercise, which is included in the ```sample-solutions``` folder.


[pig-embedding]: http://pig.apache.org/docs/r0.9.2/cont.html#embed-python "Pig Embedding"


