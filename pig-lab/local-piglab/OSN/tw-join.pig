-- TODO: load the input dataset, located in ./local-input/OSN/tw.txt

-- Set default parallel 
SET default_parallel 20;

datasetA = LOAD '/data/TWITTER/twitter_graph2.txt' AS (id: long, fr: long);

datasetB = LOAD '/data/TWITTER/twitter_graph2.txt' AS (id: long, fr: long);

SPLIT datasetA INTO good_datasetA IF id is not null and fr is not null, bad_datasetA OTHERWISE;

SPLIT datasetB INTO good_datasetB IF id is not null and fr is not null, bad_datasetB OTHERWISE;

-- compute all the two-hop paths 
twohop = JOIN good_datasetA by $1, good_datasetB by $0;

-- project the twohop relation such that in output you display only the start and end nodes of the two hop path
p_result = FOREACH twohop GENERATE $0,$3;

-- Remove duplicates
d_result = DISTINCT p_result;

-- make sure you avoid loops (e.g., if user 12 and 13 follow eachother) 
result = FILTER d_result BY $0!=$1;

STORE result INTO '/output/OSN/twj/';
