# Redit-Rocket-255

### Details
Title: Offset store is null after consumer clients start()

|         Label         |    Value     | Label           |       Value        |
|:---------------------:|:------------:|:---------------:|:------------------:|
|       **Type**        |     Bug      | **Priority**    |       Major        |
|      **Status**       |    RESOLVED    | **Resolution**  |       Fixed        |
| **Affects Version/s** | 4.1.0-incubating | **Component/s** |      rocketmq-client       |

### Description

Offset store is null after consumer clients start().

### Testcase

Start a rocket cluster, add the rocket-client dependency package, and create a DefaultMQPushConsumer object. After starting the Consumer, it is found that consumer.getOffsetStore() is null.