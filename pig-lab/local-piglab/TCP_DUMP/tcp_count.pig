RAW_LOGS = LOAD './input/tcptest2.txt' AS (line:chararray);

LOGS_BASE = FOREACH RAW_LOGS GENERATE FLATTEN( (tuple(CHARARRAY,CHARARRAY,CHARARRAY,LONG))REGEX_EXTRACT_ALL(line, '(\\d+-\\d+-\\d+).+\\s(\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}\\.\\d{1,5}).+\\s(\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}\\.\\d{1,5}).+length\\s+(\\d+)')) AS (date:chararray, IPS:chararray, IPD:chararray, S:long);

--DUMP LOGS_BASE;

FLOW = GROUP LOGS_BASE BY IPS;
TRAFFIC = FOREACH FLOW GENERATE group, SUM(LOGS_BASE.S) as bytes_v;

DUMP TRAFFIC

