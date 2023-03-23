# Redit-Cassandra-17136

### Details

Title: ***FQL: Enabling via nodetool can trigger disk_failure_mode***

JIRA link：[https://issues.apache.org/jira/browse/CASSANDRA-17628](https://issues.apache.org/jira/browse/CASSANDRA-17628)

|         Label         |  Value   |       Label       | Value  |
|:---------------------:|:--------:|:-----------------:|:------:|
|       **Type**        |   Bug    |   **Priority**    | Normal |
|      **Status**       | RESOLVED |  **Resolution**   | Fixed  |
| **Affects Version/s** |   None   | **Fix Version/s** | 4.0.2  |

### Description

When enabling fullquerylog via nodetool, if there is a not empty directory present under the location specified via --path which would trigger an java.nio.file.AccessDeniedException during cleaning, the node will trigger the disk_failure_policy which by default is stop. This is a fairly easy way to offline a cluster if someone executes this in parallel. I don't that think the behavior is desirable for enabling via nodetool.

Repro (1 node cluster already up):

```shell
mkdir /some/path/dir
touch /some/path/dir/file
chown -R user: /some/path/dir # Non Cassandra process user
chmod 700 /some/path/dir
nodetool enablefullquerylog --path /some/path
```

Nodetool will give back this error:

```
error: /some/path/dir/file
-- StackTrace --
java.nio.file.AccessDeniedException: /some/path/dir/file
	at sun.nio.fs.UnixException.translateToIOException(UnixException.java:84)
	at sun.nio.fs.UnixException.rethrowAsIOException(UnixException.java:102)
```

On the Cassandra side, we see the following:

```log
INFO  [RMI TCP Connection(2)-10.101.33.87] 2021-11-11 00:55:40,716 BinLog.java:420 - Attempting to configure bin log: Path: /some/path Roll cycle: HOURLY Blocking: true Max queue weight: 268435456 Max log size:17179869184 Archive command:
INFO  [RMI TCP Connection(2)-10.101.33.87] 2021-11-11 00:55:40,720 BinLog.java:433 - Cleaning directory: /some/path as requested
ERROR [RMI TCP Connection(2)-10.101.33.87] 2021-11-11 00:55:40,724 DefaultFSErrorHandler.java:64 - Stopping transports as disk_failure_policy is stop
ERROR [RMI TCP Connection(2)-10.101.33.87] 2021-11-11 00:55:40,725 StorageService.java:453 - Stopping native transport
INFO  [RMI TCP Connection(2)-10.101.33.87] 2021-11-11 00:55:40,730 Server.java:171 - Stop listening for CQL clients
ERROR [RMI TCP Connection(2)-10.101.33.87] 2021-11-11 00:55:40,730 StorageService.java:458 - Stopping gossiper
WARN  [RMI TCP Connection(2)-10.101.33.87] 2021-11-11 00:55:40,731 StorageService.java:357 - Stopping gossip by operator request
INFO  [RMI TCP Connection(2)-10.101.33.87] 2021-11-11 00:55:40,731 Gossiper.java:1984 - Announcing shutdown
```

### Testcase

Reproduced version：4.0.0、4.0.1

Steps to reproduce：
1. Create a test user "cassandra" and a test dir.
2. Give the test directory the permissions of the on Cassandra process user "cassandra".
3. Execute "bin/nodetool enablefullquerylog".

This test case is exactly the same as reproduced in the comments. I run this test case on v4.0.0 and v4.0.1, and the exception does not occur. I guess the bug has been fixed, and I will roll back to the previous version according to the patch for testing.

### Patch 

TODO