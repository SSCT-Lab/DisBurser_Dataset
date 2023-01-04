package io.redit.samples.activemq7337;

import io.redit.ReditRunner;
import io.redit.exceptions.RuntimeEngineException;
import io.redit.helpers.ActivemqHelper;
import org.apache.activemq.ActiveMQConnectionFactory;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jms.*;
import java.util.concurrent.atomic.AtomicBoolean;

public class SampleTest {
    private static final Logger logger = LoggerFactory.getLogger(SampleTest.class);
    protected static ReditRunner runner;
    private static ActivemqHelper helper;
    private static Connection connection;
    private static final int messagesToSend = 20;
    private static Integer biggerCacheSize = null;
    private static Thread consumerThread = null;
    private static AtomicBoolean messageReceived = null;

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
    public static void after() throws JMSException {
        if (runner != null) {
            connection.stop();
            connection.close();
            runner.stop();
        }
    }

    @Test
    public void testPrefetchZeroConsumerReconnectionAfterFailoverWithMessageSent() throws Exception {
        runner.runtime().enforceOrder("E1", () -> {
            String url = String.format("failover:(%s,%s)?jms.prefetchPolicy.all=0", helper.ACTIVEMQ_URL1, helper.ACTIVEMQ_URL2);
            if (biggerCacheSize != null) {
                url += "&maxCacheSize=" + biggerCacheSize;
            }
            ConnectionFactory factory = new ActiveMQConnectionFactory(url);
            try {
                connection = factory.createConnection();
                connection.start();
            } catch (JMSException e) {
                throw new RuntimeException(e);
            }
        });

        runner.runtime().enforceOrder("E2", () -> {
            messageReceived = new AtomicBoolean(false);
            consumerThread = new Thread(() -> {
                try {
                    receiveMessageOnConnection(connection, "mainQueue");
                    messageReceived.set(true);
                } catch (JMSException e) {
                    if (e.getCause() instanceof InterruptedException) {
                        System.out.println("Interrupted because receive blocked");
                    } else {
                        e.printStackTrace();
                    }
                }
            });
            consumerThread.start();
        });

        // 我们实际上不需要在这里发送/接收消息，只需要打开一个执行MessagePull的消费者就足够（接收超时或接收超时为零）
        runner.runtime().enforceOrder("E3", () -> {
            for (int i = 0; i < messagesToSend; i++) {
                try {
                    sendMessageOnConnection(connection, "otherQueue", "testing " + i);
                    receiveMessageOnConnection(connection, "otherQueue");
                } catch (JMSException e) {
                    throw new RuntimeException(e);
                }
            }
        });

        runner.runtime().enforceOrder("X1", () -> {
            runner.runtime().restartNode("server1", 30);
            runner.runtime().restartNode("server2", 30);
            try {
                Thread.sleep(2000);
                helper.startServers();
                Thread.sleep(5000);
                helper.checkServers();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });

        runner.runtime().enforceOrder("E4", () -> {
            try {
                sendMessageOnConnection(connection,"mainQueue", "Main Message");
                consumerThread.join(5000);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
            if (messageReceived.get()) {
                System.out.println("messageReceived.get(): " + true);
            } else {
                System.out.println("messageReceived.get(): " + false);
                consumerThread.interrupt();
            }
        });

        runner.runtime().waitForRunSequenceCompletion(20);
        logger.info("completed !!!");
    }

    private static void receiveMessageOnConnection(Connection c, String queueName) throws JMSException {
        Session session = c.createSession(true, Session.CLIENT_ACKNOWLEDGE);
        Queue queue = session.createQueue(queueName);
        MessageConsumer consumer = session.createConsumer(queue);
        consumer.receive(0);
        session.commit();
        consumer.close();
        session.close();
    }

    private static void sendMessageOnConnection(Connection c, String queueName, String txt) throws JMSException {
        Session session = c.createSession(true, Session.CLIENT_ACKNOWLEDGE);
        Queue q = session.createQueue(queueName);
        MessageProducer producer = session.createProducer(q);
        TextMessage message = session.createTextMessage(txt);
        message.setJMSDeliveryMode(DeliveryMode.PERSISTENT);
        producer.send(message);
        session.commit();
        producer.close();
        session.close();
    }
}
