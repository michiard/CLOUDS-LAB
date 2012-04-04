# Laboratory material for the Clouds course: [Link][course]
[course]: http://www.eurecom.fr/~michiard/clouds.html "Course Web Page"

This space supports the Lectures on Hadoop MapReduce, Hadoop Pig and Hadoop HBase. Each lecture has an associated Lab, consisting in a directory holding a description of the exercises, source code, solutions and output data. Input data is currently stored in a private Hadoop deployment at Eurecom; in some cases, scripts to generate input data or small input samples are provided.

For information, the Hadoop cluster consists in 40 nodes, each with a quad-core CPU, 4 GB of RAM, and (only) a total of 3.5 TB storage space (with replication factor 3). Each machine is equipped with a single Gigabit Ethernet card.

**Acknowledgements**: many exercises have been profoundly influenced by two following resources:

+ Tom White, **Hadoop, The Definitive Guide**, Y!Press, O'Reilly
+ Jimmy Lin, Chris Dyer, **Data-Intensive Text Processing with MapReduce**, Morgan Claypool ed.
+ Anand Rajaraman and Jeff Ullman, **Mining of Massive Datasets**, Cambridge University Press
+ Hortonworks Blog: http://hortonworks.com/blog/

# Pre-requisites for the exercises
Next, we provide some information on the software setup required to use the laboratory material. For the laboratory sessions, we suggest students to download the whole repository, following this [Link][download]

[download]: https://github.com/michiard/CLOUDS-LAB/downloads "CLOUDS-LAB download"

## Software setup
To work with the exercises, you need to download and install java sdk and eclipse. 
You also need to download and install Hadoop core jar files.

**NOTE**: for students attending the Lab sessions at Eurecom, the software setup has been done for you. Refer to a teaching assistant for further information. See also how to configure bash below.

### Links:
+ Java download page:
        [Link][javasdk]
       
+ Hadoop download page (hadoop-0.20.203.0):
        [Link][hadoop]

+ Hadoop Pig download page (pig-0.9.2):
	[Link][pig]
    
+ Eclipse download page:
        [Link][eclipse]

[javasdk]: http://www.oracle.com/technetwork/java/javase/downloads/index.html "Java download"
[hadoop]: http://www.apache.org/dyn/closer.cgi/hadoop/common/ "Hadoop download"
[pig]: http://apache.multidist.com/pig/pig-0.9.2/pig-0.9.2.tar.gz "Pig download"
[eclipse]: http://www.eclipse.org/downloads/ "Eclipse download"

## Configuring Bash:
Note that this configuration works for studens machines in Laboratory rooms 1 and 2, and is tailored to the private Hadoop deployment at Eurecom.

```

export JAVA_HOME=/home/Admin_Data/hadoop/jdk1.6.0_24

export PIG_HADOOP_VERSION=20

export PIG_CLASSPATH=/home/Admin_Data/hadoop/hadoop/conf/

export HADOOP_HOME=/home/Admin_Data/hadoop/hadoop/

export PATH=$HADOOP_HOME/bin:$PATH

export PATH=/home/Admin_Data/hadoop/pig-0.9.2/bin:$PATH

```

# Links to the three Laboratories

+ Laboratory on Scalable Algorithm Design in MapReduce [Link][mr-lab]

+ Laboratory on Pig and Pig Latin [Link][pig-lab]

+ Laboratory on HBase

[mr-lab]: https://github.com/michiard/CLOUDS-LAB/tree/master/mapreduce-lab "MapReduce Lab"
[pig-lab]: https://github.com/michiard/CLOUDS-LAB/tree/master/pig-lab "Pig Lab"
