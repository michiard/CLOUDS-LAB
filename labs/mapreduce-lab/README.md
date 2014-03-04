# MapReduce Laboratory

The focus of this lab is on [Hadoop][hadoop] and the client API. This is done through a series of exercises:

+ The classic Word Count and variations on the theme
+ Design Pattern: Pair and Stripes
+ Design Pattern: Order Inversion
+ Join implementations

Note that the two design patterns outlined above have been originally discussed in:

+ Jimmy Lin, Chris Dyer, **Data-Intensive Text Processing with MapReduce**, Morgan Claypool ed. [link][jimmilin]

[hadoop]: http://hadoop.apache.org "hadoop"
[jimmilin]: http://lintool.github.io/MapReduceAlgorithms/index.html

## Setting up "connectivity" to the cluster:

This step is only necessary if you're a student at EURECOM: the goal is to gain access to the EURECOM private cloud, which hosts a virtual Hadoop cluster that we use for the laboratory.

Follow these steps:

- Obtain from the Teaching Assistants a group identifier: e.g. group07
- Log to a Linux machine in one of the Laboratory rooms with your EURECOM unix credentials
- Generate a RSA private / public key pair:
 - ```ssh-keygen ~/.ssh/group07```
 - send your public key to the Laboratory administrator
 - wait for an acknowledgment from the Laboratory administrator
- Log to the client Virtual Machine (VM): this VM is used to submit your jobs to the Hadoop cluster
 - ```ssh -i ~/.ssh/group07 group07@192.168.45.13```

If you want to copy a file (e.g. the jar of your job, as you will see later) to this client VM, just do:

 - ```scp -i ~/.ssh/group07 <your-local-file> group07@192.168.45.13:~/<your-remote-file>```


## Setting up the laboratory sources in Eclipse:

The first step is to create a new Java Project in Eclipse:

- In Eclipse, select the menu item **File > New > Project ...** to open the **New Project** wizard
- Select **Java Project** then click **Next** to start the **New Java Project**
- Type a name for your new project, such as ''mr-lab''
- Ensure to use *JavaSE-1.6* as JRE, then click on **Next**
- Go on **Libraries** tab, and click on **Add External Jars**
- Add the following jars:
 - ```hadoop/hadoop-common.jar```
 - ```hadoop-0.20-mapreduce/hadoop-core.jar```
 - ```hadoop-hdfs/hadoop-hdfs.jar```
- Click on **Finish**

Now you have a new project.

**Note**: the above jars are provided locally, if you are at EURECOM. Otherwise, you'll have to look for them in your Hadoop installation, or download them yourself.

The next step is to import the source files to complete in the Project.
- In Eclipse, select the **src** directory in your project.
- Right-click on the src directory and select **Import**
- Select **File System** then click **Next**
- In **From Directory**,  select *./mr/src/fr*, which is inside the directory where you cloned the git repository
- In the tree on the left, select *fr*, then click **Finish**.

At this point you should have a java project named ''mr-lab'', properly configured.
**Note**: there can be errors in the source code you imported. This is normal: exercises have a series of ```TODO``` that you need to complete before the project can compile.

## How to ''build'' a job
You need first to export a **JAR** file. Therefore, from Eclipse:
- Select the menu item **File > Export**
- Type **JAR**, select **JAR file** and click on **Next**
- In the textbox **Select the export destination**, type the name of the new **JAR** you want to export, i.e. *'/home/student/mrlab.jar'*
- Type **Finish**

Once you have exported your jar file, you can submit your job to the Hadoop cluster. At EURECOM, we have a cluster ready for you. Otherwise, you should use your own cluster, pseudo-cluster, or local execution mode.
Open a *terminal*, and type:
```
hadoop jar <jarname.jar> <fully.qualified.class.Name> <Parameters>
```

For example, for running the *WordCount* exercise, type:
```
hadoop jar mrlab.jar fr.eurecom.dsg.mapreduce.WordCount 2 INPUT/text/quote.txt OUTPUT/wordcount/
```

Note that you need to specify a *non existing* output directory, or to delete it before running the job.

## Web interfaces: monitor job progress

Hadoop publishes some web interfaces that display JobTracker and HDFS statuses.
Depending on your cluster configuration, you will need to type a url in your browser tat point to:

- Jobtracker Web Interface (generally on port 50030)
- NameNode Web Interface (generally on port 50070)

# Exercises

## EXERCISE 1:: Word Count

Count the occurrences of each word in a text file. This is the simplest example of MapReduce job: in the following we illustrate three possible implementations.

+ **Basic**: the map method emits 1 for each word. The reducer aggregates the ones it receives from mappers for each key it is responsible for, and saves on disk (HDFS) the result
+ **In-Memory Combiner**: instead of emitting 1 for each encountered word, the map method (partially) aggregates the ones and emits the result for each word
+ **Combiner**: the same as In-Memory Combiner but using the MapReduce Combiner class


### Instructions

For the **basic** version, you have to modify the file *WordCount.java* in the package *fr.eurecom.dsg.mapreduce*. You have to work  on each ```TODO```, filling the gaps with code, following the description associated to each ```TODO```. The package *java.util* contains a class *StringTokenizer* that can be used to tokenize the text.

For the **In-Memory** Combiner and the **Combiner**, you have to modify *WordCountIMC.java* and *WordCountCombiner.java* in the same package referenced above. You have to complete each ```TODO``` using the same code of the basic word count example, except for the ```TODO``` marked with a star *. Those must be completed using the appropriate design pattern.

When an execise is completed you can export it into a Job jar file, that you can execute on the cluster.

### Example of usage
You Job should accept three arguments: the number of reducers, the input file and the output path. Example of executions are:

```
hadoop jar <compiled_jar> fr.eurecom.dsg.mapreduce.WordCount 3 <input_file> <output_path>
hadoop jar <compiled_jar> fr.eurecom.dsg.mapreduce.WordCountIMC 3 <input_file> <output_path>
hadoop jar <compiled_jar> fr.eurecom.dsg.mapreduce.WordCountCombiner 3 <input_file> <output_path>
```

To test your code use the file `./inputs/text/quote.txt`. **NOTE**: if you are at EURECOM, this file is available in the HDFS of the lab. Otherwise, you will have to ''load'' it yourself in your own HDFS installation.

To run the final version of your job, you can use a bigger file, `./input/gutenberg-partial.txt`, which contains an extract of the English books from Project Gutenberg http://www.gutenberg.org/, which provides a collection of full texts of public domain books.

### Questions ###

Answer the following questions:

+ How does the number of reducers affect performance? How many reducers can be executed in parallel?
+ Use the JobTracker web interface to examine Job counters: can you explain the differences among the three variants of this exercise? For example, look at the amount of bytes shuffled by Hadoop

> Zipf's law states that given some corpus of natural language utterances, the frequency of any word is inversely proportional to its rank in the frequency table. Thus the most frequent word will occur approximately twice as often as the second most frequent word, three times as often as the third most frequent word, etc. For example, in the *Brown Corpus of American English* text, the word "*the*" is the most frequently occurring word, and by itself accounts for nearly 7% of all word occurrences. The second-place word "*of*" accounts for slightly over 3.5% of words, followed by "*and*". Only 135 vocabulary items are needed to account for half the Brown Corpus. (wikipedia.org)

+ Can you explain how does the distribution of words affect your Job?

## EXERCISE 2:: Word Co-occurrence
In the following exercise, we need to build the term co-occurrence matrix for a text collection.
A co-occurrence matrix is a ''n'' x ''n'' matrix, where ''n'' is the number of unique words in the text. For each pair of words, we count the number of times they co-occurred in the text in the **same line**. Note: you can define your own ''neighborhood'' function, and extend the context to more or less than a single line.

### The ''Pairs'' Design Pattern

The basic (and maybe most intuitive) implementation of this exercise is the *Pair* design pattern.
The basic idea is to emit, for each couple of words in the same line, the couple itself (or *pair*) and the value 1.
For example, in the line `w1 w2 w3 w1`, we emit `(w1,w2):1, (w1, w3):1, (w2:w1):1, (w2,w3):1, (w2:w1):1, (w3,w1):1, (w3:w2):1, (w3,w1):1`. Essentially, the reducers need to collect enough information from mapper to ''cover'' each individual ''cell'' of the co-occurrence matrix.

In this exercise, we need to use a composite key to emit an occurrence of a pair of words. You will learn how to create a custom Hadoop data type to be used as key type.

A Pair is a tuple composed by two elements that can be used to ship two objects within a parent object. For this exercise the student has to implement a TextPair, that is a Pair that contains two words.

#### Instructions
There are two files for this exercise:

+ *TextPair.java*: data structure to be implemented by the student. Besides the implementation of the data structure itself, you have to implement the serialization Hadoop API (write and read Fields).
+ *Pair.java*: the implementation of a pair example using *TextPair.java* as datatype.

#### Example of usage
The final version should get in input three arguments: the number of reducers, the input file and the output path. An example of execution is:
```
hadoop jar <compiled_jar> fr.eurecom.dsg.mapreduce.Pair 1 <input_file> <output_path>
```

To test your code use the file `./input/quote.txt`, or the one provided in the HDFS cluster at eurecom.

To run the final version of your job, you can use a larger file, `./input/gutenberg-partial.txt`.


#### Questions
Answer the following questions (in a simple text file):

+ How does the number of reducer influence the behavior of the Pairs approach?
+ Why does `TextPair` need to be Comparable?
+ Can you use the implemented reducers as *Combiner*?


### The ''Stripes'' Design Pattern
This approach is similar to the previous one: for each line, co-occurring pairs are generated. However, now, instead of emitting every pair as soon as it is generated, intermediate results are stored in an associative array. We use an associative array, and, for each word, we emit the word itself as key and a *Stripe*, that is the map of co-occurring words with the number of associated occurrence. Essentially, mappers generate ''partial'' rows of the co-occurrence matrix. Then, reducers, will assembly partial rows to generate the aggregate and final row of the matrix.

For example, in the line `w1 w2 w3 w1`, we emit:
```
w1:{w2:1, w3:1}, w2:{w1:2,w3:1}, w3:{w1:2, w2:1}, w1:{w2:1, w3:1}
```

Note that, instead, we could emit also:
```
w1:{w2:2, w3:2}, w2:{w1:2,w3:1}, w3:{w1:2, w2:1}
```

In this exercise the student will understand how to create a custom Hadoop data type to be used as value type.

#### Instructions
There are two files for this exercise:

+ *StringToIntMapWritable.java*: the data structure file, to be implemented
+ *Stripes.java*: the MapReduce job, that the student must implement using the StringToIntMapWritable data structure

#### Example of usage
```
hadoop jar <compiled_jar> fr.eurecom.dsg.mapreduce.Stripes 2 <input_file> <output_path>
```
To test your code use the file `./input/quote.txt`, or the one provided in the HDFS cluster at eurecom.

To run the final version of your job, you can use a larger file, `./input/gutenberg-partial.txt`.

#### Questions
Answer the following questions (in a simple text file):

+ Can you use the implemented reducers as *Combiner*?
+ Do you think Stripes could be used with the in-memory combiner pattern?
+ How does the number of reducer influence the behavior of the Stripes approach?
+ Using the Jobtracker Web Interface, compare the shuffle phase of *Pair* and *Stripes* design patterns.
+ Why `StringToIntMapWritable` is not Comparable (differently from `TextPair`)?


## EXERCISE 3:: Relative term co-occurrence and the ''Order Inversion'' Design Pattern
In this example we need to compute the co-occurrence matrix, like the one in the previous exercise, but using the relative frequencies of each pair, instead of the absolute value. Pratically, we need to count the number of times each pair *(w<sub>i</sub>, w<sub>j</sub>)* occurs divided by the number of total pairs with *w<sub>i</sub>* (the marginal).

The student has to implement the `Map` and `Reduce` methods and the special partitioner (see `OrderInversion#PartitionerTextPair` class), which applies the partitioner only according to the first element in the Pair, sending all data regarding the same word to the same reducer. Note that inside the `OrderInversion` class there is a field called `ASTERISK` which should be used to output the total number of occourrences of a word. Refer to the laboratory slides for more information.

### Instructions
There is one file for this exercise called `OrderInversion.java`. The `run` method of the job is already implemented, the student should complete the mapper, the reducer and the partitioner, as explained in the ```TODO```.

### Example of usage
```
hadoop jar <compiled_jar> fr.eurecom.fr.mapreduce.OrderInversion 4 <input_file> <output_path>
```

To test your code use the file `./input/quote.txt`, or the one provided in the HDFS cluster at eurecom.

To run the final version of your job, you can use a larger file, `./input/gutenberg-partial.txt`.

### Questions ###
Answer the following questions. In answering the questions below, consider the role of the combiner.

+ Do you think the Order Inversion approach is 'faster' than a naive approach with multiple jobs? For example, consider implementing a compound job in which you compute the numerator and the denominator separately, and then perform the computation of the relative frequency
+ What is the impact of the use of a 'special' compound key on the amounts of shuffled bytes?
+ How does the default partitioner works with `TextPair`? Can you imagine a different implementation that does not change the Partitioner?
+ For each key, the reducer receives its marginal before the co-occurence with the other words. Why?

## EXERCISE 4:: Joins
In MapReduce, the term ''join'' refers to merging two different dataset stored as unstructured files in HDFS. As for databases, in MapReduce there are many different kind of joins, each with its use-cases and constraints. In this laboratory the student will implement two different kinds of MapReduce join techniques:

+ **Distributed Cache Join**: this join technique is used when one of the two files to join is small enough to fit (eventually in memory) on each computer of the cluster. This file is copied locally to each computer using the Hadoop distributed cache and then loaded by the map phase.
+ **Reduce-Side Join**: in this case the map phase tags each record such that records of different inputs that have to be joined will have the same tag. Each reducer will receive a tag with a list of records and perform the join.

### Jobs

+ **Distributed Cache Join**: implement a variant of the Word Count exercise using the distributed cache to exclude some words. The file `./input/english.stop` contains the list of the words to exclude.
+ **Reduce Side Join**: You need to find the two-hops friends, i.e. the friends of friends of each user, in a small Twitter dataset. In particular, you need to implement a self-join, that is a join between two instances of the same dataset. To test your code, use the file `./input/twitter-small.txt`. The file fortmat is:

```
userID followerID
1 		 2
1 		 6
2 		 3
2 		 7

...

```

### Instructions

+ **Distributed Cache Join**: you can start from the file *DistributedCacheJoin.java*. See also [<a href="https://hadoop.apache.org/common/docs/r0.20.2/api/org/apache/hadoop/filecache/DistributedCache.html">DistributedCache API</a>] as reference.
+ **Reduce Side Join**: use the file *ReduceSideJoin.java* as starting point. This exercise is different from the others because it does not contain any information on how to do it. The student is free to choose how to implement it.

### Example of usage
```
hadoop jar <compiled_jar> fr.eurecom.fr.mapreduce.DistributedCacheJoin 1 <big_file> <small_file> <output_path>
hadoop jar <compiled_jar> fr.eurecom.fr.mapreduce.ReduceSideJoin 1 <input_file2> <input_file1> <output_path>
```

