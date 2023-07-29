package io.redit.samples.activemq8050;

import io.redit.ReditRunner;
import io.redit.exceptions.RuntimeEngineException;
import io.redit.helpers.ActivemqHelper;
import org.apache.activemq.ActiveMQMessageConsumer;
import org.apache.activemq.ActiveMQXAConnection;
import org.apache.activemq.ActiveMQXAConnectionFactory;
import org.apache.activemq.transport.failover.FailoverTransport;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jms.*;
import javax.transaction.xa.XAResource;
import javax.transaction.xa.Xid;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.URI;
import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicLong;

import static org.awaitility.Awaitility.await;
public class SampleTest {
    private static final Logger logger = LoggerFactory.getLogger(SampleTest.class);
    protected static ReditRunner runner;
    private static ActivemqHelper helper;
    private static ActiveMQXAConnectionFactory firstFactory;
    private static XAConnection producerConnection;
    private static ActiveMQXAConnection consumerConnection;
    private static XASession consumerSession;
    private static final String queueName = "MY_QUEUE";
    private static String messageId;
    private static Message message;
    private static Xid consumerXid;

    @BeforeClass
    public static void before() throws RuntimeEngineException, InterruptedException {
        runner = ReditRunner.run(ReditHelper.getDeployment());
        ReditHelper.startNodes(runner);
        String[] homeDir = {ReditHelper.getRocketmq1HomeDir(), ReditHelper.getRocketmq2HomeDir(), ReditHelper.getRocketmq3HomeDir()};
        helper = new ActivemqHelper(runner, homeDir, logger, ReditHelper.numOfServers);

        helper.startServers();
        Thread.sleep(10000);
        helper.checkServers();
    }

    @AfterClass
    public static void after() {
        if (runner != null) {
            runner.stop();
        }
    }

    @Test
    public void testFailoverWithExceptionProgrammaticBrokers() throws RuntimeEngineException, InterruptedException, TimeoutException {
        runner.runtime().enforceOrder("E1", () -> {
            String url = String.format("failover:(%s,%s)", helper.ACTIVEMQ_URL1, helper.ACTIVEMQ_URL2);
            firstFactory = new ActiveMQXAConnectionFactory(url);
        });

        runner.runtime().enforceOrder("E2", () -> {
            try {
                producerConnection = firstFactory.createXAConnection();
                producerConnection.setClientID("PRODUCER");
                producerConnection.start();
                XASession producerSession = producerConnection.createXASession();
                Queue producerDestination = producerSession.createQueue(queueName);
                Xid xid = createXid();
                producerSession.getXAResource().start(xid, XAResource.TMNOFLAGS);
                messageId = UUID.randomUUID().toString();
                MessageProducer producer = producerSession.createProducer(producerDestination);
                TextMessage sendMessage = producerSession.createTextMessage("Test message");
                sendMessage.setStringProperty("my_id", messageId);
                producer.send(sendMessage);
                producerSession.getXAResource().end(xid, XAResource.TMSUCCESS);
                producerSession.getXAResource().prepare(xid);
                producerSession.getXAResource().commit(xid, false);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });

        runner.runtime().enforceOrder("E3", () -> {
            try {
                consumerConnection = (ActiveMQXAConnection) firstFactory.createXAConnection();
                consumerConnection.setClientID("CONSUMER");
                consumerConnection.start();
                consumerSession = consumerConnection.createXASession();
                Queue consumerDestination = consumerSession.createQueue(queueName);
                ActiveMQMessageConsumer consumer = (ActiveMQMessageConsumer) consumerSession.createConsumer(consumerDestination);
                consumerXid = createXid();
                consumerSession.getXAResource().start(consumerXid, XAResource.TMNOFLAGS);
                message = getMessage(messageId, consumer);
                consumerSession.getXAResource().end(consumerXid, XAResource.TMSUCCESS);
                consumerSession.getXAResource().prepare(consumerXid);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }

        });

        runner.runtime().enforceOrder("X1", () -> {
            try {
                logger.info("Simulating dropped connection ...");
                FailoverTransport transport = consumerConnection.getTransport().narrow(FailoverTransport.class);
                URI currentTransport = transport.getConnectedTransportURI();
                transport.handleTransportFailure(new IOException("Fake fail"));
                await().atMost(Duration.ofSeconds(10)).until(() -> !Objects.equals(currentTransport, transport.getConnectedTransportURI()) && transport.isConnected());
                Assert.assertTrue(transport.isConnected());
                Assert.assertNotEquals(currentTransport, transport.getConnectedTransportURI());
                message.acknowledge();
                consumerSession.getXAResource().commit(consumerXid, false);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });

        Thread.sleep(2000);
        runner.runtime().enforceOrder("E4", () -> {
            helper.checkServers();
        });

        runner.runtime().waitForRunSequenceCompletion(20);
        logger.info("completed !!!");
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


}
