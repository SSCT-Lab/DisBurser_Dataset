# Redit-zookeeper-4508

### Details

Title: ZooKeeper client run to endless loop in ClientCnxn.SendThread.run if all server down


|         Label         |        Value        |      Label      |    Value    |
|:---------------------:|:-------------------:|:---------------:|:-----------:|
|       **Type**        |         Bug         |  **Priority**   |    Major    |
|      **Status**       |   PATCH AVAILABLE   | **Resolution**  | Unresolved  |
| **Affects Version/s** | 3.6.3, 3.7.0, 3.8.0 | **Component/s** | java client |

### Description

The observable behavior is that client will not get expired event from watcher. The cause if twofold:
1. `updateLastSendAndHeard` is called in reconnection so the session timeout don't decrease.
2. There is not break out from `ClientCnxn.SendThread.run` after session timeout.

### Testcase

Start zookeeper in a three-node cluster. 
Next, create a `CompletableFuture` object which name is `expired` and set it complete when `event.getState()` equals to `Event.KeeperState.Expired`.
Then, kill all zookeeper nodes in the cluster and call `expired.join()`.
After that, zooKeeper client will run to endless loop in `ClientCnxn.SendThread.run`