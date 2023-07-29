package io.redit.samples.benchmark.elasticsearch.minimumMasterNodes;
import io.redit.ReditRunner;
import io.redit.exceptions.RuntimeEngineException;
import io.redit.execution.CommandResults;
import io.redit.helpers.ElasticsearchHelper;
import io.redit.helpers.Utils;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.concurrent.TimeoutException;
public class SampleTest {
    private static final Logger logger = LoggerFactory.getLogger(SampleTest.class);
    protected static ReditRunner runner;
    private static ElasticsearchHelper helper;
    private static int connectTimeout = 10;
    private static int dataTransferTimeout = 15;

    @BeforeClass
    public static void before() throws RuntimeEngineException, IOException, InterruptedException {
        runner = ReditRunner.run(ReditHelper.getDeployment());
        ReditHelper.startNodes(runner);
        helper = new ElasticsearchHelper(runner, ReditHelper.getElasticsearchHomeDir(), logger, ReditHelper.getFileRW(), ReditHelper.numOfServers);
        helper.addElasticsearchYmlFile();

        helper.startServers();
        helper.checkJps();
    }
    @AfterClass
    public static void after() {
        if (runner != null) {
            runner.stop();
        }
    }
    @Test
    public void testMinimumMasterNodesMoreThanCurrentNodeCnt() throws InterruptedException, RuntimeEngineException, TimeoutException {
        // 3个节点都正常，尝试创建一个index
        runner.runtime().enforceOrder("E1", () -> {
            logger.info("discovery.zen.minimum_master_nodes:3, master eligible node: 3");
            createAnIndex(1);
        });

        runner.runtime().enforceOrder("X1", () -> {
            runner.runtime().killNode("server2");
        });
        Thread.sleep(3000);

        // 只有2个节点，对其中任意一个轮询状态，status 一直保持为503，且日志中会有 “not enough master nodes”
        runner.runtime().enforceOrder("E2", () -> {
            logger.info("discovery.zen.minimum_master_nodes:3, master eligible node: 2");
            try {
                helper.checkOneServerStatusManyTimes(1);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        });

        runner.runtime().waitForRunSequenceCompletion(20);
        logger.info("completed !!!");
    }

    private void createAnIndex(int serverId) throws RuntimeEngineException {
        String createCommand = String.format("curl --connect-timeout %d -m %d -XPUT 'localhost:9200/customer?pretty'", connectTimeout, dataTransferTimeout);
        String catCommand = String.format("curl --connect-timeout %d -m %d 'localhost:9200/_cat/indices?v'", connectTimeout, dataTransferTimeout);
        CommandResults r1 = runner.runtime().runCommandInNode("server" + serverId, createCommand);
        Utils.printResult(r1, logger);
        CommandResults r2 = runner.runtime().runCommandInNode("server" + serverId, catCommand);
        Utils.printResult(r2, logger);
    }
}
