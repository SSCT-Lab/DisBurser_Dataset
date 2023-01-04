# Redit-Cassandra-13669

### Details

Title: Error when starting cassandra: Unable to make UUID from 'aa' (SASI index)

|         Label         |                  Value                   |      Label      |     Value      |
|:---------------------:|:----------------------------------------:|:---------------:|:--------------:|
|       **Type**        |                   Bug                    |  **Priority**   |     Urgent     |
|      **Status**       |                 RESOLVED                 | **Resolution**  |     Fixed      |
|   **Since Version**   |                   3.9                    | **Component/s** |  Feature/SASI  |

### Description

Recently I experienced a problem that prevents me to restart cassandra.
I narrowed it down to SASI Index when added on uuid field.

Steps to reproduce:

1. start cassandra (./bin/cassandra -f)

2. create keyspace, table, index and add data:
```
CREATE KEYSPACE testkeyspace
WITH replication = {'class': 'SimpleStrategy', 'replication_factor': '1'} 
           AND durable_writes = true;

use testkeyspace ;

CREATE TABLE testtable (
   col1 uuid,
   col2 uuid,
   ts timeuuid,
   col3 uuid,
   PRIMARY KEY((col1, col2), ts) ) with clustering order by (ts desc);

CREATE CUSTOM INDEX col3_testtable_idx ON testtable(col3)
USING 'org.apache.cassandra.index.sasi.SASIIndex'
WITH OPTIONS = {'analyzer_class': 'org.apache.cassandra.index.sasi.analyzer.StandardAnalyzer', 'mode': 'PREFIX'};

INSERT INTO testtable(col1, col2, ts, col3)
VALUES(898e0014-6161-11e7-b9b7-238ea83bd70b,
               898e0014-6161-11e7-b9b7-238ea83bd70b,
               now(), 898e0014-6161-11e7-b9b7-238ea83bd70b);
```

3. restart cassandra

It crashes with an error (sorry it's huge):
```
DEBUG 09:09:20 Writing Memtable-testtable@1005362073(0.075KiB serialized bytes, 1 ops, 0%/0% of on/off-heap limit), flushed range = (min(-9223372036854775808), max(9223372036854775807)]
ERROR 09:09:20 Exception in thread Thread[PerDiskMemtableFlushWriter_0:1,5,main]
org.apache.cassandra.serializers.MarshalException: Unable to make UUID from 'aa'
	at org.apache.cassandra.db.marshal.UUIDType.fromString(UUIDType.java:118) ~[apache-cassandra-3.9.jar:3.9]
	at org.apache.cassandra.index.sasi.analyzer.StandardAnalyzer.hasNext(StandardAnalyzer.java:168) ~[apache-cassandra-3.9.jar:3.9]
    ......
```

### Testcase

Start the cassandra cluster, create keyspaces, tables, indexes and add data according to the information provided by the author, restart node 1, and restart the service. Crash on startup: 
```
ERROR [PerDiskMemtableFlushWriter_0:1] 2022-09-05 07:38:25,688 CassandraDaemon.java:226 - Exception in thread Thread[PerDiskMemtableFlushWriter_0:1,5,main]
    org.apache.cassandra.serializers.MarshalException: Unable to make UUID from 'aa'
    ......
```