# SETTING UP YOUR WORKING ENVIRONMENT

You will be working as follows:

- On your **local machine** (your laptop, a machine in the laboratory, ...): this is where you will clone the git repository, where you will create your own git repository, and where you will spend most of the time developing your algorithms.

- On the **gateway machine**: this is a virtual machine in the Eurecom cloud computing platform. You will use this machine to sync your working repository (you will pull the code of your algorithms you developed on your local machine), and to package it such that it can be submitted to the YARN cluster. You will also use this machine to actually submit your MapReduce jobs to the YARN cluster.

- On the **YARN Cluster**: this is a virtualized YARN cluster, consisting of 22 powerful virtual machines executing all the tools necessary for Hadoop MapReduce to work. You don't have direct access to the cluster, but can interact with some of the web-interfaces that allow checking the progress and the logs of your jobs.

Next, we will see how to configure and/or use each of the above components.

## The Local Machine
In what follows, we assume you will be using a machine in the laboratory. At the end of this section, there will be some information about installing the required software on your laptop.

Laboratory machines are equipped with:

- git: this is the version control system we will use in our laboratories. Specifically, you will use the internal GitLab account available at Eurecom.

- maven: this is the tool we will use to build our MapReduce job projects. It manages dependencies and create compressed archives of your job binary code, that can be directly shipped to the YARN cluster

- IntelliJ: this is a popular IDE, that we will use as a lightweight replacement for Eclipse

### Obtain a local copy of this repository:
First of all, you should clone locally this repository. There are two ways for doing this:

- Clone the repository: ```git clone https://github.com/michiard/CLOUDS-LAB.git```
- Download an archive of the repository: click on this [Download][downloadrepo]

[downloadrepo]: https://github.com/michiard/CLOUDS-LAB/archive/master.zip "Download"

**Create a new repository in GitLab**

### Make sure maven is properly configured

### Use IntelliJ to create a new project


## The Gateway Machine:

This step is only necessary if you're a student at EURECOM. Follow these steps:

- Obtain from the Teaching Assistants a group identifier: e.g. group07
- Obtain from the Teaching Assistants a secret RSA key associated to your group identifier
- Log to the gateway machine: ```ssh -i ~/.ssh/group07 group07@192.168.45.181```

At this point you have gained access to the Eurecom cloud computing platform, and you will be able to submit your MapReduce jobs. To do so, follow these steps:

- Clone your own GitLab repository in a directory of your choice in the gateway machine
- Generate ```jar''' archives of your MapReduce job
- Submit your job: ```hadoop jar <jarname.jar> <fully.qualified.class.Name> <Parameters>```

Note that you need to specify a **non existing** output directory, or to delete it before running the job.






## The YARN Cluster

Hadoop publishes some web interfaces that display JobTracker and HDFS statuses.
Depending on your cluster configuration, you will need to type a url in your browser tat point to:

- JobTracker Web Interface (generally on port 50030)
- NameNode Web Interface (generally on port 50070)

If you are a student at EURECOM, use the following URLs:

- JobTracker: ```http://192.168.45.14:50030/```
- NameNode: ```http://192.168.45.14:50070/```
