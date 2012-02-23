# HADOOP Cheat Sheet

## Working with HDFS from the command line
`$HADOOP_PATH/bin/hadoop dfs <CMD>`

### Inspect files	
+ `-ls <path>`:: list all files in `<path>`
+ `-cat <src>`:: print `<src>` on `stdout`
+ `-tail [-f] <file>`:: output the last part of the `<file>`
+ `-du <path>`:: show `<path>` space utilization

### Create/remove files	
+ `-mkdir <path>`:: create a directory
+ `-mv <src> <dst>`:: move (rename) files
+ `-cp <src> <dst>`:: copy files
+ `-rmr <path>`:: remove files

### Copy/Put files from a remote machine into the HADOOP cluster
+ `-copyFromLocal <localsrc> <dst>`:: copy a local file to the HDFS
+ `-copyToLocal <src> <localdst>`:: copy a file on the HDFS to the local disk

### HELP
+ `-help [cmd]`:: hopefully this is self-describing

#### Examples:
`$HADOOP_PATH/bin/hadoop dfs -ls /`

`$HADOOP_PATH/bin/hadoop dfs -copyFromLocal myfile remotefile`

## Launching Hadoop Jobs - Ccommand line
+ Copy the jar file of your job to a cluster machine (let's call it `machine_name`)

`scp localJarFile studentXX@machine_name:~/`

+ SSH to machine_name:

`ssh studentXX@machine_name`

+ Launch the job:

`$HADOOP_PATH/hadoop jar jarFile.jar ClassNameWithPackage [job args]`

++ `If the output directory exists:`

`$HADOOP_PATH/hadoop dfs -rmr output`

#### Example:
`$HADOOP_PATH/bin/hadoop jar fr.eurecom.dsg.WordCount /user/hadoop/wikismall.xml output 2`
