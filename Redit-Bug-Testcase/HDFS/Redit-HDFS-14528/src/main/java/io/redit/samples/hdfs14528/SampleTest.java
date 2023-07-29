package io.redit.samples.hdfs14528;

import io.redit.ReditRunner;
import io.redit.exceptions.RuntimeEngineException;
import io.redit.helpers.HdfsHelper;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.TimeoutException;

public class SampleTest {
    private static final Logger logger = LoggerFactory.getLogger(SampleTest.class);
    protected static ReditRunner runner;
    private static HdfsHelper helper;
    @BeforeClass
    public static void before() throws RuntimeEngineException, InterruptedException {
        runner = ReditRunner.run(ReditHelper.getDeployment());
        ReditHelper.startNodes(runner);
        helper = new HdfsHelper(runner, ReditHelper.getHadoopHomeDir(), logger, ReditHelper.numOfNNs);

        helper.waitActive();
        logger.info("The cluster is UP!");
        helper.transitionToActive(1, runner);
        helper.checkNNs(runner);
    }

    @AfterClass
    public static void after() {
        if (runner != null) {
            runner.stop();
        }
    }

    @Test
    public void testFailoverFromActiveToStandbyFailed() throws RuntimeEngineException, InterruptedException, TimeoutException {
        runner.runtime().enforceOrder("X1", () -> {
            runner.runtime().killNode("nn3");
        });
        Thread.sleep(5000);

        runner.runtime().enforceOrder("E1", () -> {
            helper.checkNN(runner,1);
            helper.checkNN(runner,2);
        });

        runner.runtime().enforceOrder("E2", () -> {
            helper.failoverNNtoNN(1, 2, runner);
        });

        runner.runtime().waitForRunSequenceCompletion(20);
        logger.info("completed !!!");
    }
}