package io.redit.samples.hdfs12935;

import io.redit.ReditRunner;
import io.redit.exceptions.RuntimeEngineException;
import io.redit.execution.CommandResults;
import io.redit.helpers.HdfsHelper;
import io.redit.helpers.Utils;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

public class SampleTest {
    private static final Logger logger = LoggerFactory.getLogger(SampleTest.class);
    protected static ReditRunner runner;
    private static HdfsHelper helper;

    @BeforeClass
    public static void before() throws RuntimeEngineException, IOException, InterruptedException {
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
    public void testGetAmbiguousResultForDFSAdminCommandInHAMode() throws RuntimeEngineException, InterruptedException, TimeoutException {
        runner.runtime().enforceOrder("X1", () -> {
            runner.runtime().runCommandInNode("nn2", ReditHelper.getHadoopHomeDir() + "/bin/hdfs --daemon stop namenode");
        });
        Thread.sleep(5000);

        runner.runtime().enforceOrder("E1", () -> {
            helper.checkNN(runner,1);
            String command = ReditHelper.getHadoopHomeDir() + "/bin/hdfs dfsadmin -setBalancerBandwidth 12345";
            CommandResults res = runner.runtime().runCommandInNode("nn1", command);
            Utils.printResult(res, logger);
        });

        runner.runtime().enforceOrder("X2", () -> {
            runner.runtime().runCommandInNode("nn2", ReditHelper.getHadoopHomeDir() + "/bin/hdfs --daemon start namenode");
            helper.waitActive();
            helper.transitionToStandby(1, runner);
            helper.transitionToActive(2, runner);
        });

        runner.runtime().enforceOrder("X3", () -> {
            CommandResults res = runner.runtime().runCommandInNode("nn1", ReditHelper.getHadoopHomeDir() + "/bin/hdfs --daemon stop namenode");
            Utils.printResult(res, logger);
        });
        Thread.sleep(5000);

        runner.runtime().enforceOrder("E2", () -> {
            helper.checkNN(runner,2);
            String command = ReditHelper.getHadoopHomeDir() + "/bin/hdfs dfsadmin -setBalancerBandwidth 1234";
            CommandResults res = runner.runtime().runCommandInNode("nn2", command);
            Utils.printResult(res, logger);
        });

        runner.runtime().waitForRunSequenceCompletion(20);
        logger.info("completed !!!");
    }
}