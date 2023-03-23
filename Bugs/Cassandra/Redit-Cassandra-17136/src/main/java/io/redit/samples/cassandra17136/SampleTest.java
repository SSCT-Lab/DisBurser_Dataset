package io.redit.samples.cassandra17136;

import io.redit.ReditRunner;
import io.redit.exceptions.RuntimeEngineException;
import io.redit.execution.CommandResults;
import io.redit.helpers.CassandraHelper;
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
    private static ReditRunner runner;
    private static CassandraHelper helper;

    @BeforeClass
    public static void before() throws RuntimeEngineException, InterruptedException, IOException {
        runner = ReditRunner.run(ReditHelper.getDeployment());
        ReditHelper.startNodes(runner);
        helper = new CassandraHelper(runner, ReditHelper.getCassandraHomeDir(), logger, ReditHelper.getFileRW(), ReditHelper.numOfServers);
        String seedsIp = runner.runtime().ip("server1");
        helper.addCassandraYamlFile(seedsIp);
        helper.makeCassandraDirs();

        logger.info("wait for Cassandra ...");
        helper.startServer(1);
        helper.startServer(2);
        Thread.sleep(25000);
        helper.checkNetStatus(2);
        helper.checkStatus(1);
    }

    @AfterClass
    public static void after() {
        if (runner != null) {
            runner.stop();
        }
    }

    @Test
    public void testEnablingViaNodetoolCanTriggerDisk_failure_mode() throws RuntimeEngineException, TimeoutException {
        runner.runtime().enforceOrder("E1", () -> {
            String addUserCmd = "useradd cassandra";
            CommandResults addUserRes = runner.runtime().runCommandInNode("server1", addUserCmd);
            Utils.printResult(addUserRes, logger);

            String cmd1 = "mkdir -p /tmp/dir/";
            CommandResults res1 = runner.runtime().runCommandInNode("server1", cmd1);
            Utils.printResult(res1, logger);

            String cmd2 = "chown cassandra:cassandra /tmp/dir";
            CommandResults res2 = runner.runtime().runCommandInNode("server1", cmd2);
            Utils.printResult(res2, logger);
        });

        runner.runtime().enforceOrder("E2", () -> {
            String cmd1 = "mkdir /tmp/dir/sub && touch /tmp/dir/sub/file";
            CommandResults res1 = runner.runtime().runCommandInNode("server1", cmd1);
            Utils.printResult(res1, logger);

            String cmd2 = "ls -la /tmp/dir";
            CommandResults res2 = runner.runtime().runCommandInNode("server1", cmd2);
            Utils.printResult(res2, logger);

            String cmd3 = "ls -la /tmp/dir/sub";
            CommandResults res3 = runner.runtime().runCommandInNode("server1", cmd3);
            Utils.printResult(res3, logger);
        });

        runner.runtime().enforceOrder("E3", () -> {
            String cmd = helper.homeDir + "/bin/nodetool enablefullquerylog --path /tmp/dir ";
            CommandResults res = runner.runtime().runCommandInNode("server1", cmd);
            Utils.printResult(res, logger);
        });

        runner.runtime().waitForRunSequenceCompletion(20);
        logger.info("completed !!!");
    }
}
