# Redit-Rocket-255

### Details
Title: ***Offset store is null after consumer clients start()***

JIRA link：[https://issues.apache.org/jira/browse/ROCKETMQ-255](https://issues.apache.org/jira/browse/ROCKETMQ-255)

|         Label         |    Value     | Label           |       Value        |
|:---------------------:|:------------:|:---------------:|:------------------:|
|       **Type**        |     Bug      | **Priority**    |       Major        |
|      **Status**       |    RESOLVED    | **Resolution**  |       Fixed      |
| **Affects Version/s** | 4.1.0-incubating | **Fix Version/s** |     None     |

### Description

Offset store is null after consumer clients start().

### Testcase

Reproduced version：4.1.0-incubating

Steps to reproduce：

1. Create a DefaultMQPushConsumer object, set the setConsumeFromWhere and setConsumeTimestamp properties.
2. The consumer registers the message listener event and starts.
3. After starting the Consumer, it is found that consumer.getOffsetStore() is null.

