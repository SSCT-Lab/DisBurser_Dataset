# Redit-Zookeeper-1367

### Details

Title: ***Data inconsistencies and unexpired ephemeral nodes after cluster restart***

JIRA link：[https://issues.apache.org/jira/browse/ZOOKEEPER-1367](https://issues.apache.org/jira/browse/ZOOKEEPER-1367)

|         Label         |  Value   |       Label       |        Value        |
|:---------------------:|:--------:|:-----------------:|:-------------------:|
|       **Type**        |   Bug    |   **Priority**    |       Blocker       |
|      **Status**       | RESOLVED |  **Resolution**   |        Fixed        |
| **Affects Version/s** |  3.4.2   | **Fix Version/s** | 3.4.3, 3.3.5, 3.5.0 |

### Description

In one of our tests, we have a cluster of three ZooKeeper servers. We kill all three, and then restart just two of them. Sometimes we notice that on one of the restarted servers, ephemeral nodes from previous sessions do not get deleted, while on the other server they do. We are effectively running 3.4.2, though technically we are running 3.4.1 with the patch manually applied for ZOOKEEPER-1333 and a C client for 3.4.1 with the patches for ZOOKEEPER-1163.

I noticed that when I connected using zkCli.sh to the first node (90.0.0.221, zkid 84), I saw only one znode in a particular path:

```
[zk: 90.0.0.221:2888(CONNECTED) 0] ls /election/zkrsm
[nominee0000000011]
[zk: 90.0.0.221:2888(CONNECTED) 1] get /election/zkrsm/nominee0000000011
90.0.0.222:7777
cZxid = 0x400000027
ctime = Thu Jan 19 08:18:24 UTC 2012
mZxid = 0x400000027
mtime = Thu Jan 19 08:18:24 UTC 2012
pZxid = 0x400000027
cversion = 0
dataVersion = 0
aclVersion = 0
ephemeralOwner = 0xa234f4f3bc220001
dataLength = 16
numChildren = 0
```

However, when I connect zkCli.sh to the second server (90.0.0.222, zkid 251), I saw three znodes under that same path:

```
[zk: 90.0.0.222:2888(CONNECTED) 2] ls /election/zkrsm
nominee0000000006 nominee0000000010 nominee0000000011
[zk: 90.0.0.222:2888(CONNECTED) 2] get /election/zkrsm/nominee0000000011
90.0.0.222:7777
cZxid = 0x400000027
ctime = Thu Jan 19 08:18:24 UTC 2012
mZxid = 0x400000027
mtime = Thu Jan 19 08:18:24 UTC 2012
pZxid = 0x400000027
cversion = 0
dataVersion = 0
aclVersion = 0
ephemeralOwner = 0xa234f4f3bc220001
dataLength = 16
numChildren = 0
[zk: 90.0.0.222:2888(CONNECTED) 3] get /election/zkrsm/nominee0000000010
90.0.0.221:7777
cZxid = 0x30000014c
ctime = Thu Jan 19 07:53:42 UTC 2012
mZxid = 0x30000014c
mtime = Thu Jan 19 07:53:42 UTC 2012
pZxid = 0x30000014c
cversion = 0
dataVersion = 0
aclVersion = 0
ephemeralOwner = 0xa234f4f3bc220000
dataLength = 16
numChildren = 0
[zk: 90.0.0.222:2888(CONNECTED) 4] get /election/zkrsm/nominee0000000006
90.0.0.223:7777
cZxid = 0x200000cab
ctime = Thu Jan 19 08:00:30 UTC 2012
mZxid = 0x200000cab
mtime = Thu Jan 19 08:00:30 UTC 2012
pZxid = 0x200000cab
cversion = 0
dataVersion = 0
aclVersion = 0
ephemeralOwner = 0x5434f5074e040002
dataLength = 16
numChildren = 0
```

These never went away for the lifetime of the server, for any clients connected directly to that server. Note that this cluster is configured to have all three servers still, the third one being down (90.0.0.223, zkid 162).

I captured the data/snapshot directories for the two live servers. When I start single-node servers using each directory, I can briefly see that the inconsistent data is present in those logs, though the ephemeral nodes seem to get (correctly) cleaned up pretty soon after I start the server.

I will upload a tar containing the debug logs and data directories from the failure. I think we can reproduce it regularly if you need more info.

### Testcase

Reproduced version：3.4.3、3.6.0

Steps to reproduce：
1. Start a three-node zookeeper cluster and elect a leader.
2. Create client zk1 to connect to the zookeeper cluster.
3. Use zk1 to create a PERSISTENT node "/test" and three children EPHEMERAL nodes.
4. Kill all 3 nodes and restart both.
5. After waiting for its election to be successful, create clients to connect to the two nodes respectively.
6. Query the PERSISTENT node status and child EPHEMERAL node data.
7. I found that the EPHEMERAL nodes was not deleted, and its data was intact and consistent.
8. The reason should be that the session created between the client and the zk cluster was not deleted due to the cluster crash. Even if the session times out, the client will continue to request the cluster, which is consistent with the bug [ZOOKEEPER-4508](https://issues.apache.org/jira/browse/ZOOKEEPER-4508).
