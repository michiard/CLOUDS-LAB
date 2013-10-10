-- This is the raw input data
RAW_DATA = LOAD './input/LADIS11/sample.txt'
	AS (ts:long, sport, dport, sip, dip, 
		l3proto, l4proto, flags,
		phypkt, netpkt, overhead,
		phybyte, netbyte:long);

-- Prepare the data such that input time stamp can be used accordingly to the queries
OP_DATA_UP = FOREACH RAW_DATA GENERATE 
	(FLOOR(ts/3600)) AS hour,
	(FLOOR(ts/(3600*24))) AS day,
        (FLOOR(ts/(3600*24*7))) AS week,
	sip, 
	netbyte;

OP_DATA_DW = FOREACH RAW_DATA GENERATE
	(FLOOR(ts/3600)) AS hour,
	(FLOOR(ts/(3600*24))) AS day,
        (FLOOR(ts/(3600*24*7))) AS week,
        dip,     
        netbyte;


-- Hourly Statistics

GROUPED_DATA_H = COGROUP OP_DATA_UP BY (sip,hour), OP_DATA_DW BY (dip,hour);

TRAFFIC_H = FOREACH GROUPED_DATA_H {
	up_bytes = SUM(OP_DATA_UP.netbyte);
	dw_bytes = SUM(OP_DATA_DW.netbyte);
	total_bytes = (up_bytes is null?0:up_bytes) + (dw_bytes is null?0:dw_bytes);
	GENERATE
		FLATTEN(group) AS (ip,h),
		(up_bytes is null?0:up_bytes) AS upb,
		(dw_bytes is null?0:dw_bytes) AS dwb,
		total_bytes AS ttb;
	};


-- Dayly Statistics
GROUPED_DATA_D = COGROUP OP_DATA_UP BY (sip,day), OP_DATA_DW BY (dip,day);

TRAFFIC_D = FOREACH GROUPED_DATA_D {
        up_bytes = SUM(OP_DATA_UP.netbyte);
        dw_bytes = SUM(OP_DATA_DW.netbyte);
        total_bytes = (up_bytes is null?0:up_bytes) + (dw_bytes is null?0:dw_bytes);
        GENERATE
                FLATTEN(group) AS (ip,d),
                (up_bytes is null?0:up_bytes) AS upb,
                (dw_bytes is null?0:dw_bytes) AS dwb,
                total_bytes AS ttb;
        };


-- Weekly Statistics
GROUPED_DATA_W = COGROUP OP_DATA_UP BY (sip,week), OP_DATA_DW BY (dip,week);

TRAFFIC_W = FOREACH GROUPED_DATA_W {
        up_bytes = SUM(OP_DATA_UP.netbyte);
        dw_bytes = SUM(OP_DATA_DW.netbyte);
        total_bytes = (up_bytes is null?0:up_bytes) + (dw_bytes is null?0:dw_bytes);
        GENERATE
                FLATTEN(group) AS (ip,w),
                (up_bytes is null?0:up_bytes) AS upb,
                (dw_bytes is null?0:dw_bytes) AS dwb,
                total_bytes AS ttb;
        };


dump TRAFFIC_H;
dump TRAFFIC_D;
dump TRAFFIC_W;

