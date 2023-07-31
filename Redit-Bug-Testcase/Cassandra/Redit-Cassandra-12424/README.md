# Redit-Cassandra-12424

### Details

Title: ***Assertion failure in ViewUpdateGenerator***

JIRA link：[https://issues.apache.org/jira/browse/CASSANDRA-12424](https://issues.apache.org/jira/browse/CASSANDRA-12424)

|         Label         |  Value   |       Label       |   Value   |
|:---------------------:|:--------:|:-----------------:|:---------:|
|       **Type**        |   Bug    |   **Priority**    |  Normal   |
|      **Status**       | RESOLVED |  **Resolution**   | Duplicate |
| **Affects Version/s** |   3.7    | **Fix Version/s** |   None    |

### Issue Links

Duplicates: [https://issues.apache.org/jira/browse/CASSANDRA-12247](https://issues.apache.org/jira/browse/CASSANDRA-12247)

### Description

Using released apache-cassandra-3.7.0, we have managed to get a node into a state where it won't start up. The exception is java.lang.AssertionError: We shouldn't have got there is the base row had no associated entry and it appears in ViewUpdateGenerator.computeLivenessInfoForEntry(ViewUpdateGenerator.java:455).

I still have the offending node; what diags/data would be useful for diagnosis? I've attached the full cassandra.log. In summary, cassandra.log contains multiple instances of the following when replaying the commit log on startup, leading ultimately to failure to start up.

```
ERROR 15:24:17 Unknown exception caught while attempting to update MaterializedView! edison.scs_subscriber
java.lang.AssertionError: We shouldn't have got there is the base row had no associated entry
at org.apache.cassandra.db.view.ViewUpdateGenerator.computeLivenessInfoForEntry(ViewUpdateGenerator.java:455) ~[apache-cassandra-3.7.0.jar:3.7.0]
at org.apache.cassandra.db.view.ViewUpdateGenerator.updateEntry(ViewUpdateGenerator.java:273) ~[apache-cassandra-3.7.0.jar:3.7.0]
at org.apache.cassandra.db.view.ViewUpdateGenerator.addBaseTableUpdate(ViewUpdateGenerator.java:127) ~[apache-cassandra-3.7.0.jar:3.7.0]
at org.apache.cassandra.db.view.TableViews.addToViewUpdateGenerators(TableViews.java:403) ~[apache-cassandra-3.7.0.jar:3.7.0]
at org.apache.cassandra.db.view.TableViews.generateViewUpdates(TableViews.java:236) ~[apache-cassandra-3.7.0.jar:3.7.0]
at org.apache.cassandra.db.view.TableViews.pushViewReplicaUpdates(TableViews.java:140) ~[apache-cassandra-3.7.0.jar:3.7.0]
at org.apache.cassandra.db.Keyspace.apply(Keyspace.java:514) [apache-cassandra-3.7.0.jar:3.7.0]
at org.apache.cassandra.db.Keyspace.applyFromCommitLog(Keyspace.java:409) [apache-cassandra-3.7.0.jar:3.7.0]
at org.apache.cassandra.db.commitlog.CommitLogReplayer$MutationInitiator$1.runMayThrow(CommitLogReplayer.java:152) [apache-cassandra-3.7.0.jar:3.7.0]
at org.apache.cassandra.utils.WrappedRunnable.run(WrappedRunnable.java:28) [apache-cassandra-3.7.0.jar:3.7.0]
at java.util.concurrent.Executors$RunnableAdapter.call(Executors.java:511) [na:1.8.0_91]
at org.apache.cassandra.concurrent.AbstractLocalAwareExecutorService$FutureTask.run(AbstractLocalAwareExecutorService.java:164) [apache-cassandra-3.7.0.jar:3.7.0]
at org.apache.cassandra.concurrent.SEPWorker.run(SEPWorker.java:105) [apache-cassandra-3.7.0.jar:3.7.0]
at java.lang.Thread.run(Thread.java:745) [na:1.8.0_91]
WARN  15:24:17 Uncaught exception on thread Thread[SharedPool-Worker-4,5,main]: {}

```

and ultimately

```
ERROR 15:24:18 Exception encountered during startup
java.lang.RuntimeException: java.util.concurrent.ExecutionException: java.lang.AssertionError: We shouldn't have got there is the base row had no associated entry

```

### Testcase

Reproduced version：3.7

Steps to reproduce：
1. Create a client to connect to the cluster and get the session object.
2. Create key space, table and Materialized view.
3. Insert test data, and select shows row as expected.
4. Set last_contact = NULL, which is indexed by MV.
5. Update another field FAILS with message:
    ```
    com.datastax.driver.core.exceptions.WriteTimeoutException: Cassandra timeout during SIMPLE write query at consistency LOCAL_ONE (1 replica were required but only 0 acknowledged the write)
    ```
6. From cassandra log system.log:
    ```
    ERROR [SharedPool-Worker-1] 2023-03-23 09:42:25,780 Keyspace.java:519 - Unknown exception caught while attempting to update MaterializedView! likes.like
        java.lang.AssertionError: We shouldn't have got there is the base row had no associated entry
        at org.apache.cassandra.db.view.ViewUpdateGenerator.computeLivenessInfoForEntry(ViewUpdateGenerator.java:455) ~[apache-cassandra-3.7.jar:3.7]
        ...
    ```
