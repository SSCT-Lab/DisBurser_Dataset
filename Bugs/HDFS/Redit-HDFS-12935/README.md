# Redit-HDFS-12935

### Details

Title: ***Get ambiguous result for DFSAdmin command in HA mode when only one namenode is up***

JIRA link：[https://issues.apache.org/jira/browse/HDFS-13998](https://issues.apache.org/jira/browse/HDFS-13998)

|         Label         |           Value           |       Label       |            Value            |
|:---------------------:|:-------------------------:|:-----------------:|:---------------------------:|
|       **Type**        |            Bug            |   **Priority**    |            Major            |
|      **Status**       |         RESOLVED          |  **Resolution**   |            Fixed            |
| **Affects Version/s** | 2.9.0, 3.0.0-beta1, 3.0.0 | **Fix Version/s** | 3.1.0, 2.10.0, 2.9.1, 3.0.1 |

### Description

In HA mode, if one namenode is down, most of functions can still work. When considering the following two occasions:
(1)nn1 up and nn2 down
(2)nn1 down and nn2 up
These two occasions should be equivalent. However, some of the DFSAdmin commands will have ambiguous results. The commands can be send successfully to the up namenode and are always functionally useful only when nn1 is up regardless of exception (IOException when connecting to the down namenode nn2). If only nn2 is up, the commands have no use at all and only exception to connect nn1 can be found.
See the following command "hdfs dfsadmin setBalancerBandwidth" which aim to set balancer bandwidth value for datanodes as an example. It works and all the datanodes can get the setting values only when nn1 is up. If only nn2 is up, the command throws exception directly and no datanode get the bandwidth setting. Approximately ten DFSAdmin commands use the similar logical process and may be ambiguous.

```
[root@jiangjianfei01 ~]# hdfs haadmin -getServiceState nn1
active
[root@jiangjianfei01 ~]# hdfs dfsadmin -setBalancerBandwidth 12345
Balancer bandwidth is set to 12345 for jiangjianfei01/172.17.0.14:9820
setBalancerBandwidth: Call From jiangjianfei01/172.17.0.14 to jiangjianfei02:9820 failed on connection exception: java.net.ConnectException: Connection refused; For more details see: http://wiki.apache.org/hadoop/ConnectionRefused
[root@jiangjianfei01 ~]# hdfs haadmin -getServiceState nn2
active
[root@jiangjianfei01 ~]# hdfs dfsadmin -setBalancerBandwidth 1234
setBalancerBandwidth: Call From jiangjianfei01/172.17.0.14 to jiangjianfei01:9820 failed on connection exception: java.net.ConnectException: Connection refused; For more details see: http://wiki.apache.org/hadoop/ConnectionRefused
[root@jiangjianfei01 ~]#
```

### Testcase

Reproduced version：3.0.0

Steps to reproduce：
1. Start a hadoop cluster with two namenodes, where nn1 is set to active.
2. Shut down nn2, execute "setBalancerBandwidth" on nn1：
    ```
    17:59:02.932 [main] INFO  i.redit.samples.hdfs13998.SampleTest - Result output:
    Balancer bandwidth is set to 12345 for nn1/10.2.0.2:8020
    
    17:59:02.932 [main] INFO  i.redit.samples.hdfs13998.SampleTest - Result error:
    OpenJDK 64-Bit Server VM warning: You have loaded library /hadoop/hadoop-3.0.0/lib/native/libhadoop.so which might have disabled stack guard. The VM will try to fix the stack guard now.
    It's highly recommended that you fix the library with 'execstack -c <libfile>', or link it with '-z noexecstack'.
    2023-03-21 09:59:02,573 WARN util.NativeCodeLoader: Unable to load native-hadoop library for your platform... using builtin-java classes where applicable
    setBalancerBandwidth: Call From nn1/10.2.0.2 to nn2:8020 failed on connection exception: java.net.ConnectException: Connection refused; For more details see:  http://wiki.apache.org/hadoop/ConnectionRefused
    ```
3. Start nn2, convert nn1 to standby, nn2 to active.
4. Shut down nn1, execute "setBalancerBandwidth" on nn2.
    ```
    17:59:28.132 [main] INFO  i.redit.samples.hdfs13998.SampleTest - Result error:
    OpenJDK 64-Bit Server VM warning: You have loaded library /hadoop/hadoop-3.0.0/lib/native/libhadoop.so which might have disabled stack guard. The VM will try to fix the stack guard now.
    It's highly recommended that you fix the library with 'execstack -c <libfile>', or link it with '-z noexecstack'.
    2023-03-21 09:59:27,803 WARN util.NativeCodeLoader: Unable to load native-hadoop library for your platform... using builtin-java classes where applicable
    setBalancerBandwidth: Call From nn2/10.2.0.3 to nn1:8020 failed on connection exception: java.net.ConnectException: Connection refused; For more details see:  http://wiki.apache.org/hadoop/ConnectionRefused
    ```
   
### Patch 

TODO