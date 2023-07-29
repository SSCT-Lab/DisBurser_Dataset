package io.redit.samples.activemq8252;

import io.redit.ReditRunner;
import io.redit.exceptions.RuntimeEngineException;
import io.redit.helpers.ActivemqHelper;
import org.apache.activemq.transport.stomp.StompConnection;
import org.apache.activemq.transport.stomp.StompFrame;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.Map;

public class SampleTest {
    private static final Logger logger = LoggerFactory.getLogger(SampleTest.class);
    protected static ReditRunner runner;
    private static ActivemqHelper helper;

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
    public void sampleTest() throws Exception {
        runner.runtime().enforceOrder("E1", () -> {
            testStompProducer();
        });

        runner.runtime().enforceOrder("E2", () -> {
            try {
                testStompConsumer();
            } catch (Exception e) {
                e.printStackTrace();
            }
        });

        Thread.sleep(2000);
        runner.runtime().waitForRunSequenceCompletion(20);
        logger.info("completed !!!");
    }

    private void testStompProducer() {
        try {
            // 建立Stomp协议的连接
            StompConnection con = new StompConnection();
            Socket so = new Socket(runner.runtime().ip("server1"), 61613);
            con.open(so);
            // 注意，协议版本可以是1.2，也可以是1.1
            con.setVersion("1.2");
            // 用户名和密码
            con.connect("admin", "admin");
            // 以下发送一条信息（您也可以使用“事务”方式）
            con.send("/test", "生产者发送的消息");
        } catch (Exception e) {
            System.out.println("---------producer exception------------");
        }
    }

    private void testStompConsumer() throws Exception {
        // 建立连接
        StompConnection con = new StompConnection();
        Socket so = new Socket(runner.runtime().ip("server2"), 61613);
        con.open(so);
        con.setVersion("1.2");
        con.connect("admin", "admin");

        String ack = "client";
        con.subscribe("/test", "client");
        // 接受消息（使用循环进行）
        for (int i = 0; i < 4; i++) {
            StompFrame frame = null;
            try {
                // 注意，如果没有接收到消息，
                // 这个消费者线程会停在这里，直到本次等待超时
                frame = con.receive();
            } catch (SocketTimeoutException e) {
                continue;
            }

            // 打印本次接收到的消息
            System.out.println("frame.getAction() = " + frame.getAction());
            Map<String, String> headers = frame.getHeaders();
            String meesage_id = headers.get("message-id");
            System.out.println("frame.getBody() = " + frame.getBody());
            System.out.println("frame.getCommandId() = " + frame.getCommandId());

            // 在ack是client标记的情况下，确认消息
            if ("client".equals(ack)) {
                con.ack(meesage_id);
            }
            Thread.sleep(500);
        }
    }
}
