package io.redit.samples.kafka12702;

import io.redit.ReditRunner;
import io.redit.exceptions.RuntimeEngineException;
import io.redit.execution.CommandResults;
import io.redit.helpers.KafkaHelper;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.TimeoutException;

public class SampleTest {
    private static final Logger logger = LoggerFactory.getLogger(SampleTest.class);
    protected static ReditRunner runner;
    private static KafkaHelper helper;

    @BeforeClass
    public static void before() throws RuntimeEngineException, InterruptedException {
        runner = ReditRunner.run(ReditHelper.getDeployment());
        ReditHelper.startNodes(runner);
        helper = new KafkaHelper(runner, ReditHelper.getKafkaHomeDir(), logger, null , ReditHelper.numOfServers, "");
    }

    @AfterClass
    public static void after() {
        if (runner != null) {
            runner.stop();
        }
    }

    @Test
    public void testUnhandledExceptionCaught() throws InterruptedException, RuntimeEngineException, TimeoutException {
        logger.info("wait for kafka with kraft...");
        runner.runtime().enforceOrder("t1", () -> {
            formatAllStorage();
        });

        runner.runtime().enforceOrder("t2", () -> {
            helper.startKafkas();
        });

        Thread.sleep(5000);

        runner.runtime().enforceOrder("t3", () -> {
            helper.checkJps();
        });

        runner.runtime().waitForRunSequenceCompletion(20);
        logger.info("completed !!!");
    }

    private void formatAllStorage() throws RuntimeEngineException {
        String command = "cd " + ReditHelper.getKafkaHomeDir() + " && bin/kafka-storage.sh random-uuid";
        logger.info("wait for format Storage...");
        CommandResults commandResults = runner.runtime().runCommandInNode("server1", command);
        String uuid = commandResults.stdOut().replaceAll("\n","");
        logger.info("get uuid: " + uuid);
        for(int i = 1; i <= ReditHelper.numOfServers; i++){
            helper.formatStorageLog(i, uuid);
        }
    }
}
