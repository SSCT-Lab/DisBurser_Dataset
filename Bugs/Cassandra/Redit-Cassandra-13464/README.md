# Redit-Cassandra-13464

### Details

Title: Failed to create Materialized view with a specific token range

|         Label         |                  Value                   |      Label      |     Value      |
|:---------------------:|:----------------------------------------:|:---------------:|:--------------:|
|       **Type**        |                   Bug                    |  **Priority**   |      Low       |
|      **Status**       |                 RESOLVED                 | **Resolution**  |     Fixed      |
| **Affects Version/s** |                  3.0.0                   | **Component/s** | Feature/Materialized Views |

### Description

Failed to create Materialized view with a specific token range.
Example :
```
$ ccm create "MaterializedView" -v 3.0.13
$ ccm populate  -n 3
$ ccm start
$ ccm status
Cluster: 'MaterializedView'
---------------------------
node1: UP
node3: UP
node2: UP
$ccm node1 cqlsh
Connected to MaterializedView at 127.0.0.1:9042.
[cqlsh 5.0.1 | Cassandra 3.0.13 | CQL spec 3.4.0 | Native protocol v4]
Use HELP for help.
cqlsh> CREATE KEYSPACE test WITH replication = {'class':'SimpleStrategy', 'replication_factor':3};
cqlsh> CREATE TABLE test.test ( id text PRIMARY KEY , value1 text , value2 text, value3 text);

$ccm node1 ring test 
Datacenter: datacenter1
==========
Address    Rack        Status State   Load            Owns                Token
                                                                          3074457345618258602
127.0.0.1  rack1       Up     Normal  64.86 KB        100.00%             -9223372036854775808
127.0.0.2  rack1       Up     Normal  86.49 KB        100.00%             -3074457345618258603
127.0.0.3  rack1       Up     Normal  89.04 KB        100.00%             3074457345618258602

$ ccm node1 cqlsh
cqlsh> INSERT INTO test.test (id, value1 , value2, value3 ) VALUES ('aaa', 'aaa', 'aaa' ,'aaa');
cqlsh> INSERT INTO test.test (id, value1 , value2, value3 ) VALUES ('bbb', 'bbb', 'bbb' ,'bbb');
cqlsh> SELECT token(id),id,value1 FROM test.test;

 system.token(id)     | id  | value1
----------------------+-----+--------
 -4737872923231490581 | aaa |    aaa
 -3071845237020185195 | bbb |    bbb

(2 rows)

cqlsh> CREATE MATERIALIZED VIEW test.test_view AS SELECT value1, id FROM test.test WHERE id IS NOT NULL AND value1 IS NOT NULL AND TOKEN(id) > -9223372036854775808 AND TOKEN(id) < -3074457345618258603 PRIMARY KEY(value1, id) WITH CLUSTERING ORDER BY (id ASC);
ServerError: java.lang.ClassCastException: org.apache.cassandra.cql3.TokenRelation cannot be cast to org.apache.cassandra.cql3.SingleColumnRelation
```

Stacktrace :
```
INFO  [MigrationStage:1] 2017-04-19 18:32:48,131 ColumnFamilyStore.java:389 - Initializing test.test
WARN  [SharedPool-Worker-1] 2017-04-19 18:44:07,263 FBUtilities.java:337 - Trigger directory doesn't exist, please create it and try again.
ERROR [SharedPool-Worker-1] 2017-04-19 18:46:10,072 QueryMessage.java:128 - Unexpected error during query
java.lang.ClassCastException: org.apache.cassandra.cql3.TokenRelation cannot be cast to org.apache.cassandra.cql3.SingleColumnRelation
	at org.apache.cassandra.db.view.View.relationsToWhereClause(View.java:275) ~[apache-cassandra-3.0.13.jar:3.0.13]
	at org.apache.cassandra.cql3.statements.CreateViewStatement.announceMigration(CreateViewStatement.java:219) ~[apache-cassandra-3.0.13.jar:3.0.13]
	at org.apache.cassandra.cql3.statements.SchemaAlteringStatement.execute(SchemaAlteringStatement.java:93) ~[apache-cassandra-3.0.13.jar:3.0.13]
	at org.apache.cassandra.cql3.QueryProcessor.processStatement(QueryProcessor.java:206) ~[apache-cassandra-3.0.13.jar:3.0.13]
	at org.apache.cassandra.cql3.QueryProcessor.process(QueryProcessor.java:237) ~[apache-cassandra-3.0.13.jar:3.0.13]
	at org.apache.cassandra.cql3.QueryProcessor.process(QueryProcessor.java:222) ~[apache-cassandra-3.0.13.jar:3.0.13]
	at org.apache.cassandra.transport.messages.QueryMessage.execute(QueryMessage.java:115) ~[apache-cassandra-3.0.13.jar:3.0.13]
	at org.apache.cassandra.transport.Message$Dispatcher.channelRead0(Message.java:513) [apache-cassandra-3.0.13.jar:3.0.13]
	at org.apache.cassandra.transport.Message$Dispatcher.channelRead0(Message.java:407) [apache-cassandra-3.0.13.jar:3.0.13]
	at io.netty.channel.SimpleChannelInboundHandler.channelRead(SimpleChannelInboundHandler.java:105) [netty-all-4.0.44.Final.jar:4.0.44.Final]
	at io.netty.channel.AbstractChannelHandlerContext.invokeChannelRead(AbstractChannelHandlerContext.java:357) [netty-all-4.0.44.Final.jar:4.0.44.Final]
	at io.netty.channel.AbstractChannelHandlerContext.access$600(AbstractChannelHandlerContext.java:35) [netty-all-4.0.44.Final.jar:4.0.44.Final]
	at io.netty.channel.AbstractChannelHandlerContext$7.run(AbstractChannelHandlerContext.java:348) [netty-all-4.0.44.Final.jar:4.0.44.Final]
	at java.util.concurrent.Executors$RunnableAdapter.call(Executors.java:511) [na:1.8.0_121]
	at org.apache.cassandra.concurrent.AbstractLocalAwareExecutorService$FutureTask.run(AbstractLocalAwareExecutorService.java:164) [apache-cassandra-3.0.13.jar:3.0.13]
	at org.apache.cassandra.concurrent.SEPWorker.run(SEPWorker.java:105) [apache-cassandra-3.0.13.jar:3.0.13]
	at java.lang.Thread.run(Thread.java:745) [na:1.8.0_121]
ERROR [SharedPool-Worker-1] 2017-04-19 18:46:10,073 ErrorMessage.java:349 - Unexpected exception during request
java.lang.ClassCastException: org.apache.cassandra.cql3.TokenRelation cannot be cast to org.apache.cassandra.cql3.SingleColumnRelation
	at org.apache.cassandra.db.view.View.relationsToWhereClause(View.java:275) ~[apache-cassandra-3.0.13.jar:3.0.13]
	at org.apache.cassandra.cql3.statements.CreateViewStatement.announceMigration(CreateViewStatement.java:219) ~[apache-cassandra-3.0.13.jar:3.0.13]
	at org.apache.cassandra.cql3.statements.SchemaAlteringStatement.execute(SchemaAlteringStatement.java:93) ~[apache-cassandra-3.0.13.jar:3.0.13]
	at org.apache.cassandra.cql3.QueryProcessor.processStatement(QueryProcessor.java:206) ~[apache-cassandra-3.0.13.jar:3.0.13]
	at org.apache.cassandra.cql3.QueryProcessor.process(QueryProcessor.java:237) ~[apache-cassandra-3.0.13.jar:3.0.13]
	at org.apache.cassandra.cql3.QueryProcessor.process(QueryProcessor.java:222) ~[apache-cassandra-3.0.13.jar:3.0.13]
	at org.apache.cassandra.transport.messages.QueryMessage.execute(QueryMessage.java:115) ~[apache-cassandra-3.0.13.jar:3.0.13]
	at org.apache.cassandra.transport.Message$Dispatcher.channelRead0(Message.java:513) [apache-cassandra-3.0.13.jar:3.0.13]
	at org.apache.cassandra.transport.Message$Dispatcher.channelRead0(Message.java:407) [apache-cassandra-3.0.13.jar:3.0.13]
	at io.netty.channel.SimpleChannelInboundHandler.channelRead(SimpleChannelInboundHandler.java:105) [netty-all-4.0.44.Final.jar:4.0.44.Final]
	at io.netty.channel.AbstractChannelHandlerContext.invokeChannelRead(AbstractChannelHandlerContext.java:357) [netty-all-4.0.44.Final.jar:4.0.44.Final]
	at io.netty.channel.AbstractChannelHandlerContext.access$600(AbstractChannelHandlerContext.java:35) [netty-all-4.0.44.Final.jar:4.0.44.Final]
	at io.netty.channel.AbstractChannelHandlerContext$7.run(AbstractChannelHandlerContext.java:348) [netty-all-4.0.44.Final.jar:4.0.44.Final]
	at java.util.concurrent.Executors$RunnableAdapter.call(Executors.java:511) [na:1.8.0_121]
	at org.apache.cassandra.concurrent.AbstractLocalAwareExecutorService$FutureTask.run(AbstractLocalAwareExecutorService.java:164) [apache-cassandra-3.0.13.jar:3.0.13]
	at org.apache.cassandra.concurrent.SEPWorker.run(SEPWorker.java:105) [apache-cassandra-3.0.13.jar:3.0.13]
	at java.lang.Thread.run(Thread.java:745) [na:1.8.0_121]
INFO  [IndexSummaryManager:1] 2017-04-19 19:20:43,246 IndexSummaryRedistribution.java:74 - Redistributing index summaries
```


### Testcase

Start a Cassandra cluster, define a cluster class to connect to the cluster, and get the session object. Create key space, column family and insert test data, query data according to token. Finally, execute "CREATE MATERIALIZED VIEW ..." to throw an exception.