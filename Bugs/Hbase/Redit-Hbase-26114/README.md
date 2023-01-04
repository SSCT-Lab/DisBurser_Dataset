# Redit-HBASE-26114

### Details

Title: when “hbase.mob.compaction.threads.max” is set to a negative number, HMaster cannot start normally

|         Label         |        Value        |      Label      |         Value          |
|:---------------------:|:-------------------:|:---------------:|:----------------------:|
|       **Type**        |         Bug         |  **Priority**   |         Minor          |
|      **Status**       |      RESOLVED       | **Resolution**  |         Fixed          |
| **Affects Version/s** | 2.2.0 ~ 2.4.4       | **Component/s** |        master          |

### Description

In hbase-default.xml：
```
<property>     
    <name>hbase.mob.compaction.threads.max</name>     
    <value>1</value>     
    <description>       
      The max number of threads used in MobCompactor.     
    </description>   
</property>
```

When the value is set to a negative number, such as -1, Hmaster cannot start normally.

The log file will output:
```
2021-07-22 18:54:13,758 ERROR [master/JavaFuzz:16000:becomeActiveMaster] master.HMaster: Failed to become active master java.lang.IllegalArgumentException            
    at java.util.concurrent.ThreadPoolExecutor.<init>(ThreadPoolExecutor.java:1314)                      at org.apache.hadoop.hbase.mob.MobUtils.createMobCompactorThreadPool(MobUtils.java:880)
    at org.apache.hadoop.hbase.master.MobCompactionChore.<init>
(MobCompactionChore.java:51)   at org.apache.hadoop.hbase.master.HMaster.initMobCleaner(HMaster.java:1278) 
  at org.apache.hadoop.hbase.master.HMaster.finishActiveMasterInitialization(HMaster.java:1161) 
    at org.apache.hadoop.hbase.master.HMaster.startActiveMasterManager(HMaster.java:2112)
    at org.apache.hadoop.hbase.master.HMaster.lambda$run$0(HMaster.java:580)
    at java.lang.Thread.run(Thread.java:748) 

2021-07-22 18:54:13,760 ERROR [master/JavaFuzz:16000:becomeActiveMaster] master.HMaster: Master server abort: loaded coprocessors are: [org.apache.hadoop.hbase.coprocessor.MultiRowMutationEndpoint] 
2021-07-22 18:54:13,760 ERROR [master/JavaFuzz:16000:becomeActiveMaster] master.HMaster: ***** ABORTING master javafuzz,16000,1626951243154: Unhandled exception. Starting shutdown. ***** java.lang.IllegalArgumentException     
    at java.util.concurrent.ThreadPoolExecutor.<init>(ThreadPoolExecutor.java:1314)   at org.apache.hadoop.hbase.mob.MobUtils.createMobCompactorThreadPool(MobUtils.java:880)     
    at org.apache.hadoop.hbase.master.MobCompactionChore.<init>(MobCompactionChore.java:51) 
  at org.apache.hadoop.hbase.master.HMaster.initMobCleaner(HMaster.java:1278) 
  at org.apache.hadoop.hbase.master.HMaster.finishActiveMasterInitialization(HMaster.java:1161) 
  at org.apache.hadoop.hbase.master.HMaster.startActiveMasterManager(HMaster.java:2112) 
    at org.apache.hadoop.hbase.master.HMaster.lambda$run$0(HMaster.java:580) 
  at java.lang.Thread.run(Thread.java:748) 

2021-07-
22 18:54:13,760 INFO  [master/JavaFuzz:16000:becomeActiveMaster] regionserver.HRegionServer: ***** STOPPING region server 'javafuzz,16000,1626951243154' *****
```

When MOB_COMPACTION_THREADS_MAX is set to 0, mobUtil will set it to 1. But the program does not take into account that it is set to a negative number. When it is set to a negative number, the initialization of the ThreadPoolExecutor will fail and an IllegalArgumentException will be thrown, making HMaster fail to start.

Sometimes users will use -1 as the value of the default item in the configuration file.

Therefore, it is best to modify the source code, when the value is negative, also set its maxThread to 1.

Only need to modify 

    if (maxThreads == 0) {

to 

    if (maxThreads <= 0) {


### Testcase

Configure in hbase-site.xml:
```
<property>
    <name>hbase.mob.compaction.threads.max</name>
    <value>-1</value>
    <description>
      The max number of threads used in MobCompactor.
    </description>
  </property>
```

Start the hdfs cluster, the zookeeper cluster and the hbase cluster in turn, and throw the above exception when the master starts.