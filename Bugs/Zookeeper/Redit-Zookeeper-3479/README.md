# Redit-zookeeper-3479

### Details

Title: Logging false leader election times


|         Label         |        Value        |      Label      |    Value    |
|:---------------------:|:-------------------:|:---------------:|:-----------:|
|       **Type**        |         Bug         |  **Priority**   |    Minor    |
|      **Status**       |      RESOLVED       | **Resolution**  |    Fixed    |
| **Affects Version/s** |     3.5.5           | **Component/s** | leaderElection |

### Description

There seems to be a problem with the logging of leader election times: the logged times are much smaller than the actual time it took for the leader election to complete.

This bug can be easily reproduced by following these steps:

1) Run a ZK cluster of 3 servers

2) Kill the server that is currently the leader

3) The log files of the remaining 2 servers contain false leader election times

```
2019-07-31 00:57:31,852 [myid:1] - WARN [QuorumPeer[myid=1](plain=/0.0.0.0:2791)(secure=disabled):QuorumPeer@1318] - PeerState set to LOOKING
2019-07-31 00:57:31,853 [myid:1] - INFO [QuorumPeer[myid=1](plain=/0.0.0.0:2791)(secure=disabled):QuorumPeer@1193] - LOOKING
2019-07-31 00:57:31,853 [myid:1] - INFO [QuorumPeer[myid=1](plain=/0.0.0.0:2791)(secure=disabled):FastLeaderElection@885] - New election. My id = 1, proposed zxid=0x100000001
[...]
2019-07-31 00:57:32,272 [myid:1] - INFO [QuorumPeer[myid=1](plain=/0.0.0.0:2791)(secure=disabled):Follower@69] - FOLLOWING - LEADER ELECTION TOOK - 1 MS
```

Leader election supposedly took only 1ms, but in reality it took (32,272 - 31,853) = 419ms!

The reason for this bug seems to be the introduction of this line:

```
start_fle = Time.currentElapsedTime();
```

### Testcase

Start the zookeeper cluster of three nodes, and after the election is completed, kill the leader node and make it re-election. In the log, the leader election is said to take only 1 millisecond, but in fact it took tens or even hundreds of milliseconds!