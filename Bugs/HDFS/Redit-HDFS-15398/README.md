# Redit-HDFS-15398

### Details

Title: ***EC: hdfs client hangs due to exception during addBlock***

JIRA link：[https://issues.apache.org/jira/browse/HDFS-15398](https://issues.apache.org/jira/browse/HDFS-15398)

|         Label         |  Value   |       Label       |    Value     |
|:---------------------:|:--------:|:-----------------:|:------------:|
|       **Type**        |   Bug    |   **Priority**    |   Critical   |
|      **Status**       | RESOLVED |  **Resolution**   |   RESOLVED   |
| **Affects Version/s** |  3.2.0   | **Fix Version/s** | 3.3.1, 3.4.0 |

### Description

In the operation of writing EC files, when the client calls addBlock() applying for the second block group (or >= the second block group) and it happens to exceed quota at this time, the client program will hang forever.
See the demo below:

```
$ hadoop fs -mkdir -p /user/wanghongbing/quota/ec
$ hdfs dfsadmin -setSpaceQuota 2g /user/wanghongbing/quota
$ hdfs ec -setPolicy -path /user/wanghongbing/quota/ec -policy RS-6-3-1024k
Set RS-6-3-1024k erasure coding policy on /user/wanghongbing/quota/ec
$ hadoop fs -put 800m /user/wanghongbing/quota/ec
^@^@^@^@^@^@^@^@^Z
In the case of blocksize=128M, spaceQuota=2g and EC 6-3 policy, a block group needs to apply for 1152M physical space to write 768M logical data. Therefore, writing 800M data will exceed quota when applying for the second block group. At this point, the client will be hang forever.
```

The exception stack of client is as follows:

```
java.lang.Thread.State: TIMED_WAITING (parking)
        at sun.misc.Unsafe.park(Native Method)
        - parking to wait for  <0x000000008009d5d8> (a java.util.concurrent.locks.AbstractQueuedSynchronizer$ConditionObject)
```

When an exception occurs in addBlock, the program will call DFSStripedOutputStream.closeImpl() -> flushBuffer() -> writeChunk() -> allocateNewBlock() -> waitEndBlocks(), waitEndBlocks will enter an infinite loop because the queue in endBlocks is empty.

So I close all stripedDataStreamer to fix it When an exception occurs in addBlock.

### Testcase

Reproduced version：3.2.0

Steps to reproduce：
1. Set the dfs.blockSize property in hdfs-site.xml:
   ```
   <property>
        <name>dfs.blockSize</name>
        <value>1048576</value>
    </property>
   ```
2. Start hadoop cluster nodes (including 3 nn, 3 dn and 3 jn).
3. Create a test file directory in hdfs and set EC policy.
4. Create a file and open an output stream to write 2 block-sized bytes.
5. Set Quota and write 2 block-sized bytes.
6. Close the output stream.

The erasure coding policy in the bug description uses RS-6-3-1024k, but the provided patch file is XOR-2-1-1024k.
I can't understand the function of this patch, because the principles of different EC policies are Different, and the requirements for cluster nodes are also different.
I used three different EC strategies for testing:

1. **XOR-2-1-1024k**, Failed to trigger the exception in the description, the test case passed.
2. **RS-6-3-1024k**, An exception is thrown because the cluster has only 3 datanodes. EC requires that the DataNode in the cluster is at least the same as the EC stripe width (stripe width = number of EC blocks + number of parity check blocks). That is, if we use the RS-6-3 strategy, at least 9 DataNodes are required.
   ```
   16:18:53.471 [main] WARN  o.a.h.i.e.ErasureCodeNative - ISA-L support is not available in your platform... using builtin-java codec where applicable
   org.apache.hadoop.ipc.RemoteException(java.io.IOException): File /test/file could only be written to 3 of the 6 required nodes for RS-6-3-1024k. There are 3 datanode(s) running and 3 node(s) are excluded in this operation.
       at org.apache.hadoop.hdfs.server.blockmanagement.BlockManager.chooseTarget4NewBlock(BlockManager.java:2142)
       ...
   ```
3. **RS-3-2-1024k**, No exception is thrown, but there is a warning log:
   ```
   16:10:16.091 [main] WARN  o.apache.hadoop.hdfs.DFSOutputStream - Cannot allocate parity block(index=3, policy=RS-3-2-1024k). Not enough datanodes? Exclude nodes=[]
   16:10:16.091 [main] WARN  o.apache.hadoop.hdfs.DFSOutputStream - Cannot allocate parity block(index=4, policy=RS-3-2-1024k). Not enough datanodes? Exclude nodes=[]
   16:10:16.673 [main] WARN  o.apache.hadoop.hdfs.DFSOutputStream - Block group <1> failed to write 2 blocks. It's at high risk of losing data.
   ```
   I personally think that an exception should also be thrown here to remind users that there is a lack of data node support, and the EC strategy cannot implement a fault-tolerant mechanism.