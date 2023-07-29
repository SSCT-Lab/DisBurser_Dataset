package io.redit.samples.cassandra15297;

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
    private static final String test_data_file = "/opt/cassandra/data_file_directories/system_schema/aggregates-924c55872e3a345bb10c12f37c1ba895";

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
        Thread.sleep(5000);
        helper.startServer(2);
        Thread.sleep(10000);
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
    public void testCreateSnapshotWithSpecialCharacter() throws RuntimeEngineException, TimeoutException {
        runner.runtime().enforceOrder("E1", () -> {
            createSnapshot(1);
        });

        runner.runtime().enforceOrder("E2", () -> {
            checkSnapshot(1);
        });

        runner.runtime().waitForRunSequenceCompletion(20);
        logger.info("completed !!!");
    }

    private static void createSnapshot(int serverId) throws RuntimeEngineException {
        logger.info("create snapshot \"p/s\" ...");
        String command1 = "cd " + ReditHelper.getCassandraHomeDir() + " && bin/nodetool snapshot -t \"p/s\"";
        CommandResults commandResult1 = runner.runtime().runCommandInNode("server" + serverId, command1);
        Utils.printResult(commandResult1, logger);
    }

    private static void checkSnapshot(int serverId) throws RuntimeEngineException {
        String command2 = "cd " + ReditHelper.getCassandraHomeDir() + " && bin/nodetool listsnapshots";
        String command3 = "cd " + test_data_file + "/snapshots/p/s" + " && ls -l";
        CommandResults commandResult2 = runner.runtime().runCommandInNode("server" + serverId, command2);
        CommandResults commandResult3 = runner.runtime().runCommandInNode("server" + serverId, command3);
        Utils.printResult(commandResult2, logger);
        Utils.printResult(commandResult3, logger);
    }
}
