/** 
 *	Authors: Enrico Canzonieri <canzonie@eurecom.com>
 *		 Antonio Uccio Verardi <verardia@eurecom.com>
 **/
RAW_DATA = LOAD './local-input/NETWORK_TRAFFIC/100linee.txt'
        AS (ts:long, sport, dport, sip, dip,
                l3proto, l4proto, flags,
                phypkt, netpkt, overhead,
                phybyte, netbyte:long);

DATA = FOREACH RAW_DATA GENERATE (FLOOR(ts/3600)) AS hour,
	sip,
	dip,
	netbyte;

--HOURLY stats

h_group_up = GROUP DATA by (sip,hour);
h_counts_up = FOREACH h_group_up GENERATE group as id_up, SUM(DATA.netbyte) as upload;

h_group_down = GROUP DATA by (dip,hour);
h_counts_down = FOREACH h_group_down GENERATE group as id_down , SUM(DATA.netbyte) as download;

h_counts = JOIN h_counts_up by id_up FULL, h_counts_down by id_down;

-- we compute the next aggregations (DAILY and WEEKLY) from the finest granularity (HOURLY) using roll-up, in this way we don't need to rejoin the relations

h_summary = FOREACH h_counts GENERATE 
	(id_up.sip is null?id_down.dip:id_up.sip) AS IP, (id_up.hour is null?id_down.hour:id_up.hour) AS HOUR,
	(upload is null?0:upload) as upload,
	(download is null?0:download) as download,
	((upload is null?0:upload)+(download is null?0:download)) as total;

h_summary_d = ORDER h_summary BY download DESC;

h_summary_top10_d = LIMIT h_summary_d 10;

h_summary_u = ORDER h_summary BY upload DESC;

h_summary_top10_u = LIMIT h_summary_u 10;

h_summary_t = ORDER h_summary BY total DESC;

h_summary_top10_t = LIMIT h_summary_t 10;

-- DAILY stats from h_summary HOURLY

d_group = GROUP h_summary by (IP,FLOOR(HOUR/24)); 

d_summary = FOREACH d_group GENERATE group.$0 as IP, group.$1 as day, SUM(h_summary.upload) as upload, SUM(h_summary.download) as download, SUM(h_summary.total) as total;


d_summary_d = ORDER d_summary BY download DESC;

d_summary_top10_d = LIMIT d_summary_d 10;

d_summary_u = ORDER d_summary BY upload DESC;

d_summary_top10_u = LIMIT d_summary_u 10;

d_summary_t = ORDER d_summary BY total DESC;

d_summary_top10_t = LIMIT d_summary_t 10;

-- WEEK stats from h_summart DAILY

w_group = GROUP d_summary by (IP,FLOOR(day/7));

w_summary = FOREACH w_group GENERATE group.$0 as IP, group.$1 as week, SUM(d_summary.upload) as upload, SUM(d_summary.download) as download, SUM(d_summary.total) as total;

w_summary_d = ORDER w_summary BY download DESC;

w_summary_top10_d = LIMIT w_summary_d 10;

w_summary_u = ORDER w_summary BY upload DESC;

w_summary_top10_u = LIMIT w_summary_u 10;

w_summary_t = ORDER w_summary BY total DESC;

w_summary_top10_t = LIMIT w_summary_t 10;

STORE h_summary INTO './local-output/NETWORK_TRAFFIC/h_summary';
STORE d_summary INTO './local-output/NETWORK_TRAFFIC/d_summary';
STORE w_summary INTO './local-output/NETWORK_TRAFFIC/w_summary';


-- Top10 download
STORE h_summary_top10_d INTO './local-output/NETWORK_TRAFFIC/h_summary_top10_d';
STORE d_summary_top10_d INTO './local-output/NETWORK_TRAFFIC/d_summary_top10_d';
STORE w_summary_top10_d INTO './local-output/NETWORK_TRAFFIC/w_summary_top10_d';


-- Top10 upload
STORE h_summary_top10_u INTO './local-output/NETWORK_TRAFFIC/h_summary_top10_u';
STORE d_summary_top10_u INTO './local-output/NETWORK_TRAFFIC/d_summary_top10_u';
STORE w_summary_top10_u INTO './local-output/NETWORK_TRAFFIC/w_summary_top10_u';


-- Top10 total
STORE h_summary_top10_t INTO './local-output/NETWORK_TRAFFIC/h_summary_top10_t';
STORE d_summary_top10_t INTO './local-output/NETWORK_TRAFFIC/d_summary_top10_t';
STORE w_summary_top10_t INTO './local-output/NETWORK_TRAFFIC/w_summary_top10_t';
