# Redit-Cassandra-14365

### Details

Title: Commit log replay failure for static columns with collections in clustering keys

|         Label         |                  Value                   |      Label      |     Value      |
|:---------------------:|:----------------------------------------:|:---------------:|:--------------:|
|       **Type**        |                   Bug                    |  **Priority**   |     Normal     |
|      **Status**       |                 RESOLVED                 | **Resolution**  |     Fixed      |
|   **Since Version**   |                  2.1.0                   | **Component/s** | Legacy/Core |

### Description

In the old storage engine, static cells with a collection as part of the clustering key fail to validate because a 0 byte collection (like in the cell name of a static cell) isn't valid.

To reproduce:

1. 
```
CREATE TABLE test.x (
    id int,
    id2 frozen<map<text, text>>,
    st int static,
    PRIMARY KEY (id, id2)
);

INSERT INTO test.x (id, st) VALUES (1, 2);
```

2. Kill the cassandra process

3. Restart cassandra to replay the commitlog

Outcome:
```
ERROR [main] 2018-04-05 04:58:23,741 JVMStabilityInspector.java:99 - Exiting due to error while processing commit log during initialization.
org.apache.cassandra.db.commitlog.CommitLogReplayer$CommitLogReplayException: Unexpected error deserializing mutation; saved to /tmp/mutation3825739904516830950dat.  This may be caused by replaying a mutation against a table with the same name but incompatible schema.  Exception follows: org.apache.cassandra.serializers.MarshalException: Not enough bytes to read a set
        at org.apache.cassandra.db.commitlog.CommitLogReplayer.handleReplayError(CommitLogReplayer.java:638) [main/:na]
        at org.apache.cassandra.db.commitlog.CommitLogReplayer.replayMutation(CommitLogReplayer.java:565) [main/:na]
        at org.apache.cassandra.db.commitlog.CommitLogReplayer.replaySyncSection(CommitLogReplayer.java:517) [main/:na]
        at org.apache.cassandra.db.commitlog.CommitLogReplayer.recover(CommitLogReplayer.java:397) [main/:na]
        at org.apache.cassandra.db.commitlog.CommitLogReplayer.recover(CommitLogReplayer.java:143) [main/:na]
        at org.apache.cassandra.db.commitlog.CommitLog.recover(CommitLog.java:181) [main/:na]
        at org.apache.cassandra.db.commitlog.CommitLog.recover(CommitLog.java:161) [main/:na]
        at org.apache.cassandra.service.CassandraDaemon.setup(CassandraDaemon.java:284) [main/:na]
        at org.apache.cassandra.service.CassandraDaemon.activate(CassandraDaemon.java:533) [main/:na]
        at org.apache.cassandra.service.CassandraDaemon.main(CassandraDaemon.java:642) [main/:na]
```

I haven't investigated if there are other more subtle issues caused by these cells failing to validate other places in the code, but I believe the fix for this is to check for 0 byte length collections and accept them as valid as we do with other types.

I haven't had a chance for any extensive testing but this naive patch seems to have the desired affect.

### Testcase

Start the cluster, create keyspaces and tables, set frozen<map<text, text>> fields in the table, insert data, and the static cells are empty. Rebooting node 1, crashes on startup: JVMStabilityInspector.java:99 - Exiting due to error while processing commit log during initialization.