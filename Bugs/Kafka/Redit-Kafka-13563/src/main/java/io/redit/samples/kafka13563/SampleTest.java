package io.redit.samples.kafka13563;

import io.redit.ReditRunner;
import io.redit.exceptions.RuntimeEngineException;
import io.redit.execution.CommandResults;
import io.redit.helpers.KafkaHelper;
import io.redit.helpers.ZookeeperHelper;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.apache.kafka.common.serialization.VoidDeserializer;
import org.apache.kafka.common.serialization.VoidSerializer;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class SampleTest {
    private static final Logger logger = LoggerFactory.getLogger(SampleTest.class);
    protected static ReditRunner runner;
    private static ZookeeperHelper zookeeperHelper;
    private static KafkaHelper kafkaHelper;
    private static final String TEST_TOPIC = "TEST";
    private static final String GROUP_ID = "TestGroup";
    private static int firstShutdownServerId = -1;
    private static int secondShutdownServerId = -1;

    @BeforeClass
    public static void before() throws RuntimeEngineException, IOException, InterruptedException {
        runner = ReditRunner.run(ReditHelper.getDeployment());
        ReditHelper.startNodes(runner);
        zookeeperHelper = new ZookeeperHelper(runner, ReditHelper.getZookeeperHomeDir(), logger, ReditHelper.getZookeeperFileRW(), ReditHelper.numOfServers);
        zookeeperHelper.addConfFile();
        kafkaHelper = new KafkaHelper(runner, ReditHelper.getKafkaHomeDir(), logger, ReditHelper.getKafkaFileRW(), ReditHelper.numOfServers, "");
        kafkaHelper.addKafkaPropFile();

        zookeeperHelper.startServers();
        Thread.sleep(5000);
        zookeeperHelper.checkServersStatus();
        kafkaHelper.startKafkas();
        Thread.sleep(5000);
        kafkaHelper.checkJps();
    }

    @AfterClass
    public static void after() {
        if (runner != null) {
            runner.stop();
        }
    }

    @Test
    public void testNeverGetClearedInNonGroupMode() throws InterruptedException, RuntimeEngineException, TimeoutException {
        runner.runtime().enforceOrder("E1", () -> {
            kafkaHelper.createTopic(1, TEST_TOPIC);
        });

        runner.runtime().enforceOrder("E2", () -> {
            testConsumerAssign();
        });

        kafkaHelper.findController();
        firstShutdownServerId = kafkaHelper.kafkaControllerId + 1;

        runner.runtime().enforceOrder("E3", () -> {
            kafkaHelper.shutdownBroker(firstShutdownServerId);
        });

        Thread.sleep(5000);
        kafkaHelper.findController();
        secondShutdownServerId = kafkaHelper.kafkaControllerId + 1;

        runner.runtime().enforceOrder("E4", () -> {
            kafkaHelper.startBroker(firstShutdownServerId);
        });

        Thread.sleep(5000);
        kafkaHelper.findController();

        runner.runtime().enforceOrder("E5", () -> {
            kafkaHelper.shutdownBroker(secondShutdownServerId);
        });

        Thread.sleep(10000);
        kafkaHelper.findController();
        runner.runtime().waitForRunSequenceCompletion(20);
        logger.info("completed !!!");
    }

    private void testConsumerAssign(){
        final ScheduledExecutorService executor = Executors.newScheduledThreadPool(2);
        Properties properties1 = new Properties();
        properties1.setProperty(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, kafkaHelper.BOOTSTRAP_SERVERS);
        properties1.setProperty(ConsumerConfig.GROUP_ID_CONFIG, GROUP_ID);
        properties1.setProperty(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "latest");
        final KafkaConsumer<Void, String> consumer = new KafkaConsumer<>(properties1, new VoidDeserializer(), new StringDeserializer());
        consumer.assign(Arrays.asList(new TopicPartition(TEST_TOPIC, 0)));
        executor.execute(() -> {
            while (true) {
                consumer.poll(Duration.ofSeconds(1)).forEach(record -> logger.info("============" + record.toString() + "============" ));
            }
        });

        Properties properties2 = new Properties();
        properties2.setProperty(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, kafkaHelper.BOOTSTRAP_SERVERS);
        properties2.setProperty(ProducerConfig.ENABLE_IDEMPOTENCE_CONFIG, String.valueOf(true));
        final KafkaProducer<Void, String> producer = new KafkaProducer<>(properties2, new VoidSerializer(), new StringSerializer());

        executor.scheduleWithFixedDelay(() -> producer.send(new ProducerRecord<>(TEST_TOPIC, Instant.now().toString())), 1, 1, TimeUnit.SECONDS);
    }
}
