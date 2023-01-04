# Redit-Rocket-189

### Details
Title: Offset store is null after consumer clients start()

|         Label         |    Value     | Label           |       Value        |
|:---------------------:|:------------:|:---------------:|:------------------:|
|       **Type**        |     Bug      | **Priority**    |       Major        |
|      **Status**       |    CLOSED    | **Resolution**  |       Fixed        |
| **Affects Version/s** | 4.0.0-incubating | **Component/s** |      rocketmq-client       |

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

Start a rocket cluster, add the rocket-client dependency package, create a DefaultMQPushConsumer object, set the setConsumeFromWhere and setConsumeTimestamp properties, and start the Consumer. Throw an exception:
```
org.apache.rocketmq.client.exception.MQClientException: consumeTimestamp is invalid, YYYY_MMDD_HHMMSS
See http://rocketmq.apache.org/docs/faq/ for further details.

	at org.apache.rocketmq.client.impl.consumer.DefaultMQPushConsumerImpl.checkConfig(DefaultMQPushConsumerImpl.java:659)
	at org.apache.rocketmq.client.impl.consumer.DefaultMQPushConsumerImpl.start(DefaultMQPushConsumerImpl.java:545)
	at org.apache.rocketmq.client.consumer.DefaultMQPushConsumer.start(DefaultMQPushConsumer.java:456)
	at io.redit.samples.rocketmq189.SampleTest.startConsumer(SampleTest.java:127)
	at io.redit.samples.rocketmq189.SampleTest.sampleTest(SampleTest.java:60)
	at sun.reflect.NativeMethodAccessorImpl.invoke0(Native Method)
	at sun.reflect.NativeMethodAccessorImpl.invoke(NativeMethodAccessorImpl.java:62)
	at sun.reflect.DelegatingMethodAccessorImpl.invoke(DelegatingMethodAccessorImpl.java:43)
	at java.lang.reflect.Method.invoke(Method.java:498)
	at org.junit.runners.model.FrameworkMethod$1.runReflectiveCall(FrameworkMethod.java:50)
	at org.junit.internal.runners.model.ReflectiveCallable.run(ReflectiveCallable.java:12)
	at org.junit.runners.model.FrameworkMethod.invokeExplosively(FrameworkMethod.java:47)
	at org.junit.internal.runners.statements.InvokeMethod.evaluate(InvokeMethod.java:17)
	at org.junit.runners.ParentRunner.runLeaf(ParentRunner.java:325)
	at org.junit.runners.BlockJUnit4ClassRunner.runChild(BlockJUnit4ClassRunner.java:78)
	at org.junit.runners.BlockJUnit4ClassRunner.runChild(BlockJUnit4ClassRunner.java:57)
	at org.junit.runners.ParentRunner$3.run(ParentRunner.java:290)
	at org.junit.runners.ParentRunner$1.schedule(ParentRunner.java:71)
	at org.junit.runners.ParentRunner.runChildren(ParentRunner.java:288)
	at org.junit.runners.ParentRunner.access$000(ParentRunner.java:58)
	at org.junit.runners.ParentRunner$2.evaluate(ParentRunner.java:268)
	at org.junit.internal.runners.statements.RunBefores.evaluate(RunBefores.java:26)
	at org.junit.internal.runners.statements.RunAfters.evaluate(RunAfters.java:27)
	at org.junit.runners.ParentRunner.run(ParentRunner.java:363)
	at org.junit.runner.JUnitCore.run(JUnitCore.java:137)
	at com.intellij.junit4.JUnit4IdeaTestRunner.startRunnerWithArgs(JUnit4IdeaTestRunner.java:69)
	at com.intellij.rt.junit.IdeaTestRunner$Repeater.startRunnerWithArgs(IdeaTestRunner.java:33)
	at com.intellij.rt.junit.JUnitStarter.prepareStreamsAndStart(JUnitStarter.java:220)
	at com.intellij.rt.junit.JUnitStarter.main(JUnitStarter.java:53)
```