# Redit-HDFS-15443

### Details

Title: ***Setting dfs.datanode.max.transfer.threads to a very small value can cause strange failure***

JIRA link：[https://issues.apache.org/jira/browse/HDFS-15443](https://issues.apache.org/jira/browse/HDFS-15443)

|         Label         |  Value   |       Label       |    Value     |
|:---------------------:|:--------:|:-----------------:|:------------:|
|       **Type**        |   Bug    |   **Priority**    |    Major     |
|      **Status**       | RESOLVED |  **Resolution**   |   RESOLVED   |
| **Affects Version/s** |   None   | **Fix Version/s** | 3.3.1, 3.4.0 |

### Description

Configuration parameter dfs.datanode.max.transfer.threads is to specify the maximum number of threads to use for transferring data in and out of the DN. This is a vital param that need to tune carefully.

```
// DataXceiverServer.java
// Make sure the xceiver count is not exceeded
intcurXceiverCount = datanode.getXceiverCount();
if (curXceiverCount > maxXceiverCount) {
thrownewIOException("Xceiver count " + curXceiverCount
+ " exceeds the limit of concurrent xceivers: "
+ maxXceiverCount);
  }
  There are many issues that caused by not setting this param to an appropriate value. However, there is no any check code to restrict the parameter. Although having a hard-and-fast rule is difficult because we need to consider number of cores, main memory etc, we can prevent users from setting this value to an absolute wrong value by accident. (e.g. a negative value that totally break the availability of datanode.)

```

How to fix:

Add proper check code for the parameter.

### Testcase

Reproduced version：3.1.2

Steps to reproduce：
1. Set the dfs.datanode.max.transfer.threads property in hdfs-site.xml:
    ```
    <property>
        <name>dfs.datanode.max.transfer.threads</name>
        <value>-1</value>
    </property>
    ```
2. Start hadoop cluster nodes (including 3 nn, 3 dn and 3 jn).
3. Create a test file directory in hdfs and set EC policy.
4. Use the command "bin/hdfs dfs -put xxx xxx" to put the test file under the directory.
5. This will call the transfer thread to execute, so we found an exception in the datenode log:
    ```
    023-03-20 06:08:53,378 WARN org.apache.hadoop.hdfs.server.datanode.DataNode: dn1:9866:DataXceiverServer: 
    java.io.IOException: Xceiver count 1 exceeds the limit of concurrent xcievers: -1
        at org.apache.hadoop.hdfs.server.datanode.DataXceiverServer.run(DataXceiverServer.java:150)
        at java.lang.Thread.run(Thread.java:748)
    2023-03-20 06:08:53,518 INFO org.apache.hadoop.hdfs.server.datanode.DataNode: Namenode Block pool BP-816139872-10.2.0.2-1679292508807 (Datanode Uuid 6a920ac2-f9bb-4d9b-bc31-1a0e93a69748) service to nn1/10.2.0.2:8020 trying to claim ACTIVE state with txid=10
    2023-03-20 06:08:53,518 INFO org.apache.hadoop.hdfs.server.datanode.DataNode: Acknowledging ACTIVE Namenode Block pool BP-816139872-10.2.0.2-1679292508807 (Datanode Uuid 6a920ac2-f9bb-4d9b-bc31-1a0e93a69748) service to nn1/10.2.0.2:8020
    2023-03-20 06:10:26,383 WARN org.apache.hadoop.hdfs.server.datanode.DataNode: IOException in offerService
    java.io.IOException: DestHost:destPort nn1:8020 , LocalHost:localPort dn1/10.2.0.5:0. Failed on local exception: java.io.IOException: Connection reset by peer
        at sun.reflect.NativeConstructorAccessorImpl.newInstance0(Native Method)
        ...
    ```
