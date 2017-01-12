# zklab
ZooKeeper laboratory exercise, part of the CLOUDS course.

## Setup

2. Clone this repository in a local directory, if you already cloned it in a previous lab session, pull the latest version
3. Start IntelliJ: `/packages/idea/bin/idea.sh`
4. Click on "Open" and navigate to the directory `/<cloning path>/CLOUDS-LAB/labs/zk-lab` to open the pre-configured project in IntelliJ.

### Select the Java SDK

Before continuing you need to configure the JAVA SDK:

1. Click on the top-level project name on the right, it should read "zk-lab [zklab]"
2. Press F4
3. On the window that opens select "Project" on left
4. Under "Project SDK" select "1.7"
5. Click OK

IntelliJ will now create its indexes and download dependencies (the ZooKeeper client libraries in particular)

## Laboratory exercise

In this laboratory you will implement the leader election primitive using the ZooKeeper API. Leader election is a fault tolerance mechanism that ensures that a leader process is always available, even in the event of crashes.

### Objectives

1. Run multiple processes (three at least) and verify that one of them becomes the leader.
2. Terminate the leader process and verify that one of the remaining processes is elected as a new Leader
3. (bonus) restart the terminated process and verify that there is still only one leader and that no new election happened

The IntellJ project your are provided is already configured with three targets:

* Process ID 1
* Process ID 2
* Process ID 3

You can find them at the top right of the IntellJ window. Each one of these targets will execute the Process class, passing a different ID as argument. By running these processes you will be able to test your code.

### Files and missing code

The source code is available under the `src` sub-directory and is composed of three files:

* Process.java: startup class with the main() entry point
* Elections.java: the class that contains the leader election implementation
* ZooKeeperClient.java: a thin layer on top of the ZooKeeper API to simplify the exercise, no changes are needed to this file

**VERY IMPORTANT**: set the `GROUP_NAME` variable in `Process.java`, it will be used as the prefix for ZooKeeper paths used by your application. If your group number is 15, set this variable to `"group15"`

To complete the exercise you should add the code missing from the `Process.java` and `Elections.java` files. No other files need to be modified or created. Look at the comments in the code for hints of where the missing code should be added.

### When you finish

Send the `Process.java` and `Elections.java` files as attachments via email to daniele.venzano@eurecom.fr

The email must conform to the following rules, otherwise it will be ignored:

* must have the `Process.java` and `Elections.java` files attached, nothing else (no zip file or other archive formats, no other files)
* the subject must read: "[ZK lab] group XX" (where XX is your group number)
* the email should be sent before 12h

If the email does not conform to these rules we will not be able to grade your work.
