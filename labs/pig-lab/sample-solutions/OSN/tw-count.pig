dataset = LOAD './local-input/OSN/tw.txt' AS (id: long, fr: long);

-- TODO: check if user IDs are valid (e.g. not null) and clean the dataset
--SPLIT ... 

-- TODO: organize data such that each node ID is associated to a list of neighbors
--nodes = ...

-- TODO: foreach node ID generate an output relation consisting of the node ID and the number of "friends"
--friends = ...

STORE friends INTO './local-output/OSN/twc/';
