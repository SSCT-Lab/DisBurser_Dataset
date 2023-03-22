package io.redit.samples.kafka13964;

import io.redit.ReditRunner;
import io.redit.exceptions.RuntimeEngineException;
import io.redit.execution.CommandResults;
import io.redit.helpers.KafkaHelper;
import io.redit.helpers.Utils;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.TimeoutException;

public class SampleTest {
    private static final Logger logger = LoggerFactory.getLogger(SampleTest.class);
    protected static ReditRunner runner;
    private static KafkaHelper kafkaHelper;

    @BeforeClass
    public static void before() throws RuntimeEngineException, InterruptedException {
        runner = ReditRunner.run(ReditHelper.getDeployment());
        ReditHelper.startNodes(runner);
        kafkaHelper = new KafkaHelper(runner, ReditHelper.getKafkaHomeDir(), logger, null , ReditHelper.numOfServers, "");

        formatAllStorage();
        Thread.sleep(2000);
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
    public void sampleTest() throws RuntimeEngineException, TimeoutException {
        runner.runtime().enforceOrder("E1", () -> {
            describeTLS(1);
        });

        runner.runtime().waitForRunSequenceCompletion(20);
        logger.info("completed !!!");
    }

    private void describeTLS(int serverId) throws RuntimeEngineException {
        String dockerName = "server" + serverId;
        String command = "cd " + ReditHelper.getKafkaHomeDir() + " && bin/kafka-configs.sh --bootstrap-server " + runner.runtime().ip(dockerName) + ":9092 --describe --user CN=encrypted-arnost";
        CommandResults commandResults = runner.runtime().runCommandInNode(dockerName, command);
        logger.error(commandResults.stdErr());
    }


    private static void formatAllStorage() throws RuntimeEngineException {
        String command = "cd " + ReditHelper.getKafkaHomeDir() + " && bin/kafka-storage.sh random-uuid";
        logger.info("wait for format Storage...");
        CommandResults commandResults = runner.runtime().runCommandInNode("server1", command);
        String uuid = commandResults.stdOut().replaceAll("\n","");
        logger.info("get uuid: " + uuid);
        for(int i = 1; i <= ReditHelper.numOfServers; i++){
            formatStorageLog(i, uuid);
        }
    }

    private static void formatStorageLog(int serverId, String uuid) throws RuntimeEngineException {
        String command = "cd " + ReditHelper.getKafkaHomeDir() + " && bin/kafka-storage.sh format -t " + uuid + " -c  ./config/server.properties";
        CommandResults commandResults = runner.runtime().runCommandInNode("server" + serverId, command);
        Utils.printResult(commandResults, logger);
    }
}
