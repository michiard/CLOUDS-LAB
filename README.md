# Laboratory session on MapReduce, Hive and Pig

This page supports the Lecture on practical exercises on Hadoop MapReduce. 
In the following some basic software requirements to work with the exercises.

The tutorial and this lecture have been profoundly influenced by two "must-have" books on Hadoop and MapReduce:

+ Tom White, **Hadoop, The Definitive Guide**, Y!Press, O'Reilly
+ Jimmy Lin, Chris Dyer, **Data-Intensive Text Processing with MapReduce**, Morgan Claypool ed.

## Software setup
To work with the exercises, you need to download and install java sdk and eclipse. 
You also need to download and install Hadoop.

### Links:
+ Java download page:
        [Link][javasdk]
       
+ Hadoop download page (hadoop-0.20.203.0):
        [Link][hadoop]
    
+ Eclipse download page:
        [Link][eclipse]

[javasdk]: http://www.oracle.com/technetwork/java/javase/downloads/index.html "Java download"
[hadoop]: http://www.apache.org/dyn/closer.cgi/hadoop/common/ "Hadoop download"
[eclipse]: http://www.eclipse.org/downloads/ "Eclipse download"

### Additional documentation for the laboratory:
The following [Link][cheatsheet] contains a "cheat-sheet" to help students with common commands on Hadoop.

[cheatsheet]: https://github.com/michiard/CLOUDS-LAB/blob/master/C-S.md "Cheatsheet"

### How to create an Eclipse project containing the laboratory source code:
+ Download the course source code from [the download page][download]
+ Open Eclipse and select *File -> New -> Java Project*
+ Give a name to the project, click the *Next* button and select the *Libraries* tab
+ Click on *Add External JARs...* and select *hadoop-core-0.20.203.0.jar* from the Hadoop directory. Click on the *Finish* button
+ Select the new project then select *File -> Import*
+ Select the laboratory source inside the CKOUDS-LAB directory. Each laboratory has its own source directory, for example the mapreduce laboratory is inside the directory *CLOUDS-LAB/mapreduce-lab*
+ Select the *src* subdirectory and click on the *finish* button


[download]: https://github.com/michiard/CLOUDS-LAB/downloads "CLOUDS-LAB download"