# Design of a URL shortner service with HBase
Credits: this exercise has been adapted from the case study described in the book: “HBase, the Definitive Guide”, Lars George, O’Reilly.

*URL shortening is a technique on the World Wide Web in which a Uniform Resource Locator (URL) may be made substantially shorter in length and still direct to the required page. This is achieved by using an HTTP Redirect on a domain name that is short, which links to the web page that has a long URL. For example, the URL http://en.wikipedia.org/wiki/URL_shortening can be shortened to http://bit.ly/urlwiki or http://tinyurl.com/urlwiki.* (wikipedia)

Moreover, besides a giant lookup table, usually url shortner services offer user registration, tracking of statistics associated to each shorturl, such as the number of clicks per day, week, month and in total, and the country of origin of the requests.

The objective of this laboratory is to design a simple schema for such a service using HBase.

### A note on the dataset
In the following, we synthesize a dataset to be used in the exercise. Usernames and long URLs are _real_: we extracted them from an openly available dataset from http://delicio.us. Short URLs and Cliks are synthetic, with non-uniform distributions: Short URLs are base-64 encoded, clicks are log-normally distributed, click inter-arrival time is exponentially distributed starting from the time a short URL is created.

## Setup the laboratory sources in Eclipse:

The first step is to import the laboratory source code into Eclipse.

+ Download the project file [hbase-lab.jar][hbase-lab.jar]
+ Open Eclipse and select File -> Import... -> Existing Projects into Workspace
+ From the Import Projects dialog window, choose Select archive file and then Browse... to import hbase-lab.jar that you downloaded at step 1
+ Select the project hbase-lab and the press the Finish button

At this point you should have a java project named hbase-lab already configured that compiles.

[hbase-lab.jar]: https://github.com/michiard/CLOUDS-LAB/raw/master/hbase-lab/hbase-lab.jar "hbase-lab.jar"


## Background: A SQL implementation

Before working on HBase, let us look at a traditional database design, with normalized relationships.
The Entity Relationship Diagram for our service is reported in the following figure. The corresponding SQL schema is shown immediately below it.

![ER-Diagram](https://raw.github.com/michiard/CLOUDS-LAB/master/hbase-lab/figure/er.png)

```SQL

CREATE TABLE IF NOT EXISTS user (
    id INTEGER UNSIGNED NOT NULL AUTO_INCREMENT,
    username CHAR(40) NOT NULL,
    credentials CHAR(12) NOT NULL,
    roles CHAR(10) NOT NULL,
    lastname CHAR(30),
    email VARCHAR(60),
    CONSTRAINT pk_user PRIMARY KEY (id),
    CONSTRAINT idx_user_username UNIQUE INDEX (username)
);

CREATE TABLE IF NOT EXISTS url (
    id INTEGER UNSIGNED NOT NULL AUTO_INCREMENT,
    url VARCHAR(200) NOT NULL,
    title VARCHAR(200),
    description VARCHAR(400),
    content TEXT,
    CONSTRAINT idx_url UNIQUE INDEX (url),
    CONSTRAINT pk_url PRIMARY KEY (id)
);


CREATE TABLE IF NOT EXISTS shorturl (
    id INTEGER UNSIGNED NOT NULL AUTO_INCREMENT,
    userId INTEGER,
    urlId INTEGER,
    shortId CHAR(8) NOT NULL,
    description VARCHAR(400),
    datestamp DATETIME,
    CONSTRAINT pk_shorturl PRIMARY KEY (id),
    CONSTRAINT idx_shorturl_shortid UNIQUE INDEX (shortId),
    FOREIGN KEY fk_user (userId) REFERENCES user (id),
    FOREIGN KEY fk_url (urlId) REFERENCES url (id)
);

CREATE TABLE IF NOT EXISTS click (
    id INTEGER UNSIGNED NOT NULL AUTO_INCREMENT,
    datestamp DATETIME,
    shortId INTEGER UNSIGNED NOT NULL,
    country CHAR(2),
    CONSTRAINT pk_clicks PRIMARY KEY (id),
    CONSTRAINT idx_click_shortid UNIQUE INDEX (shortId),
    FOREIGN KEY fk_shortid (shortId) REFERENCES shorturl (id)
);

```

### Data access pattern: Click on a shorturl
When someone clicks on a shorturl (i.e. http://goo.gl/shortID), we need to retrieve the corresponding long url and update the corresponding statististics.
Therefore, given a shorturl, we need to get the corresponding URL:


```SQL
SELECT url.url, shorturl.id
FROM shorturl, url
WHERE shorturl.shortId="6PMQ" and shorturl.urlId=url.id;
```


```
+---------------------------------------+--------+
| url                                   | id     |
+---------------------------------------+--------+
| http://www.theatermania.com/broadway/ | 710842 |
+---------------------------------------+--------+

1 row in set (0.00 sec)
```

This query can be executed very quickly: it involves only one table and an index on the column that appears in the where clause exists.

Using the id retrieved from the previous query, we can update the statistics:


```SQL
INSERT INTO click
VALUE (0, '2012-03-19 14:00:01', 1, 'IT');

Query OK, 1 row affected (0.00 sec)
```


### Data access pattern: User Authentication
We need to save data for each user, so that they can authenticate themselves, therefore
given a username and a password, we need to verify that the corresponding account exists.
In SQL:

```SQL
SELECT username, lastname, email
FROM user
WHERE username="mrfernan" and credentials="jW4BVuJt";
```


### Data access pattern: Users’ personal pages and shorturl counters
Each authenticated user needs to view all his created ShortURLs, therefore, given a userid, we need to get all its shorturls and, for each one, we need to show:

+ the associated URL
+ the number of clicks
+ its statistics
+ its creation date


Moreover, since the user can create a lot of ShorURLs, we want to list them ordered by reverse timestamp

```SQL
SELECT shorturl.shortId, url.url, count(click.id)
FROM user, shorturl, url, click
WHERE user.username="cainarachi" and
     shorturl.userId = user.id and
     shorturl.urlId = url.id and
     click.shortId=shorturl.id
GROUP by shorturl.id
ORDER BY shorturl.datestamp DESC;
```

```
+---------+------------------------------------+-----------------+
| shortId | url                                | count(click.id) |
+---------+------------------------------------+-----------------+
| 6PMU    | http://www.mcfc.co.uk/             |               1 |
| VSJ0    | http://www.history.co.uk/home.html |               6 |
+---------+------------------------------------+-----------------+
2 rows in set (0.30 sec)
```


The previous query has a problem: the user cainarachi created 3 shortIds (http://goo.gl/ELHB, http://goo.gl/6PMU and http://goo.gl/VSJ0), but the result shows just two shortIds. In fact, it does not output shortId with zero clicks, since the previous syntax (FROM t1, t2 WHERE t1.PK=t2.FK) implies an inner join: in fact, shortId "ELHB" has no clicks, when we look for click.shortId corresponding to shorturl.id, we do not find anything, so we do not output anything. We should have output a NULL instead. In this case we should use a "LEFT JOIN". Let us see what happens when using a LEFT JOIN:


```SQL
SELECT shorturl.shortId, url.url, click.id
FROM user INNER JOIN shorturl ON user.id=shorturl.userId
         INNER JOIN url ON url.id=shorturl.urlId
         LEFT JOIN click ON click.shortId=shorturl.id
WHERE user.username="cainarachi"
ORDER BY shorturl.datestamp DESC;
```

```
+---------+------------------------------------+---------+
| shortId | url                                | id      |
+---------+------------------------------------+---------+
| ELHB    | http://www.3dluvr.com/pascalb/     |    NULL |
| 6PMU    | http://www.mcfc.co.uk/             | 2164834 |
| VSJ0    | http://www.history.co.uk/home.html | 2366641 |
| VSJ0    | http://www.history.co.uk/home.html | 2366642 |
| VSJ0    | http://www.history.co.uk/home.html | 2366643 |
| VSJ0    | http://www.history.co.uk/home.html | 2366644 |
| VSJ0    | http://www.history.co.uk/home.html | 2366645 |
| VSJ0    | http://www.history.co.uk/home.html | 2366646 |
+---------+------------------------------------+---------+

8 rows in set (0.12 sec)
```


We can see that shortId 681023 has no clicks, so the corresponding click.id is NULL. Now we can correct the previous erroneous query as follow:


```SQL
SELECT shorturl.shortId, url.url, count(click.id)
FROM user INNER JOIN shorturl ON user.id=shorturl.userId
         INNER JOIN url ON url.id=shorturl.urlId
         LEFT JOIN click ON click.shortId=shorturl.id
WHERE user.username="cainarachi"
GROUP by shorturl.id
ORDER BY shorturl.datestamp DESC;
```

```
+---------+------------------------------------+-----------------+
| shortId | url                                | count(click.id) |
+---------+------------------------------------+-----------------+
| ELHB    | http://www.3dluvr.com/pascalb/     |               0 |
| 6PMU    | http://www.mcfc.co.uk/             |               1 |
| VSJ0    | http://www.history.co.uk/home.html |               6 |
+---------+------------------------------------+-----------------+
3 rows in set (0.00 sec)
```


This query is rather complicated, as it requires 3 joins:


+ First, the user table is used: the condition on the WHERE clause selects a single row. This is fast, since there is an index for user.username
+ Second we join a table with a single row with the shorturl table, matching the userId. This query is fast, as there is an index for the foreign key shorturl.userId. The result contains three rows, one for each shorturl. Of course, in case of a user with lots of shorturls (try “medicman”), the output would have been bigger.
+ Third, we join the result table with the url table using the click.shortId=shorturl.id. This query is fast, it uses only clauses with attributes having an index.
+ Finally, the click table is left-joined on the click.shorID=shorturl.id clause. Again, the join is fast, whereas the produced output can be very large.

### Data access pattern: Shorturl statistics
Finally, given a shorturl, a user needs to visualize the statistics related, e.g., number of clicks in per month:


```SQL
SELECT shortId, YEAR(datestamp), MONTH(datestamp), count(*)
FROM click
WHERE click.shortId=325056
GROUP BY YEAR(datestamp), MONTH(datestamp)
ORDER BY datestamp DESC
LIMIT 12;
```

```
+---------+-----------------+------------------+----------+
| shortId | YEAR(datestamp) | MONTH(datestamp) | count(*) |
+---------+-----------------+------------------+----------+
|  325056 |            2012 |                3 |       12 |
|  325056 |            2012 |                2 |       11 |
|  325056 |            2012 |                1 |        9 |
|  325056 |            2011 |               12 |       11 |
|  325056 |            2011 |               11 |       17 |
|  325056 |            2011 |               10 |       26 |
|  325056 |            2011 |                9 |       26 |
|  325056 |            2011 |                8 |       23 |
|  325056 |            2011 |                7 |       14 |
|  325056 |            2011 |                6 |       29 |
|  325056 |            2011 |                5 |       32 |
|  325056 |            2011 |                4 |       31 |
+---------+-----------------+------------------+----------+

12 rows in set (0.04 sec)
```


This query can be performed very quickly, but it requires the sorting of the intermediate output in order to apply the group by clause.

## Working with HBase
We need to translate this SQL schema to an HBase schema; furthermore, we will work on how to interact with HBase, that does not support SQL queries. We will do it step by step.
It is important to notice that, “normalized tables are suitable for general-purpose querying. This means any queries against these tables, including future queries whose details cannot be anticipated, are supported. In contrast, tables that are not normalized lend themselves to some types of queries, but not others” (wikipedia).

The basic principle of HBase schema designs are DDI, Denormalization, Duplication and Intelligent Keys.
HBase supports inherently denormalized schemes: it has no concept of foreign key, secondary keys or indexes, just a primary key.
Denormalized data is redundant, therefore we have to think about access patterns, in order to avoid data aggregation at read time for example.
HBase can perform queries based on the key in a efficient manner. Remember that all rows are always sorted lexicographically by their row key, and this can act as an index on the row key. As a further point, partial key lookups are possible, therefore compound keys allow to access data based on a kind of left-edge index.
HBase stores data on column families, therefore we need to group data based on the planned access patterns, in order to avoid reading useless columns.

It should be clear that we need to to create our tables based on the requirements of our application, on the queries we plan to serve and on the data access patterns.

### **EXERCISE 1: Working with Users**
We need to save and access data about each user stored in HBase

Data access patterns:

+ A user wants to authenticate: Design a Query based on the username (i.e., the row-key is the user identifier)
+ Authenticated users obtain all of his/her information (e.g. displayed on the service main page)


**Q1. Create a table (user-HOST_NAME) to save users’ information, using the HBase Shell (where HOST_NAME is the name of the machine you’re working on)**

+ *[HINT] [HBase Shell][hbaseshell]*
[hbaseshell]: http://wiki.apache.org/hadoop/Hbase/Shell "HBase Shell Wiki"

**Q2. List all the existing tables (note that you will see also the table created by your colleagues) using the HBase Shell**

**Q3. Insert into the table 100 lines from users.json in the table you created**

+ *[HINT] the input file -- users.json -- is located in the HDFS deployed in the laboratory, under the directory /data/hbase/
+ *[HINT]: You need to access the directory hbase-lab in git, and modify appropriately the java program located in the sub-directory: hbase-lab/*


**Q4. Scan the table you filled in Q3 using the HBase Shell.**

**Q5. Which RegionServers are the regions assigned to? Which range of keys does each RegionServer serve? What is the cluster utilization?**

+ *[Hint]: Use the HBase shell and scan the .META. table*

**Q6. Insert all the remaining data (from the file user.json) in the user-HOST_NAME table (where HOST_NAME is the name of the machine you’re working on).**

**Q7. Is there any split? Why? What is the size of the HFiles in the HDFS?**

+ *[Hint]: Use the HBase shell to answer this question*


### **EXERCISE 2: Working with URLs**
In this section, we will design the table used to keep the urls.

Data access patterns:

+ A user creates a new shorturl. The content of the webpage linked to the url is saved.
+ The data related to a single url is accessed by url.
+ The content of the webpage (the full body) is accessed only occasionally.
+ Another user creates another shorturl for the same url. We want to avoid saving the same content again
+ Many user will create a shorturl pointing to the same website (we can expect more shorturls for facebook pages or for newspapers websites, than for http://my.personal.blog.me). URL distribution is biased.


**Q1. Design a table to save the urls. Which row-key would you choose? How many column families?**

+ **Q1.1 What does point n.5 in the data access patterns imply?**
+ *[HINT]: Using the HBase Shell, create the table and label it ‘url-HOST_NAME’*


**Q2. Write a java program to load the url data in the table you created. To do so, you can adapt the code used to load user data in exercise 1.**

+ *[HINT] the input file -- urls.json -- is located in the HDFS deployed in the laboratory, under the directory /hbase-lab/*
+ *[HINT]: while loading the data, continue to the next exercises, as this procedure may take some time (roughly 10 minutes)*

### **EXERCISE 3: Working with Shorturls and statistics**
In this section we will design the tables used to keep the shortids and the click information/statistics.

Data access patterns:

+ A user creates a new shorturl (e.g. http://goo.gl/128a79). 128a79 is the shortId.
+ Someone clicks on the shorturl:
  + The frontend will have to find the matching long url.
  + We have to update the number of clicks
+ An authenticated user wants to see all his shorturls with the following informations:
  + associated long url
  + creation date
  + number of clicks
+ Moreover, since a user could have created a long number of shorturls, we want to visualize the last 25 created shorturls. Of course the remaining shorturls should be accessible if requested.
+ An authenticated user could see the statistics related to a shorturl, for example, the number of clicks for each month in the last year.

Try to answer all the following questions before designing the tables: such questions are conceived to make you reflect on the table design.

**Q1. Which row-key should you use to satisfy point 2.a in the data access patterns described above?**

**Q2. Which row-key should you use to to satisfy point 3 in the data access patterns described above? Can you use the same row-key used in Q.1? In case of positive answer, which command (in the HBase Shell) would you use to answer both questions? In case of a negative answer, how would you solve this problem?**

**Q3. Is point 4 in the data access patterns described above backend-related? Do you think that it should be addressed in the database? How would you address it?**

**Q4. Think about point 5 in the data access patterns described above. Remember the SQL way to retrieve these counters. Is it scalable? Could you improve the scalability using Hadoop MapReduce? How? What are the pros and cons? Is it necessary to save the data related to each click?**

**Q5. How many tables would you need to satisfy all data access patterns described above?**

+ **Q5.1. Using the HBase Shell, create the table needed to save the shorturl (call it shorturl-HOST_NAME)**
+ **Q5.2. Using the HBase Shell, create the table required to save the statistics. Do you need an additional table?**

**Q6. With the created tables, can you satisfy point 3 of the data access patterns defined above? How is that query treated in SQL? Do you need additional tables?**


## Working with Queries
For this laboratory, we created and filled all the tables needed for our example application -- basically we answered all questions above and loaded all data. For the following exercises, students should use the tables we created.

Our schema is reported below, please look at it carefully and try to understand the design logic and the architectural choices we did. In order to be able to continue with the exercises, you need to be familiar with it.

![HBase-Schema](https://raw.github.com/michiard/CLOUDS-LAB/master/hbase-lab/figure/hbase-schema.png)


In the next part, we will translate the SQL queries of the first part of the lab into HBase queries.

### **EXERCISE 4: QUERY 1**
Write the HBase equivalent of the SQL query reported below (See also section “User action: Click on a shorturl”):
    
+ *[Hint]: Use the java program QueryOne.java in your local copy of the hbase-lab repository*


```SQL
SELECT url.url, shorturl.id
FROM shorturl, url
WHERE shorturl.shortId="6PMQ" and shorturl.urlId=url.id;
```

**Q1. How many tables does QUERY 1 involve?**

*Bonus question: write a query to update the statistics corresponding to the accessed shorturl.*

### **EXERCISE 5: QUERY 2**
Write the HBase equivalent the SQL query reported below (see section “Users’ personal pages and shorturl counters”):

```SQL
SELECT shorturl.shortId, url.url, count(click.id)
FROM user INNER JOIN shorturl ON user.id=shorturl.userId
         INNER JOIN url ON url.id=shorturl.urlId
         LEFT JOIN click ON click.shortId=shorturl.id
WHERE user.username="cainarachi"
GROUP by shorturl.id
ORDER BY shorturl.datestamp DESC;
```

**Q1. How many tables does QUERY 2 involve?**

**Q2. How can you have the results ordered by timestamp?**

**Q3. In the SQL query you used different indexes and tables: how are they translated in HBase? Do you need indexes?**

