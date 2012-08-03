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
-- APPROACH 1:
-- The idea is to build a frequency table for the unordered pair (i,j) where i and j are distinct airport codes
-- This means we are not interested in any relative counts. In APPROACH 2 we will see how to do this

-- QUESTION: what about the shuffle key space? Is it balanced? How can it be made balanced?
--------------------------------------------------------------------------------------


-- project to get rid of unused fields
A = FOREACH RAW_DATA GENERATE scode AS s, dcode AS d;

-- group by (s,d) pair
B = GROUP A by (s,d);

COUNT = FOREACH B GENERATE group, COUNT(A);

dump COUNT;


