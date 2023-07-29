package io.redit.samples.rocketmq255;

import io.redit.ReditRunner;
import io.redit.exceptions.RuntimeEngineException;
import io.redit.helpers.RocketmqHelper;
import org.apache.rocketmq.client.consumer.DefaultMQPushConsumer;
import org.apache.rocketmq.client.consumer.listener.ConsumeConcurrentlyStatus;
import org.apache.rocketmq.client.consumer.listener.MessageListenerConcurrently;
import org.apache.rocketmq.client.exception.MQClientException;
import org.apache.rocketmq.common.consumer.ConsumeFromWhere;
import org.apache.rocketmq.common.message.MessageExt;
import org.apache.rocketmq.common.protocol.heartbeat.MessageModel;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.concurrent.TimeoutException;

public class SampleTest {
    private static final Logger logger = LoggerFactory.getLogger(SampleTest.class);
    protected static ReditRunner runner;
    private static RocketmqHelper helper;
    private DefaultMQPushConsumer consumer;

    @BeforeClass
    public static void before() throws RuntimeEngineException, IOException, InterruptedException {
        runner = ReditRunner.run(ReditHelper.getDeployment());
        ReditHelper.startNodes(runner);
        helper = new RocketmqHelper(runner, ReditHelper.getRocketmqHomeDir(), logger, ReditHelper.getFileRW(), ReditHelper.numOfServers);
        helper.addRocketPropFile();
        helper.givePermission();

        helper.startServers();
        Thread.sleep(3000);
        helper.startBroker(1, "a");
        Thread.sleep(3000);
        helper.startBroker(2, "a-s");
        Thread.sleep(3000);
        helper.startBroker(2, "b");
        Thread.sleep(3000);
        helper.startBroker(1, "b-s");
        Thread.sleep(3000);
        helper.checkStatus(1);
    }

    @AfterClass
    public static void after() {
        if (runner != null) {
            runner.stop();
        }
    }

    @Test
    public void testOffsetStoreIsNullAfterConsumerClientsStart() throws RuntimeEngineException, TimeoutException {
        runner.runtime().enforceOrder("E1", () -> {
            consumer = new DefaultMQPushConsumer("consumer_group");
            consumer.setNamesrvAddr(helper.namesrvAddr);
            consumer.setConsumeFromWhere(ConsumeFromWhere.CONSUME_FROM_FIRST_OFFSET);
            consumer.setMessageModel(MessageModel.BROADCASTING);
            consumer.setConsumeMessageBatchMaxSize(10);
            try {
                consumer.subscribe("test_topic", "*");
            } catch (MQClientException e) {
                throw new RuntimeException(e);
            }
        });

        runner.runtime().enforceOrder("E2", () -> {
            consumer.registerMessageListener((MessageListenerConcurrently) (msgs, context) -> {
                StringBuilder sb = new StringBuilder();
                sb.append("Listen Message msgs条数：" + msgs.size());
                MessageExt messageExt = msgs.get(0);
                if (messageExt.getReconsumeTimes() == 3) {
                    return ConsumeConcurrentlyStatus.CONSUME_SUCCESS;
                }

                for (MessageExt msg : msgs) {
                    try {
                        String topic = msg.getTopic();
                        String messageBody = new String(msg.getBody(), "utf-8");
                        String tags = msg.getTags();
                        sb.append(", topic:" + topic + ",tags:" + tags + ",msg:" + messageBody);
                    } catch (Exception e) {
                        e.printStackTrace();
                        return ConsumeConcurrentlyStatus.RECONSUME_LATER;
                    }
                }
                System.out.println(sb.toString());
                return ConsumeConcurrentlyStatus.CONSUME_SUCCESS;
            });
            try {
                consumer.start();
                Assert.assertNotNull(consumer.getOffsetStore());
            } catch (MQClientException e) {
                throw new RuntimeException(e);
            }
        });

        runner.runtime().waitForRunSequenceCompletion(20);
        logger.info("completed !!!");
    }
}
