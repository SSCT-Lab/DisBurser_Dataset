# Redit-HDFS-14528

### Details

Title: ***Failover from Active to Standby Failed***

JIRA link：[https://issues.apache.org/jira/browse/HDFS-14528](https://issues.apache.org/jira/browse/HDFS-14528)

|         Label         |      Value      |       Label       |   Value    |
|:---------------------:|:---------------:|:-----------------:|:----------:|
|       **Type**        |       Bug       |   **Priority**    |   Major    |
|      **Status**       | PATCH AVAILABLE |  **Resolution**   | Unresolved |
| **Affects Version/s** |      None       | **Fix Version/s** |    None    |

### Description

n a cluster with more than one Standby namenode, manual failover throws exception for some cases

When trying to exectue the failover command from active to standby

```
./hdfs haadmin  -failover nn1 nn2, below Exception is thrown
```

Operation failed: Call From X-X-X-X/X-X-X-X to Y-Y-Y-Y:nnnn failed on connection exception: java.net.ConnectException: Connection refused

This is encountered in the following cases :

Scenario 1 :

Namenodes - NN1(Active) , NN2(Standby), NN3(Standby)

When trying to manually failover from NN1 to NN2 if NN3 is down, Exception is thrown

Scenario 2 :

Namenodes - NN1(Active) , NN2(Standby), NN3(Standby)

ZKFC's -              ZKFC1,            ZKFC2,            ZKFC3

When trying to manually failover using NN1 to NN3 if NN3's ZKFC (ZKFC3) is down, Exception is thrown

### Testcase

Reproduced version：3.3.4

Steps to reproduce：
1. Kill node nn3 to shut down the namenode nn3.
2. Check the status of the remaining two nodes to ensure that nn1 is active and nn2 is standby.
3. Try to manually failover from NN1 to NN2 and throw an exception:
    ```
    java.lang.RuntimeException: Error while Failover nn1 to nn2
    Illegal argument: failover requires a fencer
	    at io.redit.samples.hdfs16381.SampleTest.failoverNNtoNN(SampleTest.java:64)
        ...
    ```
