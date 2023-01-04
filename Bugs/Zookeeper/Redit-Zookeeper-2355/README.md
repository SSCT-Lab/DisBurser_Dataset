# Redit-zookeeper-2355

### Details

Title: Ephemeral node is never deleted if follower fails while reading the proposal packet


|         Label         |        Value        |      Label      |    Value    |
|:---------------------:|:-------------------:|:---------------:|:-----------:|
|       **Type**        |         Bug         |  **Priority**   |  Critical   |
|      **Status**       |      RESOLVED       | **Resolution**  |   Fixed    |
| **Affects Version/s** | 3.4.8, 3.4.9, 3.4.10, 3.5.1, 3.5.2, 3.5.3 | **Component/s** | quorum, server |

### Description

ZooKeeper ephemeral node is never deleted if follower fail while reading the proposal packet
The scenario is as follows:

1. Configure three node ZooKeeper cluster, lets say nodes are A, B and C, start all, assume A is leader, B and C are follower
2. Connect to any of the server and create ephemeral node /e1
3. Close the session, ephemeral node /e1 will go for deletion
4. While receiving delete proposal make Follower B to fail with SocketTimeoutException. This we need to do to reproduce the scenario otherwise in production environment it happens because of network fault.
5. Remove the fault, just check that faulted Follower is now connected with quorum
6. Connect to any of the server, create the same ephemeral node /e1, created is success.
7. Close the session, ephemeral node /e1 will go for deletion
8. /e1 is not deleted from the faulted Follower B, It should have been deleted as it was again created with another session
9. /e1 is deleted from Leader A and other Follower C

### Testcase

Start a three-node zookeeper cluster, connect to the cluster and create a temporary node /e1, select a follower node to create a network partition with any other node, and then close the connected client to restore the network. It is found that the temporary node has not been deleted by the leader, and the follower has an additional temporary node /e1. Creating it again will show that the temporary node already exists. Reason: If the follower fails while reading the proposal packet, the ephemeral node will never be deleted.