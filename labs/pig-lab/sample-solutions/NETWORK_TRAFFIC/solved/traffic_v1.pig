-- This is the raw input data
RAW_DATA = LOAD './input/LADIS11/100linee.txt'
	AS (ts:long, sport, dport, sip, dip, 
		l3proto, l4proto, flags,
		phypkt, netpkt, overhead,
		phybyte, netbyte:long);

-- Prepare the data such that input time stamp can be used accordingly to the queries
OP_DATA = FOREACH RAW_DATA GENERATE 
	(FLOOR(ts/3600)) AS hour, 
	(FLOOR(ts/(3600*24))) AS day, 
	(FLOOR(ts/(3600*24*7))) AS week,
	sip, dip, 
	netbyte;

-- HOURLY stats
h_group_up = GROUP OP_DATA BY (sip,hour);
h_counts_up = FOREACH h_group_up GENERATE group AS id_up, SUM(OP_DATA.netbyte) AS bytes_up;

h_group_down = GROUP OP_DATA BY (dip,hour);
h_counts_down = FOREACH h_group_down GENERATE group AS id_down, SUM(OP_DATA.netbyte) AS bytes_down;

h_counts = JOIN h_counts_up BY id_up.sip, h_counts_down BY id_down.dip;


h_summary = FOREACH h_counts GENERATE 
	id_up.sip AS IP, id_up.hour AS hour, 
	bytes_up AS hbu,
	bytes_down AS hbd,
	(bytes_up+bytes_down) AS hbt;


-- DAILY stats
d_group_up = GROUP OP_DATA BY (sip,day);
d_counts_up = FOREACH d_group_up GENERATE group AS id_up, SUM(OP_DATA.netbyte) AS bytes_up;

d_group_down = GROUP OP_DATA BY (dip,day);
d_counts_down = FOREACH d_group_down GENERATE group AS id_down, SUM(OP_DATA.netbyte) AS bytes_down;

d_counts = JOIN d_counts_up BY id_up.sip, d_counts_down BY id_down.dip;


d_summary = FOREACH d_counts GENERATE
        id_up.sip AS IP, id_up.day AS day,
        bytes_up AS hbu,
        bytes_down AS hbd,
        (bytes_up+bytes_down) AS hbt;



-- WEEKLY stats
w_group_up = GROUP OP_DATA BY (sip,week);
w_counts_up = FOREACH w_group_up GENERATE group AS id_up, SUM(OP_DATA.netbyte) AS bytes_up;

w_group_down = GROUP OP_DATA BY (dip,week);
w_counts_down = FOREACH w_group_down GENERATE group AS id_down, SUM(OP_DATA.netbyte) AS bytes_down;

w_counts = JOIN w_counts_up BY id_up.sip, w_counts_down BY id_down.dip;


w_summary = FOREACH w_counts GENERATE
        id_up.sip AS IP, id_up.week AS week,
        bytes_up AS hbu,
        bytes_down AS hbd,
        (bytes_up+bytes_down) AS hbt;


dump h_summary;
describe h_summary;

dump d_summary;
describe d_summary;

dump w_summary;
describe w_summary;

