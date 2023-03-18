# Redit-Zookeeper-1878

### Details

Title: ***Inconsistent behavior in autocreation of dataDir and dataLogDir***

JIRA link：[https://issues.apache.org/jira/browse/ZOOKEEPER-1878](https://issues.apache.org/jira/browse/ZOOKEEPER-1878)

|         Label         |  Value   |       Label       |    Value     |
|:---------------------:|:--------:|:-----------------:|:------------:|
|       **Type**        |   Bug    |   **Priority**    |    Major     |
|      **Status**       | RESOLVED |  **Resolution**   |    Fixed     |
| **Affects Version/s** |  3.4.5   | **Fix Version/s** | 3.4.7, 3.5.0 |

### Description

During the startup if dataDir is not exists server will auto create this. But when user specifies different dataLogDir location which doesn't exists the server will validate and startup will fail.

```
org.apache.zookeeper.server.quorum.QuorumPeerConfig$ConfigException: Error processing build\test3085582797504170966.junit.dir\zoo.cfg
at org.apache.zookeeper.server.quorum.QuorumPeerConfig.parse(QuorumPeerConfig.java:123)
at org.apache.zookeeper.server.ServerConfig.parse(ServerConfig.java:79)
at org.apache.zookeeper.server.ZooKeeperServerMain.initializeAndRun(ZooKeeperServerMain.java:81)
at org.apache.zookeeper.server.ZooKeeperServerMainTest$MainThread.run(ZooKeeperServerMainTest.java:92)
Caused by: java.lang.IllegalArgumentException: dataLogDir build/test3085582797504170966.junit.dir/data_txnlog is missing.
at org.apache.zookeeper.server.quorum.QuorumPeerConfig.parseProperties(QuorumPeerConfig.java:253)
at org.apache.zookeeper.server.quorum.QuorumPeerConfig.parse(QuorumPeerConfig.java:119)
... 3 more
```

### Testcase

Reproduced version：3.4.5, 3.4.6, 3.4.9, 3.4.10

Steps to reproduce：
1. Start a three-node zookeeper cluster and elect a leader.
2. Create client zk to connect to the zookeeper cluster.
3. Use zk to create a EPHEMERAL node "/test" and check its data.

I did not find the exception in the description on the version 3.4.5, 3.4.6, 3.4.9 and 3.4.10. Instead, I found a startup error where two of the three nodes failed to start properly due to this exception, so the cluster couldn't hold elections, eventually crashing:
```log
2023-03-16 09:08:31,945 [myid:3] - INFO  [main:QuorumPeer@429] - currentEpoch not found! Creating with a reasonable default of 0. This should only happen when you are upgrading your installation
2023-03-16 09:08:31,962 [myid:3] - INFO  [main:QuorumPeer@444] - acceptedEpoch not found! Creating with a reasonable default of 0. This should only happen when you are upgrading your installation
2023-03-16 09:08:31,972 [myid:3] - ERROR [main:QuorumPeer@453] - Unable to load database on disk
java.io.IOException: Could not rename temporary file /zookeeper/zookeeper-3.4.5/zkdata/version-2/currentEpoch.tmp to /zookeeper/zookeeper-3.4.5/zkdata/version-2/currentEpoch
	at org.apache.zookeeper.common.AtomicFileOutputStream.close(AtomicFileOutputStream.java:82)
	at org.apache.zookeeper.server.quorum.QuorumPeer.writeLongToFile(QuorumPeer.java:1117)
	at org.apache.zookeeper.server.quorum.QuorumPeer.loadDataBase(QuorumPeer.java:447)
	at org.apache.zookeeper.server.quorum.QuorumPeer.start(QuorumPeer.java:409)
	at org.apache.zookeeper.server.quorum.QuorumPeerMain.runFromConfig(QuorumPeerMain.java:151)
	at org.apache.zookeeper.server.quorum.QuorumPeerMain.initializeAndRun(QuorumPeerMain.java:111)
	at org.apache.zookeeper.server.quorum.QuorumPeerMain.main(QuorumPeerMain.java:78)
2023-03-16 09:08:31,973 [myid:3] - ERROR [main:QuorumPeerMain@89] - Unexpected exception, exiting abnormally
java.lang.RuntimeException: Unable to run quorum server 
	at org.apache.zookeeper.server.quorum.QuorumPeer.loadDataBase(QuorumPeer.java:454)
	at org.apache.zookeeper.server.quorum.QuorumPeer.start(QuorumPeer.java:409)
	at org.apache.zookeeper.server.quorum.QuorumPeerMain.runFromConfig(QuorumPeerMain.java:151)
	at org.apache.zookeeper.server.quorum.QuorumPeerMain.initializeAndRun(QuorumPeerMain.java:111)
	at org.apache.zookeeper.server.quorum.QuorumPeerMain.main(QuorumPeerMain.java:78)
Caused by: java.io.IOException: Could not rename temporary file /zookeeper/zookeeper-3.4.5/zkdata/version-2/currentEpoch.tmp to /zookeeper/zookeeper-3.4.5/zkdata/version-2/currentEpoch
	at org.apache.zookeeper.common.AtomicFileOutputStream.close(AtomicFileOutputStream.java:82)
	at org.apache.zookeeper.server.quorum.QuorumPeer.writeLongToFile(QuorumPeer.java:1117)
	at org.apache.zookeeper.server.quorum.QuorumPeer.loadDataBase(QuorumPeer.java:447)
	... 4 more
```