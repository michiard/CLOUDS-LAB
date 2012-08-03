REGISTER PartitionerSDPair.class;

-- First, we load the raw data from a test dataset
RAW_DATA = LOAD '/home/michiard/work/input/AIRLINE/test.csv' USING PigStorage(',') AS 
	(year: int, month: int, day: int, dow: int, 
	dtime: int, sdtime: int, arrtime: int, satime: int, 
	carrier: chararray, fn: int, tn: chararray, 
	etime: int, setime: int, airtime: int, 
	adelay: int, ddelay: int, 
	scode: chararray, dcode: chararray, dist: int, 
	tintime: int, touttime: int, 
	cancel: chararray, cancelcode: chararray, diverted: int, 
	cdelay: int, wdelay: int, ndelay: int, sdelay: int, latedelay: int);

--------------------------------------------------------------------------------------
-- APPROACH 2:
-- The idea is to build a frequency table for the unordered pair (i,j) where i and j are distinct airport codes
-- However, this time we are interested in the relative frequence of (s,d) pairs over the total outgoing flights from s: that is we want COUNT(s,d)/COUNT(s)

-- IDEA: we wish to use the order inversion design pattern, for which we need to modify the partitioner, and also generate a complex key with an asterisk to have a single job.

-- The alternative is to have 3 jobs:
-- 1 job to compute the denominator
-- 1 job to compute the numberator
-- 1 job to do the join and compute the ratio
---------------------------------------------------------------------------------------

-- project to get rid of unused fields
A = FOREACH RAW_DATA GENERATE scode AS s, dcode AS d;

-- group by (s,d) pair
B = GROUP A by (s,d) PARTITION BY mypartitioners.PartitionerSDPair;

dump B;
describe B;

-- NOTE: for now we only use a custom partitioner, but we still have to implement the order inversion design pattern
-- TO BE COMPLETED

