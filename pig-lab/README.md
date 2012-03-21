# Hadoop Pig Laboratory

This laboratory is dedicated to Hadoop Pig and consists of a series of exercises that somewhat mimic those in the MapReduce laboratory. Besides getting familiar with Pig Latin and writing scripts to analyze data, the goal of this laboratory is to relate the design of Pig scripts to their underlying implementation in MapReduce.


## Additional documentation for the laboratory:
The underlying assumption is that students taking part to this laboratory are familiar with MapReduce ([Link][mr-tutorial]) and Pig/Pig Latin ([Link][pig-tutorial]).
Additional documentation that is useful for the exercises is available here: [Link][pig-doc]. Note that we will use Hadoop Pig 0.9.2.


Finally, note that some exercises will be devoted to embedding Pig Latin scripts in Python. Basic knowledge of python is required to such exercises.

[mr-tutorial]: http://www.eurecom.fr/~michiard/teaching/clouds/tutorial-mapreduce.pdf "MapReduce Tutorial"
[pig-tutorial]: http://www.eurecom.fr/~michiard/teaching/clouds/tutorial-high_level.pdf "Pig Tutorial"
[pig-doc]: http://pig.apache.org/docs/r0.9.2/ "Pig Documentation"

## Software setup:
This laboratory relies on a private Hadoop deployment: HDFS and MapReduce are available on 40 machines of the Lab rooms 1 and 2. As for the MapReduce laboratory, the system setup is not intended to be "production level": in particular, students will use machines that are part of the cluster to interact with it. Note also that the overall available storage is roughly 3 TB, which is going to be shared among all students, both for input files (that have been uploaded to HDFS) and for output files.


Students are required to install Pig in their local homes (follow the link below to download Hadoop Pig 0.9.2). In case you use vim to edit your scripts, it is suggested to install the plugin below for syntax highlighting. Students can also use Eclipse, and try (at their own risk) to install the PigPen plugin, which is available from the link below.

+ Hadoop Pig download page (pig-0.9.2): [Link][pig-download]
+ vim plugin for Pig Latin syntax: [Link][vim-pig]
+ PigPen: [Link][pig-pen]

[pig-download]: http://apache.multidist.com/pig/pig-0.9.2/pig-0.9.2.tar.gz "Pig download"
[vim-pig]: http://www.vim.org/scripts/script.php?script_id=2186 "vim Pig plugin"
[pig-pen]: https://issues.apache.org/jira/browse/PIG-366 "Pig Eclipse plugin"

# Exercises
The general rule to work on the exercises is the following:

+ First work locally: ``` pig -x local```: you can use both the interactive shell or directly work on pig scripts, to operate on data residing in the local filesystem
+ Then submit job to the cluster: ``` pig -x mapreduce```: in this case you need to make sure pig has been appropriately configured. In case of doubts, contact the teaching assistants. **NOTE** : remember that a script that works locally may require some minor modifications when submitted to the Hadoop cluster. For example, you may want to explicitly set the degree of parallelism for the "reduce" phase, using the ```PARALLEL``` clause.

## Exercise 1:: Word Count

Count the occurrences of each word in a text file. This is exactly the same exercise included in the MapReduce laboratory. In this exercise we will let Pig (query plan, physical plan and execution plan optimizers) do the work of producing appropriate combiners to reduce I/O utilization during the shuffle phase.

### Writing your first Pig Latin script: **LOCAL EXECUTION**
It is important to launch pig in local execution mode: ```pig -x local```. The following lines of code can also be submitted to the interactive pig shell (grunt). Use your favorite editor/IDE and open the file ```pig-lab/local-piglab/WORD_COUNT/word_count.pig```. For convenience, the code for this exercise is reported below:

```
-- Load input data from local input directory
A = LOAD './local-input/WORD_COUNT/sample.txt';

-- Parse and clean input data
B = FOREACH A GENERATE FLATTEN(TOKENIZE((chararray)$0)) AS word;
C = FILTER B BY word MATCHES '\\w+';

-- Explicit the GROUP-BY / SHUFFLE Phase
D = GROUP C BY word;

-- Generate output data in the form: <word, counts>
E = FOREACH D GENERATE group, COUNT(C);

-- Store output data in local output directory
store E into './local-output/WORD_COUNT/';
```

As you can notice, this exercise is solved (to be precise, this is a possible implementation). Students should get familiar with Pig's troubleshooting instruments, which are:

+ ```DESCRIBE relation```: this is very useful to understand the schema applied to each relation. Note that understanding schema propagation in Pig requires some time.
+ ```DUMP relation```: this command is similar to the ```STORE``` command, except that it outputs on stdout the selected relation.
+ ```ILLUSTRATE relation```: this command is useful to get a sample of the data in a relation.

For example, the student may want to understand why a parsing/cleanup phase is necessary just after data loading, and how this is achieved. Besides looking up for the relevant documentation to the ```FLATTEN``` and the ```TOKENIZE``` operators, it is informative to ```DESCRIBE B``` and ```DUMP B``` as well as ```DESCRIBE C``` and ```DUMP C```. The student should focus here on what is produced when first loading data with the default operator. Students are invited to experiment with the command ```LOAD '...' USING PigStorage()```: see [Link][pig-load]

[pig-load]: http://pig.apache.org/docs/r0.9.2/basic.html#load "Pig Load"

### Executing the word_count Pig Latin script: **CLUSTER EXECUTION**
Now that you are ready to submit you first pig script to the cluster, you need to specify the execution mode: ```pig -x mapreduce```. When you interact with HDFS (e.g., when you create an output file) you will see a directory corresponding to your unix credentials (login) under the ```/user/``` directory.


Note that the pig script you wrote for local execution requires some modifications to be run on the cluster:

+ Change input path: ```/pig-lab/input/WORD_COUNT/sample.txt/```
+ Change output path: ```/user/ - your unix login - /output/WORD_COUNT```
+ Set parallelism where appropriate: this is left for the student.

You can also use another troubleshooting instrument to understand the logical, physical and execution plans produced by Pig:

+ ```EXPLAIN relation```: note that you can generate output files in the "dot" format for better rendering

### How-to inspect your results and check your job in the cluster

+ Local execution: there is no mystery here, you can inspect output files in your local output directory

+ Cluster execution: you can both use ```hadoop dfs``` from the command line and use the Web UI, that is available (only internally) here [Link][nn]

+ Inspecting your job status on the cluster: you can identify your job by name (try to use a original/unique name for the pig script you submit, and also check for your unix login) and check its status using the Web UI of the JobTracker (only internally in the lab) available here: [Link][jt]

[nn]: http://lhoste.eurecom.fr:51070 "HDFS"
[jt]: http://lhoste.eurecom.fr:51030 "Job Tracker"

## Exercise 2:: Working with Online Social Networks data
In this exercise we will work on a Twitter dataset that was obtained from this project: [Link][tw-data]. For convenience, an example of the twitter dataset is available in the ```local-input directory```. A larger dataset is available in the private HDFS deployment in the laboratory, under the directory ```/pig-lab/input/OSN/twitter_small.txt```, which is roughly 125 MB of size.

The format of the dataset (both local and cluster) is the following:

```USER \t FOLLOWER \n ```

+ USER and FOLLOWER are represented by numeric ID (integer). 
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
Open the pig script ```./pig-lab/local-piglab/OSN/tw-count.pig``` in your favorite editor. Your goal is to fill-in the TODOs and produce in output the number of followers for each user ID: in the example above, we would like to have that user 12 has 3 followers and user 16 has 1 follower.
An optional exercise is to count, for each user ID, the number of followed users. Another optional exercise is to find outliers, that is to find users for which the follower count exceeds an arbitrary threshold.

First, work **locally**; when your script is working as you expect, you can move to the cluster execution, by specifying the appropriate input and output directories.

[tw-data]: http://an.kaist.ac.kr/traces/WWW2010.html "Twitter datasets" 


### Find the number of two-hop paths in the Twitter network
This exercise is related to the JOIN example discussed during the class on relational algebra and Hadoop Pig. You goal is to find all two-hop social relations in the Twitter dataset: for example, if user 12 is followed by user 13 and user 13 is followed by user 19, then there is a two-hop relation between user 12 and user 19. Open the pig script ```./pig-lab/local-piglab/OSN/tw-join.pig``` and fill-in the TODOs to produce the desired output.

First, work **locally*; when your script is working as you expect, you can move to the cluster execution, by specifying the appropriate input and output directories.

### How-to inspect your results and check your job in the cluster

+ Local execution: there is no mystery here, you can inspect output files in your local output directory

+ Cluster execution: you can both use ```hadoop dfs``` from the command line and use the Web UI, that is available (only internally in the lab) here [Link][nn]

+ Inspecting your job status on the cluster: you can identify your job by name (try to use a original/unique name for the pig script you submit, and also check for your unix login) and check its status using the Web UI of the JobTracker (only internally in the lab) available here: [Link][jt]

[nn]: http://lhoste.eurecom.fr:51070 "HDFS"
[jt]: http://lhoste.eurecom.fr:51030 "Job Tracker"

## Exercise 3:: Working with Network Data

## Exercise 4:: Implementing PageRank in Pig



