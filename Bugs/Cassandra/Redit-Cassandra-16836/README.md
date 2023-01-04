# Redit-Cassandra-16836

### Details

Title: Materialized views incorrect quoting of UDF

|         Label         |                  Value                   |      Label      |     Value      |
|:---------------------:|:----------------------------------------:|:---------------:|:--------------:|
|       **Type**        |                   Bug                    |  **Priority**   |     Normal     |
|      **Status**       |                 RESOLVED                 | **Resolution**  |     Fixed      |
|   **Since Version**   |                  3.11.x                  | **Component/s** | Feature/Materialized Views |

### Description

Creating a MV with a UDF needing quotes will explode on inserts after restart:

```
create keyspace test WITH replication = {'class':'SimpleStrategy', 'replication_factor' : 3};

use test;

CREATE TABLE t (k int PRIMARY KEY, v int);

CREATE FUNCTION "Double" (input int) 

   CALLED ON NULL INPUT 

   RETURNS int 

   LANGUAGE java 

   AS 'return input*2;';

CREATE MATERIALIZED VIEW mv AS SELECT * FROM t 

   WHERE k < test."Double"(2) 

   AND k IS NOT NULL 

   AND v IS NOT NULL 

   PRIMARY KEY (v, k);
 
```

Now restart the node, run an insert and you get an error:

```
INSERT INTO t(k, v) VALUES (3, 1);
ERROR [MutationStage-2] 2021-08-10 09:55:56,662 StorageProxy.java:1551 - Failed to apply mutation locally : 
org.apache.cassandra.exceptions.InvalidRequestException: Unknown function test.double called
	at org.apache.cassandra.cql3.statements.RequestValidations.invalidRequest(RequestValidations.java:217)
	at org.apache.cassandra.cql3.functions.FunctionCall$Raw.prepare(FunctionCall.java:155)
	at org.apache.cassandra.cql3.SingleColumnRelation.toTerm(SingleColumnRelation.java:123)
	at org.apache.cassandra.cql3.SingleColumnRelation.newSliceRestriction(SingleColumnRelation.java:231)
	at org.apache.cassandra.cql3.Relation.toRestriction(Relation.java:144)
	at org.apache.cassandra.cql3.restrictions.StatementRestrictions.<init>(StatementRestrictions.java:188)
	at org.apache.cassandra.cql3.restrictions.StatementRestrictions.<init>(StatementRestrictions.java:135)
	at org.apache.cassandra.cql3.statements.SelectStatement$RawStatement.prepareRestrictions(SelectStatement.java:1067)
	at org.apache.cassandra.cql3.statements.SelectStatement$RawStatement.prepare(SelectStatement.java:937)
	at org.apache.cassandra.db.view.View.getSelectStatement(View.java:180)
	at org.apache.cassandra.db.view.View.getReadQuery(View.java:204)
	at org.apache.cassandra.db.view.TableViews.updatedViews(TableViews.java:368)
	at org.apache.cassandra.db.view.ViewManager.updatesAffectView(ViewManager.java:85)
	at org.apache.cassandra.db.Keyspace.applyInternal(Keyspace.java:538)
	at org.apache.cassandra.db.Keyspace.apply(Keyspace.java:513)
	at org.apache.cassandra.db.Mutation.apply(Mutation.java:215)
	at org.apache.cassandra.db.Mutation.apply(Mutation.java:220)
	at org.apache.cassandra.db.Mutation.apply(Mutation.java:229)
	at org.apache.cassandra.service.StorageProxy$4.runMayThrow(StorageProxy.java:1545)
	at org.apache.cassandra.service.StorageProxy$LocalMutationRunnable.run(StorageProxy.java:2324)
	at java.util.concurrent.Executors$RunnableAdapter.call(Executors.java:511)
	at org.apache.cassandra.concurrent.AbstractLocalAwareExecutorService$FutureTask.run(AbstractLocalAwareExecutorService.java:162)
	at org.apache.cassandra.concurrent.AbstractLocalAwareExecutorService$LocalSessionFutureTask.run(AbstractLocalAwareExecutorService.java:134)
	at org.apache.cassandra.concurrent.SEPWorker.run(SEPWorker.java:119)
	at io.netty.util.concurrent.FastThreadLocalRunnable.run(FastThreadLocalRunnable.java:30)
	at java.lang.Thread.run(Thread.java:748)
WriteFailure: Error from server: code=1500 [Replica(s) failed to execute write] message="Operation failed - received 0 responses and 1 failures: UNKNOWN from localhost/127.0.0.1:7000" info={'consistency': 'ONE', 'required_responses': 1, 'received_responses': 0, 'failures': 1, 'error_code_map': {'127.0.0.1': '0x0000'}}
```

### Testcase

After starting the cluster, create keyspace and table respectively, create custom method and MATERIALIZED VIEW, then restart node one, restart cassandra service and execute an insert sql, throw exception Cassandra failure during write query at consistency LOCAL_ONE (1 responses were required but only 0 replica responded, 2 failed), the exception information provided by the author is consistent with the system.log