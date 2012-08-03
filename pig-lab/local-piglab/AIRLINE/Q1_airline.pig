-- First, we load the raw data from a test dataset
RAW_DATA = LOAD '/home/michiard/work/input/AIRLINE/2008.csv' USING PigStorage(',') AS 
	(year: int, month: int, day: int, dow: int, 
	dtime: int, sdtime: int, arrtime: int, satime: int, 
	carrier: chararray, fn: int, tn: chararray, 
	etime: int, setime: int, airtime: int, 
	adelay: int, ddelay: int, 
	scode: chararray, dcode: chararray, dist: int, 
	tintime: int, touttime: int, 
	cancel: chararray, cancelcode: chararray, diverted: int, 
	cdelay: int, wdelay: int, ndelay: int, sdelay: int, latedelay: int);

/*

-------------------------------------------
--  AGGREGATE OUTBOUND TRAFFIC, PER IATA AIRPORT CODE
-------------------------------------------
-- Group by the IATA code of the departure airport
SOURCE_IATA_GROUP = GROUP RAW_DATA BY scode;
-- Count the number of flights out-bound that particular airport
OUTBOUND_IATA_COUNT = FOREACH SOURCE_IATA_GROUP GENERATE group as IATA, COUNT(RAW_DATA) AS num_out_flights;


-------------------------------------------
-- AGGREGATE INBOUND TRAFFIC, PER IATA AIRPORT CODE
-------------------------------------------
-- Group by the IATA code of the destination airport
DEST_IATA_GROUP = GROUP RAW_DATA BY dcode;
-- Count the number of flights in-bound that particular airport
INBOUND_IATA_COUNT = FOREACH DEST_IATA_GROUP GENERATE group as IATA, COUNT(RAW_DATA) AS num_in_flights;


STORE OUTBOUND_IATA_COUNT INTO '/home/michiard/work/input/AIRLINE/output/OUTBOUND.txt' USING PigStorage(',');
STORE INBOUND_IATA_COUNT INTO '/home/michiard/work/input/AIRLINE/output/INBOUND.txt' USING PigStorage(',');
*/



------------------------------------------------------------
-- INBOUND TRAFFIC, PER IATA AIRPORT CODE, PER MONTH, TOP k
------------------------------------------------------------
-- project, to get rid of unused fields: only month and destination ID
INBOUND = FOREACH RAW_DATA GENERATE month AS m, dcode AS d;
-- group by month, then ID (sorted)
GROUP_INBOUND = GROUP INBOUND BY (m,d);
-- aggregate over the group, flatten group, such that output relation has 3 fields
COUNT_INBOUND = FOREACH GROUP_INBOUND GENERATE FLATTEN(group), COUNT(INBOUND) AS count;
-- aggregate over months only
GROUP_COUNT_INBOUND = GROUP COUNT_INBOUND BY m;
-- now apply UDF to compute top k (k=20)
topMonthlyInbound = FOREACH GROUP_COUNT_INBOUND {
    result = TOP(20, 2, COUNT_INBOUND); 
    GENERATE FLATTEN(result);
}

--dump topMonthlyInbound
STORE topMonthlyInbound INTO '/home/michiard/work/input/AIRLINE/output/INBOUND-TOP' USING PigStorage(',');

------------------------------------------------------------
--  OUTBOUND TRAFFIC, PER IATA AIRPORT CODE, PER MONTH, TOP k
------------------------------------------------------------
OUTBOUND = FOREACH RAW_DATA GENERATE month AS m, scode AS s;
GROUP_OUTBOUND = GROUP OUTBOUND BY (m,s);
COUNT_OUTBOUND = FOREACH GROUP_OUTBOUND GENERATE FLATTEN(group), COUNT(OUTBOUND) AS count;
GROUP_COUNT_OUTBOUND = GROUP COUNT_OUTBOUND BY m;
topMonthlyOutbound = FOREACH GROUP_COUNT_OUTBOUND {
    result = TOP(20, 2, COUNT_OUTBOUND); 
    GENERATE FLATTEN(result);
}

--dump topMonthlyOutbound
STORE topMonthlyOutbound INTO '/home/michiard/work/input/AIRLINE/output/OUTBOUND-TOP' USING PigStorage(',');



------------------------------------------------------------
-- TOTAL TRAFFIC, PER IATA AIRPORT CODE, PER MONTH, TOP k
------------------------------------------------------------
UNION_TRAFFIC = UNION COUNT_INBOUND, COUNT_OUTBOUND;
GROUP_UNION_TRAFFIC = GROUP UNION_TRAFFIC BY (m,d);
TOTAL_TRAFFIC = FOREACH GROUP_UNION_TRAFFIC GENERATE FLATTEN(group) AS (m,code), SUM(UNION_TRAFFIC.count) AS total; 
TOTAL_MONTHLY = GROUP TOTAL_TRAFFIC BY m;

topMonthlyTraffic = FOREACH TOTAL_MONTHLY {
    result = TOP(20, 2, TOTAL_TRAFFIC); 
    GENERATE FLATTEN(result) AS (month, iata, traffic);
}


STORE topMonthlyTraffic INTO '/home/michiard/work/input/AIRLINE/output/MONTHLY-TRAFFIC-TOP/' USING PigStorage(',');

explain -brief -dot -out ./ topMonthlyTraffic
