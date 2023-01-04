# Redit-ActiveMQ-8050

### Details
Title: XAException when failing over in the middle of a transaction


|         Label         |       Value       |      Label      |        Value        |
|:---------------------:|:-----------------:|:---------------:|:-------------------:|
|       **Type**        |        Bug        |  **Priority**   |        Major        |
|      **Status**       |       OPEN        | **Resolution**  |     Unresolved      |
| **Affects Version/s** | 3.6.3, 3.7, 3.6.4 | **Component/s** | java client, server |

### Description

We have been plagued in production by growing disk usage in KahaDB on our ActiveMQs. We have found that this is caused by hanging transactions, and the only solution so far has been to restart the broker. The hanging transactions happen when we have the occasional network glitch. The networking is out of our control, and not something we can fix.

However, we have found a workaround. Our clients are MDBs in Wildfly. If we disable failover for these, and instead let Wildfly handle creating new connections we don't see the issue.

I have been able to reproduce the error in a unit test. When there is a connection disturbance in the middle of a transaction (on the consumer end) and the client fails over to another broker in the network; it tries to commit the transaction on the new broker.
This fails with

`Transaction 'XID:[...]' has not been started. xaErrorCode:-4`

and the transaction ends up in a weird state on the broker.

We are not using any replicated persistence adapters, just local kahaDB for each broker in the network.

I'm not sure if the error is actually in the client, that can't handle failover during a transaction, or in the broker that doesn't distribute the transaction properly to the other brokers in the network.

I'm also very open to the possibility that this is simply a configuration error on our end, but if so, I have no idea what.

I'm adding the unit test where I have reproduced it. I happily admit that I don't know much about how transactions actually behave in reality, so I might have misconfigured them here, but we see the exact same behaviour in production code where transactions are managed by Wildfly.

```java

import org.apache.activemq.broker.region.policy.PolicyMap;
import org.apache.activemq.transport.failover.FailoverTransport;
import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.DataOutputStream;
import java.io.IOException;
import java.net.URI;
import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicLong;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageProducer;
import javax.jms.Queue;
import javax.jms.TextMessage;
import javax.jms.XAConnection;
import javax.jms.XASession;
import javax.transaction.xa.XAException;
import javax.transaction.xa.XAResource;
import javax.transaction.xa.Xid;

public class FailoverErrorTest {

  private static final Logger logger = LoggerFactory.getLogger(ClusterTest.class);

  private static BrokerService createBroker(boolean deleteAllMessagesOnStartup, String bindAddress) throws Exception {
    BrokerService broker = new BrokerService();
    broker.setUseJmx(true);
    broker.setAdvisorySupport(true);
    TransportConnector transportConnector = new TransportConnector();
    transportConnector.setName("openwire");
    transportConnector.setUri(URI.create(bindAddress));
    transportConnector.setRebalanceClusterClients(true);
    transportConnector.setUpdateClusterClientsOnRemove(true);
    broker.addConnector(transportConnector);
    broker.setDeleteAllMessagesOnStartup(deleteAllMessagesOnStartup);

    PolicyMap policyMap = new PolicyMap();
    PolicyEntry defaultEntry = new PolicyEntry();
    policyMap.setDefaultEntry(defaultEntry);
    broker.setDestinationPolicy(policyMap);

    return broker;
  }

  private static Message getMessage(String messageId, ActiveMQMessageConsumer consumer) throws JMSException {
    String receivedMessageId = null;
    Instant start = Instant.now();
    while (receivedMessageId == null || !Objects.equals(messageId, receivedMessageId)) {
      if (Instant.now().isAfter(start.plus(5, ChronoUnit.SECONDS))) {
        Assert.fail("timeout");
      }
      Message msg = consumer.receive(20000);
      Assert.assertNotNull("Couldn't get message", msg);
      receivedMessageId = msg.getStringProperty("my_id");
      if (!Objects.equals(messageId, receivedMessageId)) {
        logger.info("Got the wrong message. Looping.");
      } else {
        logger.info("Found message");
        return msg;
      }
    }
    return null;
  }

  private static Xid createXid() throws IOException {
    final AtomicLong txGenerator = new AtomicLong(System.currentTimeMillis());

    java.io.ByteArrayOutputStream baos = new java.io.ByteArrayOutputStream();
    DataOutputStream os = new DataOutputStream(baos);
    os.writeLong(txGenerator.incrementAndGet());
    os.close();
    final byte[] bs = baos.toByteArray();

    return new Xid() {
      @Override
      public int getFormatId() {
        return 86;
      }

      @Override
      public byte[] getGlobalTransactionId() {
        return bs;
      }

      @Override
      public byte[] getBranchQualifier() {
        return bs;
      }
    };
  }

  @Test
  public void failoverWithExceptionProgrammaticBrokers() throws Exception {
    BrokerService broker1 = createBroker(true, "tcp://localhost:11001");
    broker1.setBrokerName("broker1");
    BrokerService broker2 = createBroker(true, "tcp://localhost:11002");
    broker2.setBrokerName("broker2");

    XAConnection producerConnection = null;
    ActiveMQXAConnection consumerConnection = null;
    try {
      System.setProperty("org.slf4j.simpleLogger.log." + FailoverTransport.class.getName(), "DEBUG");

      broker1.start();
      broker2.start();
      await().atMost(Duration.ofSeconds(10)).until(() -> broker1.isStarted() && broker2.isStarted());

      broker1.addNetworkConnector("static:(tcp://localhost:11002)");
      broker2.addNetworkConnector("static:(tcp://localhost:11001)");
      broker1.getNetworkConnectors().get(0).start();
      broker2.getNetworkConnectors().get(0).start();

      await().atMost(Duration.ofSeconds(30))
          .until(() -> broker1.getNetworkConnectors().get(0).isStarted() &&
              broker2.getNetworkConnectors().get(0).isStarted());

      String queueName = "MY_QUEUE";

      String url = "failover:(tcp://localhost:11001,tcp://localhost:11002)";
      ActiveMQXAConnectionFactory firstFactory = new ActiveMQXAConnectionFactory(url);
      producerConnection = firstFactory.createXAConnection();
      producerConnection.setClientID("PRODUCER");
      producerConnection.start();
      XASession producerSession = producerConnection.createXASession();
      Queue producerDestination = producerSession.createQueue(queueName);
      Xid xid = createXid();
      producerSession.getXAResource().start(xid, XAResource.TMNOFLAGS);
      String messageId = UUID.randomUUID().toString();
      MessageProducer producer = producerSession.createProducer(producerDestination);
      TextMessage sendMessage = producerSession.createTextMessage("Test message");
      sendMessage.setStringProperty("my_id", messageId);
      producer.send(sendMessage);
      producerSession.getXAResource().end(xid, XAResource.TMSUCCESS);
      producerSession.getXAResource().prepare(xid);
      producerSession.getXAResource().commit(xid, false);

      consumerConnection = (ActiveMQXAConnection) firstFactory.createXAConnection();
      consumerConnection.setClientID("CONSUMER");
      consumerConnection.start();
      XASession consumerSession = consumerConnection.createXASession();
      Queue consumerDestination = consumerSession.createQueue(queueName);
      ActiveMQMessageConsumer consumer = (ActiveMQMessageConsumer) consumerSession.createConsumer(consumerDestination);

      Xid consumerXid = createXid();
      consumerSession.getXAResource().start(consumerXid, XAResource.TMNOFLAGS);
      Message message = getMessage(messageId, consumer);
      consumerSession.getXAResource().end(consumerXid, XAResource.TMSUCCESS);
      consumerSession.getXAResource().prepare(consumerXid);

      logger.info("Simulating dropped connection");
      FailoverTransport transport = consumerConnection.getTransport().narrow(FailoverTransport.class);
      URI currentTransport = transport.getConnectedTransportURI();
      transport.handleTransportFailure(new IOException("Fake fail"));
      await().atMost(Duration.ofSeconds(10)).until(() -> !Objects.equals(currentTransport, transport.getConnectedTransportURI()) && transport.isConnected());
      Assert.assertTrue(transport.isConnected());
      Assert.assertNotEquals(currentTransport, transport.getConnectedTransportURI());
      message.acknowledge();
      consumerSession.getXAResource().commit(consumerXid, false);
    } catch (XAException e) {
      if (e.errorCode == -4) {
        logger.info("Recreated error successfully");
      } else {
        logger.error("Got XAException " + e.errorCode, e);
        Assert.fail();
      }
    } finally {
      producerConnection.close();
      consumerConnection.close();
      broker1.getNetworkConnectors().get(0).stop();
      broker2.getNetworkConnectors().get(0).stop();
      await().atMost(Duration.ofSeconds(5)).until(() -> broker1.getNetworkConnectors().get(0).isStopped() && broker2.getNetworkConnectors().get(0).isStopped());
      broker1.stop();
      broker2.stop();
    }
  }
}

```

pom.xml:

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>


    <groupId>test</groupId>
    <artifactId>cluster-test</artifactId>
    <version>1</version>

    <properties>
        <!--    <version.activemq>5.11.0.redhat-630475</version.activemq>-->
        <version.activemq>5.16.0</version.activemq>
        <maven.compiler.target>1.8</maven.compiler.target>
        <maven.compiler.source>1.8</maven.compiler.source>
    </properties>


    <dependencies>
        <dependency>
            <groupId>junit</groupId>
            <artifactId>junit</artifactId>
            <version>4.13</version>
        </dependency>
        <dependency>
            <groupId>org.apache.activemq</groupId>
            <artifactId>activemq-broker</artifactId>
            <version>${version.activemq}</version>
            <scope>test</scope>
        </dependency>
        <dependency>
            <groupId>org.apache.activemq</groupId>
            <artifactId>activemq-kahadb-store</artifactId>
            <version>${version.activemq}</version>
            <scope>test</scope>
        </dependency>
        <dependency>
            <groupId>org.slf4j</groupId>
            <artifactId>slf4j-simple</artifactId>
            <version>1.7.30</version>
            <scope>test</scope>
        </dependency>
        <dependency>
            <groupId>org.awaitility</groupId>
            <artifactId>awaitility</artifactId>
            <version>4.0.3</version>
            <scope>test</scope>
        </dependency>

    </dependencies>

</project>
```

### Testcase

Start a 2-node activeMQ cluster using Redit, modify urls in the description slightly and run the code, the following exception will be observed in the console which matches the description. 

ActiveMQ version is 5.15.3.

```
javax.jms.JMSException: Transaction 'XID:[86,globalId=001ffffff83645c9e,branchId=001ffffff83645c9e]' has not been started. xaErrorCode:-4
    at org.apache.activemq.util.JMSExceptionSupport.create(JMSExceptionSupport.java:54)
    at org.apache.activemq.ActiveMQConnection.syncSendPacket(ActiveMQConnection.java:1403)
    at org.apache.activemq.ActiveMQConnection.syncSendPacket(ActiveMQConnection.java:1436)
    at org.apache.activemq.TransactionContext.commit(TransactionContext.java:585)
    at io.redit.samples.benchmark.activemq.io.redit.samples.activemq7337.SampleTest.failoverWithExceptionProgrammaticBrokers(io.redit.samples.activemq7337.SampleTest.java:190)
    at io.redit.samples.benchmark.activemq.io.redit.samples.activemq7337.SampleTest.sampleTest(io.redit.samples.activemq7337.SampleTest.java:68)
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
    at com.intellij.rt.junit.IdeaTestRunner$Repeater$1.execute(IdeaTestRunner.java:38)
    at com.intellij.rt.execution.junit.TestsRepeater.repeat(TestsRepeater.java:11)
    at com.intellij.rt.junit.IdeaTestRunner$Repeater.startRunnerWithArgs(IdeaTestRunner.java:35)
    at com.intellij.rt.junit.JUnitStarter.prepareStreamsAndStart(JUnitStarter.java:235)
    at com.intellij.rt.junit.JUnitStarter.main(JUnitStarter.java:54)
Caused by: javax.transaction.xa.XAException: Transaction 'XID:[86,globalId=001ffffff83645c9e,branchId=001ffffff83645c9e]' has not been started. xaErrorCode:-4
    at org.apache.activemq.transaction.Transaction.newXAException(Transaction.java:213)
    at org.apache.activemq.broker.TransactionBroker.getTransaction(TransactionBroker.java:351)
    at org.apache.activemq.broker.TransactionBroker.commitTransaction(TransactionBroker.java:251)
    at org.apache.activemq.broker.BrokerFilter.commitTransaction(BrokerFilter.java:114)
    at org.apache.activemq.broker.TransportConnection.processCommitTransactionTwoPhase(TransportConnection.java:544)
    at org.apache.activemq.command.TransactionInfo.visit(TransactionInfo.java:102)
    at org.apache.activemq.broker.TransportConnection.service(TransportConnection.java:336)
    at org.apache.activemq.broker.TransportConnection$1.onCommand(TransportConnection.java:200)
    at org.apache.activemq.transport.MutexTransport.onCommand(MutexTransport.java:50)
    at org.apache.activemq.transport.WireFormatNegotiator.onCommand(WireFormatNegotiator.java:125)
    at org.apache.activemq.transport.AbstractInactivityMonitor.onCommand(AbstractInactivityMonitor.java:301)
    at org.apache.activemq.transport.TransportSupport.doConsume(TransportSupport.java:83)
    at org.apache.activemq.transport.tcp.TcpTransport.doRun(TcpTransport.java:233)
    at org.apache.activemq.transport.tcp.TcpTransport.run(TcpTransport.java:215)
    at java.lang.Thread.run(Thread.java:748)


``