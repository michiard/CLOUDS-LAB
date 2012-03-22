#!/usr/bin/python
from org.apache.pig.scripting import *

P = Pig.compile("""
previous_pagerank = 
    LOAD '$docs_in'
    AS ( url: chararray, pagerank: float, links:{ link: ( url: chararray ) } );

outbound_pagerank = 
    FOREACH previous_pagerank 
    GENERATE 
        pagerank / COUNT ( links ) AS pagerank, 
        FLATTEN ( links ) AS to_url; 

new_pagerank = 
    FOREACH 
        ( COGROUP outbound_pagerank BY to_url, previous_pagerank BY url INNER )
    GENERATE 
        group AS url, 
        ( 1 - $d ) + $d * SUM ( outbound_pagerank.pagerank ) AS pagerank, 
        FLATTEN ( previous_pagerank.links ) AS links,
		FLATTEN ( previous_pagerank.pagerank ) AS previous_pagerank;

pagerank_diff = FOREACH new_pagerank GENERATE ABS ( previous_pagerank - pagerank );

max_diff = 
	FOREACH 
		( GROUP pagerank_diff ALL )
	GENERATE
        MAX ( pagerank_diff );

STORE new_pagerank 
    INTO '$docs_out';

STORE max_diff 
    INTO '$max_diff';

""")

d = 0.5
docs_in= "./local-input/pg_simple.txt"

for i in range(10):
	docs_out = "./local-output/pg_v2/pagerank_data_" + str(i + 1)
	max_diff = "./local-output/pg_v2/max_diff_" + str(i + 1)
	Pig.fs("rmr " + docs_out)
	Pig.fs("rmr " + max_diff)
	stats = P.bind().runSingle()
	if not stats.isSuccessful():
		raise 'failed'
	max_diff_value = float(str(stats.result("max_diff").iterator().next().get(0)))
	print "	max_diff_value = " + str(max_diff_value)
	if max_diff_value < 0.01:
		print "done at iteration " + str(i)
		break
	docs_in = docs_out



