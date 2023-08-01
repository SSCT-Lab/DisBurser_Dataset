# Redit-Rocket-189

### Details

Title: ***Misleading tip on consumeTimestamp and wrong consumeTimestamp exception message***

JIRA link：[https://issues.apache.org/jira/browse/ROCKETMQ-189](https://issues.apache.org/jira/browse/ROCKETMQ-189)

|         Label         |    Value     | Label           |       Value        |
|:---------------------:|:------------:|:---------------:|:------------------:|
|       **Type**        |     Bug      | **Priority**    |       Major        |
|      **Status**       |    CLOSED    | **Resolution**  |       Fixed        |
| **Affects Version/s** | 4.0.0-incubating | **Fix Version/s** | 4.1.0-incubating |

### Description

When I want to consume message,I use the following code:
```
consumer.setConsumeFromWhere(ConsumeFromWhere.CONSUME_FROM_FIRST_OFFSET);
consumer.setConsumeTimestamp("2017_0422_235500");
```

and I got the tip as following:
```
Exception in thread "main" org.apache.rocketmq.client.exception.MQClientException: consumeTimestamp is invalid, YYYY_MMDD_HHMMSS
See http://rocketmq.apache.org/docs/faq/ for further details.
at org.apache.rocketmq.client.impl.consumer.DefaultMQPushConsumerImpl.checkConfig(DefaultMQPushConsumerImpl.java:661)
```

### Testcase

Reproduced version：4.0.0-incubating

Steps to reproduce：
1. Create a DefaultMQPushConsumer object, set the setConsumeFromWhere and setConsumeTimestamp properties.
2. The consumer registers the message listener event and starts. Throw an exception:
```
org.apache.rocketmq.client.exception.MQClientException: consumeTimestamp is invalid, YYYY_MMDD_HHMMSS
See http://rocketmq.apache.org/docs/faq/ for further details.
	at org.apache.rocketmq.client.impl.consumer.DefaultMQPushConsumerImpl.checkConfig(DefaultMQPushConsumerImpl.java:659)
	...
```
