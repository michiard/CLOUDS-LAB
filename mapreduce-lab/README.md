# MapReduce Laboratory

In this laboratory the user will learn how to use the [<a href="http://hadoop.apache.org">Hadoop</a>] client API by implementing a set of well know MapReduce techniques: Word Count, Pair, Stripes, Order Inversion and Join.


## Word Count

Count the occurrences of each word inside a text file. This is the simplest example of MapReduce job and has to be implemented in three different ways:

+ **Basic**: the map method emit 1 for each word. The reduce aggregate
                      the ones and save on disk the result.
+ **In-Memory Combiner**: instead of emitting 1 for each word, the map method aggregate the ones and emit the result for each word.
+ **Combiner**: the same as In-Memory Combiner but using the MapReduce Combiner class.


### Instructions

For the **basic** version the user has to modify the file *WordCount.java* in the package *fr.eurecom.dsg.mapreduce*. The user must change each TODO with its own code following the TODO informations.

For The **In-Memory** Combiner and the **Combiner** the user has to modify
*WordCountIMC.java*
and *WordCountCombiner.java* in the same package. The user must change each
TODO using the same code of the basic word count example except for the TODOs
marked with a star *. Those must be changed with the correct design pattern.

### Example of usage
The final version should get in input three arguments: the number of reducers,
the input file and the output path. Example of execution are:

```
hadoop jar <compiled_jar> fr.eurecom.dsg.mapreduce.WordCount 3 <input_file> <output_path>
hadoop jar <compiled_jar> fr.eurecom.dsg.mapreduce.WordCountIMC 3 <input_file> <output_path>
hadoop jar <compiled_jar> fr.eurecom.dsg.mapreduce.WordCountCombinator 3 <input_file> <output_path>
```

To test your code use the file [<a href="inputs/quote">quote</a>]. Expected result in the file [<a href="inputs/wc_quote.out">wordcount quote result</a>].


## Pair

Use a composite key to emit an occurrence of a pair of words. In
this exercise the user will understand how to create a Hadoop data type that can
be used for MapReduce jobs.
A Pair is a tuple composed by two elements that is
usually used to ship two object inside one object. For this exercise the user has
to implement a TextPair, that is a Pair that contains two words.

### Instructions
There are two files for this exercise:

+ *TextPair.java*: data structure to be implemented by the user. Besides the implementation of the data structure itself, the user has to implement the serialization Hadoop API (write and readFields).
+ *Pair.java*: the implementation of a pair example using *TextPair.java* as datatype. **The user must not change this file**.

### Example of usage
The final version should get in input three arguments: the number of reducers, the input file and the output path. Example of execution are:
```
hadoop jar <compiled_jar> fr.eurecom.dsg.mapreduce.Pair 1 <input_file> <output_path>
```
To test your code use the file [<a href="inputs/quote">quote</a>].
Expected result in the file [<a href="inputs/pair_quote.out">pair quote result</a>].


## Stripes
Stripes are used to put inside a value an associative array that can be serialized and deserialized. The exercise is the same as Pair, but using your structure.

### Instructions
There are two files for this exercise:

+ *StringToIntAssociativeArray.java*: the data structure file, to be implemented
+ *Stripes.java*: the MapReduce job, that the user must implement using the StringToIntAssociativeArray data structure

### Example of usage
```
hadoop jar <compiled_jar> fr.eurecom.dsg.mapreduce.Stripes 2 <input_file> <output_path>
```
To test your code use the file [<a href="inputs/quote">quote</a>].
Expected result in the file [<a href="inputs/stripe_quote.out">stripes quote result</a>].


## Order Inversion
Order inversion is a design pattern used for computing relative frequencies. What is the number of occourrence of a pair containing a word as first element related to the number of occourrence of that word. For instance, if the word "dog" followed by the word "cat" occours 10 times and the word "dog" occours 20 in total, we say that the frequency of the pair ("dog","cat") is 0.5. The user has to implement the map and reduce method and the special partitioner (see OrderInversion.PartitionerTextPair class) that permits to send all data about a particular word to a single reducer. Note that inside the OrderInversion class there is a field called ASTERISK which should be used for output the total number of occourrence of a word. Refer to slides for more informationss

### Instructions
There is one file for this exercise called *OrderInversion.java*. The run method of the job is already implemented, the user should program the mapper, the reducer and the partitioner (see the TODO)

### Example of usage
```
hadoop jar <compiled_jar> fr.eurecom.fr.mapreduce.OrderInversion 4 <input_file> <output_path>
```

To test your code use the file [<a href="inputs/quote">quote</a>].
Expected result in the file [<a href="inputs/oi_quote.out">order inversion quote result</a>].

## Joins
In MapReduce the term join refers to merging two different dataset stored as unstructured files on HDFS. As for databases, in MapReduce there are many different kind of joins, each one with its use-cases and constraints. In this laboratory the user will implement two different kind of MapReduce joins techniques:

+ **Distributed Cache Join**: this join technique is use when on of the two files that you want to join is small enough to stay on each computer of the cluster. This file is copied locally to each computer using the Hadoop cache system and then loaded by the map phase.
+ **Reduce-Side Join**: in this case the map phase tags each record such that records of different inputs that have to be joined will have the same tag. Each reducer will receive a tag with a list of records and perform the join.

### Instructions

+ **Distributed Cache Join**: the file is *DistributedCacheJoin.java*, for the distributed cache use the [<a href="https://hadoop.apache.org/common/docs/r0.20.2/api/org/apache/hadoop/filecache/DistributedCache.html">DistributedCache API</a>].
+ **Reduce Side Join**: the file is *ReduceSideJoin.java*. This exercise is different from the others because it does not contain any information on how to do it. The user is free to choose how to implement it.

### Example of usage
```
hadoop jar <compiled_jar> fr.eurecom.fr.mapreduce.DistributedCacheJoin 1 <big_file> <small_file> <output_path>
hadoop jar <compiled_jar> fr.eurecom.fr.mapreduce.ReduceSideJoin 1 <input_file2> <input_file1> <output_path>
```
