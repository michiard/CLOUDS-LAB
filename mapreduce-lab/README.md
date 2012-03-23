# MapReduce Laboratory

In this laboratory students will learn how to use the [<a href="http://hadoop.apache.org">Hadoop</a>] client API by working on a series of exercises: Word Count and variations on the theme, Design Pattern: Pair, Design Pattern: Stripes, Design Pattern: Order Inversion and finally Join implementations.

## Additional documentation for the laboratory:
The following [Link][cheatsheet] contains a "cheat-sheet" to help students with common commands on Hadoop.

[cheatsheet]: https://github.com/michiard/CLOUDS-LAB/blob/master/C-S.md "Cheatsheet"

### How to create an Eclipse project containing the laboratory source code:
+ Download the course source code from [the download page][download]
+ Open Eclipse and select *File -> New -> Java Project*
+ Give a name to the project, click the *Next* button and select the *Libraries* tab
+ Click on *Add External JARs...* and select *hadoop-core-0.20.203.0.jar* from the Hadoop directory. Click on the *Finish* button
+ Select the new project then select *File -> Import*
+ Select the laboratory source inside the CLOUDS-LAB directory. Each laboratory has its own source directory, for example the mapreduce laboratory is inside the directory *CLOUDS-LAB/mapreduce-lab*
+ Select the *src* subdirectory and click on the *finish* button

[download]: https://github.com/michiard/CLOUDS-LAB/downloads "CLOUDS-LAB download"

# Exercises
Note, exercises are organized in ascending order of difficulty. An high level overview of this laboratory (excluding the exercises on joins) is available at this [Link][mr-lab]

[mr-lab]: http://www.eurecom.fr/~michiard/teaching/clouds/mr-lab.pdf "Algorithm Design"

## Word Count

Count the occurrences of each word in a text file. This is the simplest example of MapReduce job: in the following we illustrate three possible implementations.

+ **Basic**: the map method emits 1 for each word. The reduce aggregates the ones it receives from mappers for each key it is responsible for and save on disk (HDFS) the result
+ **In-Memory Combiner**: instead of emitting 1 for each encountered word, the map method (partially) aggregates the ones and emit the result for each word
+ **Combiner**: the same as In-Memory Combiner but using the MapReduce Combiner class


### Instructions

For the **basic** version the student has to modify the file *WordCount.java* in the package *fr.eurecom.dsg.mapreduce*. The user must operate on each TODO filling the gaps with code, following the description associated to each TODO.

For The **In-Memory** Combiner and the **Combiner** the student has to modify *WordCountIMC.java* and *WordCountCombiner.java* in the same package referenced aboce. The student has to operate on each TODO using the same code of the basic word count example except for the TODOs marked with a star *. Those must be completed with using the appropriate design pattern.

### Example of usage
The final version should get in input three arguments: the number of reducers, the input file and the output path. Example of execution are:

```
hadoop jar <compiled_jar> fr.eurecom.dsg.mapreduce.WordCount 3 <input_file> <output_path>
hadoop jar <compiled_jar> fr.eurecom.dsg.mapreduce.WordCountIMC 3 <input_file> <output_path>
hadoop jar <compiled_jar> fr.eurecom.dsg.mapreduce.WordCountCombinator 3 <input_file> <output_path>
```

To test your code use the file [<a href="mapreduce-lab/inputs/quote">quote</a>]. Expected result in the file [<a href="mapreduce-lab/inputs/wc_quote.out">wordcount quote result</a>].


## Design Pattern: Pair

Use a composite key to emit an occurrence of a pair of words. In this exercise the student will understand how to create a custom Hadoop data type.

A Pair is a tuple composed by two elements that can be used to ship two objects within a partent object. For this exercise the student has to implement a TextPair, that is a Pair that contains two words.

### Instructions
There are two files for this exercise:

+ *TextPair.java*: data structure to be implemented by the student. Besides the implementation of the data structure itself, the student has to implement the serialization Hadoop API (write and read Fields).
+ *Pair.java*: the implementation of a pair example using *TextPair.java* as datatype. **The student must not change this file**.

### Example of usage
The final version should get in input three arguments: the number of reducers, the input file and the output path. Example of execution are:
```
hadoop jar <compiled_jar> fr.eurecom.dsg.mapreduce.Pair 1 <input_file> <output_path>
```
To test your code use the file [<a href="mapreduce-lab/inputs/quote">quote</a>].
Expected result in the file [<a href="mapreduce-lab/inputs/pair_quote.out">pair quote result</a>].


## Design Pattern: Stripes
Stripes are used to put inside a value an associative array that can be serialized and deserialized. The underlying goal of this exercise is the same as for Pair design pattern.

### Instructions
There are two files for this exercise:

+ *StringToIntAssociativeArray.java*: the data structure file, to be implemented
+ *Stripes.java*: the MapReduce job, that the student must implement using the StringToIntAssociativeArray data structure

### Example of usage
```
hadoop jar <compiled_jar> fr.eurecom.dsg.mapreduce.Stripes 2 <input_file> <output_path>
```
To test your code use the file [<a href="mapreduce-lab/inputs/quote">quote</a>].
Expected result in the file [<a href="mapreduce-lab/inputs/stripe_quote.out">stripes quote result</a>].


## Design Pattern: Order Inversion
Order inversion is a design pattern used for computing relative frequencies of word occurences. What is the number of occourrence of a pair containing a word as first element related to the number of occourrence of that word. For instance, if the word "dog" followed by the word "cat" occours 10 times and the word "dog" occours 20 times in total, we say that the frequency of the pair ("dog","cat") is 0.5. The student has to implement the map and reduce methods and the special partitioner (see OrderInversion.PartitionerTextPair class) that permits to send all data about a particular word to a single reducer. Note that inside the OrderInversion class there is a field called ASTERISK which should be used to output the total number of occourrences of a word. Refer to the laboratory slides for more information.

### Instructions
There is one file for this exercise called *OrderInversion.java*. The run method of the job is already implemented, the student should program the mapper, the reducer and the partitioner (see the TODO).

### Example of usage
```
hadoop jar <compiled_jar> fr.eurecom.fr.mapreduce.OrderInversion 4 <input_file> <output_path>
```

To test your code use the file [<a href="mapreduce-lab/inputs/quote">quote</a>].
Expected result in the file [<a href="mapreduce-lab/inputs/oi_quote.out">order inversion quote result</a>].

## Joins
In MapReduce the term join refers to merging two different dataset stored as unstructured files in HDFS. As for databases, in MapReduce there are many different kind of joins, each with its use-cases and constraints. In this laboratory the student will implement two different kinds of MapReduce join techniques:

+ **Distributed Cache Join**: this join technique is used when one of the two files to join is small enough to fit (eventually in memory) on each computer of the cluster. This file is copied locally to each computer using the Hadoop distribute cache and then loaded by the map phase.
+ **Reduce-Side Join**: in this case the map phase tags each record such that records of different inputs that have to be joined will have the same tag. Each reducer will receive a tag with a list of records and perform the join.

### Instructions

+ **Distributed Cache Join**: the file is *DistributedCacheJoin.java*, for the distributed cache use the [<a href="https://hadoop.apache.org/common/docs/r0.20.2/api/org/apache/hadoop/filecache/DistributedCache.html">DistributedCache API</a>].
+ **Reduce Side Join**: the file is *ReduceSideJoin.java*. This exercise is different from the others because it does not contain any information on how to do it. The student is free to choose how to implement it.

### Example of usage
```
hadoop jar <compiled_jar> fr.eurecom.fr.mapreduce.DistributedCacheJoin 1 <big_file> <small_file> <output_path>
hadoop jar <compiled_jar> fr.eurecom.fr.mapreduce.ReduceSideJoin 1 <input_file2> <input_file1> <output_path>
```

