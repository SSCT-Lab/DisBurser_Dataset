# Redit-Kafka-13563

### Details

Title: FindCoordinatorFuture never get cleared in non-group mode( consumer#assign)

|         Label         |        Value        |      Label      |         Value          |
|:---------------------:|:-------------------:|:---------------:|:----------------------:|
|       **Type**        |         Bug         |  **Priority**   |         Major          |
|      **Status**       |      RESOLVED       | **Resolution**  |         Fixed          |
| **Affects Version/s** |   2.7.1, 3.0.0 | **Component/s** |   clients  |

### Description

In KAFKA-10793, we fix the race condition when lookup coordinator by clearing the findCoordinatorFuture when handling the result, rather than in the listener callbacks. It works well under consumer group mode (i.e. Consumer#subscribe), but we found when user is using non consumer group mode (i.e. Consumer#assign) with group id provided (for offset commitment, so that there will be consumerCoordinator created), the findCoordinatorFuture will never be cleared in some situations, and cause the offset committing keeps getting NOT_COORDINATOR error.

 
After KAFKA-10793, we clear the findCoordinatorFuture in 2 places:

1. heartbeat thread
2. AbstractCoordinator#ensureCoordinatorReady

But in non consumer group mode with group id provided, there will be no (1)heartbeat thread , and it only call (2)AbstractCoordinator#ensureCoordinatorReady when 1st time consumer wants to fetch committed offset position. That is, after 2nd lookupCoordinator call, we have no chance to clear the findCoordinatorFuture .
 

To avoid the race condition as KAFKA-10793 mentioned, it's not safe to clear the findCoordinatorFuture in the future listener. So, I think we can fix this issue by calling AbstractCoordinator#ensureCoordinatorReady when coordinator unknown in non consumer group case, under each Consumer#poll.


Reproduce steps:
 
1. Start a 3 Broker cluster with a Topic having Replicas=3.
2. Start a Client with Producer and Consumer (with Consumer#assign(), not subscribe, and provide a group id) communicating over the Topic.
3. Stop the Broker that is acting as the Group Coordinator.
4. Observe successful Rediscovery of new Group Coordinator.
5. Restart the stopped Broker.
6. Stop the Broker that became the new Group Coordinator at step 4.
7. Observe "Rediscovery will be attempted" message but no "Discovered group coordinator" message.


### Testcase

Write the reproduction code according to the reproduction steps given by the author. No exception is thrown in each step. The kafka cluster can re-elect a new coordinator after stopping the broker acting as the coordinator. The launched consumer communication thread can also print the console message of Discovered group coordinator 10.5.0.2:9092 (id: 2147483645 rack: null), contrary to the expectations given by the author. I guess this bug may have been fixed in this version.