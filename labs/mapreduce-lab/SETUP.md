# SETTING UP YOUR WORKING ENVIRONMENT

You will be working as follows:

- On your **local machine** (your laptop, a machine in the laboratory, ...): this is where you will clone the git repository, where you will create your own git repository, and where you will spend most of the time developing your algorithms.

- On the **gateway machine**: this is a virtual machine in the Eurecom cloud computing platform. You will use this machine to sync your working repository (you will pull the code of your algorithms you developed on your local machine), and to package it such that it can be submitted to the YARN cluster. You will also use this machine to actually submit your MapReduce jobs to the YARN cluster.

- On the **YARN Cluster**: this is a virtualized YARN cluster, consisting of 22 powerful virtual machines executing all the tools necessary for Hadoop MapReduce to work. You don't have direct access to the cluster, but can interact with some of the web-interfaces that allow checking the progress and the logs of your jobs.

![The working ennvironment](https://farm6.staticflickr.com/5800/22131139586_5c38b65b49_b.jpg)

Next, we will see how to configure and/or use each of the above components.

## The Local Machine
In what follows, we assume you will be using a machine in the laboratory. At the end of this section, there will be some information about installing the required software on your laptop.

Laboratory machines are equipped with:

- git: this is the version control system we will use in our laboratories. Specifically, you will use the internal GitLab account available at Eurecom.

- maven: this is the tool we will use to build our MapReduce job projects. It manages dependencies and create compressed archives of your job binary code, that can be directly shipped to the YARN cluster

- IntelliJ: this is a popular IDE, that we will use as a lightweight replacement for Eclipse

During laboratories, you will work with 2 repositories:

- **The clone of this repository**: You will use this repository as a series of templates for your MapReduce jobs, that you will use/copy/edit in your own **GitLab** repository
- **Your GitLab repository**: It is the repository which contains the source code of  *your group*.

#### Obtain a local copy of this repository:

First of all, you should clone locally this repository. There are two ways for doing this:

- Clone the repository: ```git clone https://github.com/michiard/CLOUDS-LAB.git```. On Windows, if you have problem about SSL certificate, please use command: ```env GIT_SSL_NO_VERIFY=true git clone https://github.com/michiard/CLOUDS-LAB.git```
- Download an archive of the repository: click on this [Download][downloadrepo]

[downloadrepo]: https://github.com/michiard/CLOUDS-LAB/archive/master.zip "Download"



#### Create a new repository in GitLab
Proceed with the following steps:

- Login to your GitLab web interface (http://gitlab.eurecom.fr) and use LDAP credentials to login (these are your UNIX account credentials from Eurecom).

- Click the button to create a new project. Define your project name (e.g., mr-lab). In case you already belong to GitLab groups, or you have other identifiers, you should use the name space corresponding to your user name (**NOTE: this option is not available if you never used GitLab, just ignore it**). Add some description to the repository (e.g., This repo is for the CLOUDS course laboratory). Define the visibility level of your repository.

- Once you click the button to create the project, you will be directed to an instruction page that you will need to follow to actually create the project on your **local machine**. When executing command "push", you will see an error about permission. Don't worry. After submitting the private key of your machine in the next steps, you will be able to push your repository without any problem.

- Through the GitLab web interface, you can also define members of the project, so add anybody who is in your group as members, such that they will be able to work independently on the repository.

Now, to complete the procedure of creating your repository, you need to upload an RSA public key through the web interface (Profile -> SSH Keys). This will allow you to "push" your changes without being asked a password for every operation. Proceed with the following steps:

- Generate a private/public RSA key pair for your GitLab repository
  - On Linux, in the local machine, home directory, type: ```cd ~/.ssh/; ssh-keygen -t RSA```. On Windows, type: ```cd ~/.ssh/; ssh-keygen```
  - Give the key-pair a name, such as: ```id_rsa_gitlab``` then enter the password for this file if needed. You can just press *Enter* to leave it blank. **Note:** After this step, there are two generated file: ```id_rsa_gitlab.pub``` and ```id_rsa_gitlab```. The former is the public key. The latter is your gitlab private key . From now, when we talk ```<your_gitlab_private_key>```, it aims to this file. You will need them in order to connect to the GitLab repository.
- Upload your public key to GitLab
  - Run ```cat id_rsa_gitlab.pub``` to read the content of your public key.
  - Go to the settings menu of your GitLab web-application ((Profile -> SSH Keys))
  - Add the public key by paste the above content

**Note**: By default, Git only use the private key *id_rsa* in authenticating. To tell git use our key, do the following:
- ```[ ! -f ~/.ssh/config ] && touch ~/.ssh/config```
- Open file ```~/.ssh/config``` and put the content below:
```
host gitlab.eurecom.fr
 HostName gitlab.eurecom.fr
 IdentityFile ~/.ssh/id_rsa_gitlab
 User git
```

#### Make sure maven is properly configured
Proceed with the following steps:

- Create a new (hidden) directory in your home: ```mkdir .m2```. **NOTE: this step is not necessary if you have already used maven in the past**.
- Create a new `settings.xml`file in the `~/.m2` directory, and edit it such that it looks like the following:

```
<?xml version="1.0" encoding="UTF-8"?>
<settings>
    <profiles>
        <profile>
            <id>standard-extra-repos</id>
            <activation>
                <activeByDefault>true</activeByDefault>
            </activation>
            <repositories>
                <repository>
                    <!-- Central Repository -->
                    <id>central</id>
                    <url>http://repo1.maven.org/maven2/</url>
                    <releases>
                        <enabled>true</enabled>
                    </releases>
                    <snapshots>
                        <enabled>true</enabled>
                    </snapshots>
                </repository>
                <repository>
                    <!-- Cloudera Repository -->
                    <id>cloudera</id>
                    <url>https://repository.cloudera.com/artifactory/cloudera-repos</url>
                    <releases>
                        <enabled>true</enabled>
                    </releases>
                    <snapshots>
                        <enabled>true</enabled>
                   </snapshots>
                </repository>
            </repositories>
        </profile>
    </profiles>
</settings>
```

#### Use IntelliJ to create a new project
We assume maven to be properly configured, as described above. We also assume a student to have successfully created a git repository in GitLab: for example, the repository name (and directory) could be ```mr-lab```. In what follows, we provide a simple guide to create a new IntelliJ project that takes the form of a new directory in the repository.

Proceed with the following steps:

- Launch IntelliJ
  - Type in the command line the following: ```/packages/idea/bin/idea.sh&```
  - Note: you can add the directory to your ```PATH``` environment variable and avoid to use the full path every time you start IntelliJ
- Create a new project in IntelliJ
  - If it is the first time you launch IntelliJ, you will have to configure your SDK. Select the following directory for your JDK installation: ```/usr/java```
  - Select maven from the list of project types
  - Check the box **create from archetype**
  - Select ```org.apache.maven.archetypes:maven-archetype-quickstart```
  - Click Next
- Define project identifiers. For each of the following fields, type:
  - GroupId: ```fr.eurecom.dsg.mapreduce```
  - ArtifactId: ```Your-project-name```, e.g., ```WordCount```
  - Version: ```1.0-SNAPSHOT``` works fine, but you can customize if you want
  - Click Next
- Verify the proper configuration of maven settings and of your project and click Next
- Chose a project name: e.g., WordCount
- Verify the project location is correctly placed in your GitLab repository
- Click Finish

**NOTE 1**: at this point, IntelliJ may ask you to manage your git repository. You have the choice to ignore the suggestion, or allow IntelliJ to assist you. We assume that you ignore the suggestion in what follows.

**NOTE 2**: IntelliJ may also ask you to enable the automatic import of definitions and dependencies. Agree to enable automatic import. In case you missed the IntelliJ tip, you have to manually configure automatic imports. Do the following:

- Go to the ```File``` menu and select ```Settings```
- Select the **Build, Execution, Deployment** tab on the left
- Select **Build Tools**
- Click Maven, and expand it (on the left tab)
- Check the **Import Maven projects automatically**


By now you should have a new window open with your new project, displaying the ```pom.xml``` file that we will need to configure.

Configuration of the ```pom.xml``` file. Proceed with the following steps:

- Remove the entire block ```<dependencies> ... </dependencies>```
- Copy the following lines as a replacement of the block you just removed

```
<repositories>
  <repository>
  <!-- Cloudera Repository -->
    <id>cloudera</id>
    <url>https://repository.cloudera.com/artifactory/cloudera-repos</url>
    <releases>
        <enabled>true</enabled>
        </releases>
        <snapshots>
          <enabled>true</enabled>
        </snapshots>
  </repository>
</repositories>

<dependencyManagement>
        <dependencies>
            <dependency>
                <groupId>jdk.tools</groupId>
                <artifactId>jdk.tools</artifactId>
                <version>1.7</version>
            </dependency>
            <dependency>
                <groupId>org.apache.hadoop</groupId>
                <artifactId>hadoop-hdfs</artifactId>
                <version>2.5.0-cdh5.3.2</version>
            </dependency>
            <dependency>
                <groupId>org.apache.hadoop</groupId>
                <artifactId>hadoop-auth</artifactId>
                <version>2.5.0-cdh5.3.0</version>
            </dependency>
            <dependency>
                <groupId>org.apache.hadoop</groupId>
                <artifactId>hadoop-common</artifactId>
                <version>2.5.0-cdh5.3.2</version>
            </dependency>
            <dependency>
                <groupId>org.apache.hadoop</groupId>
                <artifactId>hadoop-core</artifactId>
                <version>2.5.0-mr1-cdh5.3.2</version>
            </dependency>
            <dependency>
                <groupId>org.apache.hadoop</groupId>
                <artifactId>hadoop-mapreduce-client-core</artifactId>
                <version>2.5.0-cdh5.3.2</version>
            </dependency>
            <dependency>
                <groupId>junit</groupId>
                <artifactId>junit-dep</artifactId>
                <version>4.8.2</version>
            </dependency>
        </dependencies>
    </dependencyManagement>
    <dependencies>
        <dependency>
            <groupId>org.apache.hadoop</groupId>
            <artifactId>hadoop-hdfs</artifactId>
        </dependency>
        <dependency>
            <groupId>org.apache.hadoop</groupId>
            <artifactId>hadoop-auth</artifactId>
        </dependency>
        <dependency>
            <groupId>org.apache.hadoop</groupId>
            <artifactId>hadoop-common</artifactId>
        </dependency>
        <dependency>
            <groupId>org.apache.hadoop</groupId>
            <artifactId>hadoop-core</artifactId>
        </dependency>
        <dependency>
            <groupId>junit</groupId>
            <artifactId>junit</artifactId>
            <version>4.10</version>
            <scope>test</scope>
        </dependency>
    </dependencies>
    <build>
        <plugins>
            <plugin>
                <groupId>org.apache.maven.plugins</groupId>
                <artifactId>maven-compiler-plugin</artifactId>
                <version>2.1</version>
                <configuration>
                    <source>1.7</source>
                    <target>1.7</target>
                </configuration>
            </plugin>
        </plugins>
    </build>

```
Now you can save your new ```pom.xml``` file.

Next, you can start working on your project source files:

- Navigate (using the menu on the left of your project window) the sources to reach the basic template of your new java application: it should be called App.
- Right-click on the APP class and **refactor** it such that it has a coherent name, e.g. ```WordCount```. You will find the refactor command, and the rename sub-command in the menu that appears at the right-click.
- Follow the suggestions to refactor also the **test** path of your project

You now are ready to proceed with the exercises. For example, you can open the java file of an exercise you cloned from the **CLOUDS-LAB repository** (e.g., WordCount) and simply replace the whole code in your new IntelliJ project with that of the exercise. At this point, you will be able to start editing the source and fill-in the ```TODO``` sections of the code.






## The Gateway Machine:

This step is only necessary if you're a student at EURECOM. Follow these steps:

- Obtain from the Teaching Assistants a group identifier: e.g. groupXY
- Obtain the username (group ID) and password from the Teaching Assistants
- Log to the gateway machine: ```ssh groupXY@192.168.45.181``` and type your given password.

#### Make sure JAVA_HOME environment variable is appropriately set
When you are logged in the **Gateway Machine** you must check that you're using a consistent version of Java. Make sure to set the below command everytime you login into the **Gateway machine**:

- ```export JAVA_HOME=/usr/lib/jvm/java-7-oracle-cloudera```

#### Make sure the secret key of the GitLab repo is available in the Gateway Machine
Since you will be working on the very same GitLab repository you created in your **local machine**, you need to copy the private key of the repo from your **local machine** to the **gateway machine**. Do the following:

- On your **local machine**:
  - ```scp your_GitLab_secret_key <groupID>@192.168.45.181:~/.ssh/```
  - ```[ -f ~/.ssh/config ] && scp ~/.ssh/config <groupID>@192.168.45.181:~/.ssh/```

#### Package and submit your MapReduce jobs
At this point you have gained access to the Eurecom cloud computing platform, and you will be able to submit your MapReduce jobs. To do so, follow these steps:

- Clone your own GitLab repository in a directory of your choice in the gateway machine
- Generate **JAR** archives of your MapReduce job. This can be done once you are in the sub-directory of your repository containing the MapReduce job you want to submit: simply type ```mvn package```, and maven will download all required dependencies (it may take some time, the first time you "compile"), compile your job, and generate the **JAR** in the sub-directory ```target```.
- Submit your job: ```hadoop jar <path/jarname.jar> <fully.qualified.class.Name> <Parameters>```

Note that you need to specify a **non existing** output directory for your MapReduce jobs, or to delete it before running the job.






## The YARN Cluster

YARN publishes some web interfaces that display Resource Manager, Application Master and HDFS statuses. For Eurecom students, point your browser to:


- The Resource Manager interface: ```192.168.45.167:8088/cluster```
- The MapReduce Job History interface: ```192.168.45.167:19888/jobhistory```
- NameNode interface: ```192.168.45.157:50070/dfshealth.html#tab-overview```

#### IMPORTANT NOTE
The DNS service we use in our Cloud computing platform is currently not connected to the DNS service at Eurecom. This means that the IP addresses above are not matched to fully qualified names as they should.

This is in general not a problem. In case you want to try an experimental setup, do the following:

- From your **local machine** log to the **gateway machine**, but allow X forwarding: ```ssh -X  <groupID>@192.168.45.181```
- Launch Firefox in the **gateway machine**: ```firefox&```

Now, you will be able to access the above web interfaces **and** have the correct DNS host resolution. The downside is that this approach can overload the gateway machine, and consume quite some network traffic.

