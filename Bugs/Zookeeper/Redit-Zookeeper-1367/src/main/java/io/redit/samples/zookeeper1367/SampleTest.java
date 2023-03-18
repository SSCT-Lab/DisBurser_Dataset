package io.redit.samples.zookeeper1367;

import io.redit.ReditRunner;
import io.redit.exceptions.RuntimeEngineException;
import org.apache.zookeeper.*;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import io.redit.helpers.ZookeeperHelper;

import java.io.*;
import java.util.Arrays;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeoutException;

import static org.junit.Assert.assertNull;

public class SampleTest {
    private static final Logger logger = LoggerFactory.getLogger(SampleTest.class);
    protected static ReditRunner runner;
    private static ZookeeperHelper helper;
    private static ZooKeeper[] zooKeeper = {null, null, null};
    private static String nodePath_f = "/test";
    private static String nodePath1 = "/test/e1";
    private static String nodePath2 = "/test/e2";
    private static String nodePath3 = "/test/e3";

    @BeforeClass
    public static void before() throws RuntimeEngineException, IOException, InterruptedException {
        runner = ReditRunner.run(ReditHelper.getDeployment());
        ReditHelper.startNodes(runner);
        helper = new ZookeeperHelper(runner, ReditHelper.getZookeeperHomeDir(), logger, ReditHelper.getFileRW(), ReditHelper.numOfServers);
        helper.addConfFile();
        logger.info("wait for zookeeper...");
        helper.startServers();
        Thread.sleep(5000);
        helper.checkServersStatus();
    }

    @AfterClass
    public static void after() {
        if (runner != null) {
            runner.stop();
        }
    }

    @Test
    public void testUnexpiredEphemeralNodesAfterClusterRestart() throws RuntimeEngineException, TimeoutException, InterruptedException {
        runner.runtime().enforceOrder("E1", () -> {
            CountDownLatch countDownLatch = new CountDownLatch(1);
            try {
                zooKeeper[0] = new ZooKeeper(helper.connectionStr, 4000, watchedEvent -> countDownLatch.countDown());
                countDownLatch.await();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });

        runner.runtime().enforceOrder("E2", () -> {
            try {
                zooKeeper[0].create(nodePath_f, "nodePath_f".getBytes(), ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
                zooKeeper[0].create(nodePath1, "nodePath1 data".getBytes(), ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.EPHEMERAL);
                zooKeeper[0].create(nodePath2, "nodePath2 data".getBytes(), ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.EPHEMERAL);
                zooKeeper[0].create(nodePath3, "nodePath3 data".getBytes(), ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.EPHEMERAL);
                logger.info("create ephemeral node on " + nodePath1 + ", " + nodePath2 + ", " + nodePath3);
                logger.info(zooKeeper[0].getChildren(nodePath_f, false).toString());
                logger.info(Arrays.toString(zooKeeper[0].getData(nodePath1, false, null)));
                logger.info(Arrays.toString(zooKeeper[0].getData(nodePath2, false, null)));
                logger.info(Arrays.toString(zooKeeper[0].getData(nodePath3, false, null)));
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });

        runner.runtime().enforceOrder("X1", () -> {
            runner.runtime().killNode("server1");
            runner.runtime().killNode("server2");
            runner.runtime().killNode("server3");
        });

        runner.runtime().enforceOrder("X2", () -> {
            runner.runtime().startNode("server2");
            runner.runtime().startNode("server3");
            helper.startServer(2);
            helper.startServer(3);

            try {
                Thread.sleep(15000);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            helper.checkServerStatus(2);
            helper.checkServerStatus(3);
        });


        runner.runtime().enforceOrder("E3", () -> {
            CountDownLatch countDownLatch2 = new CountDownLatch(1);
            try {
                zooKeeper[1] = new ZooKeeper(runner.runtime().ip("server2") + ":2181", 4000, watchedEvent -> countDownLatch2.countDown());
                logger.info(zooKeeper[1].getChildren(nodePath_f, false).toString());
                logger.info(Arrays.toString(zooKeeper[1].getData(nodePath1, false, null)));
                logger.info(Arrays.toString(zooKeeper[1].getData(nodePath2, false, null)));
                logger.info(Arrays.toString(zooKeeper[1].getData(nodePath3, false, null)));
                zooKeeper[1].close();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });

        runner.runtime().enforceOrder("E4", () -> {
            CountDownLatch countDownLatch3 = new CountDownLatch(1);
            try {
                zooKeeper[2] = new ZooKeeper(runner.runtime().ip("server3") + ":2181", 4000, watchedEvent -> countDownLatch3.countDown());
                logger.info(zooKeeper[2].getChildren(nodePath_f, false).toString());
                logger.info(Arrays.toString(zooKeeper[2].getData(nodePath1, false, null)));
                logger.info(Arrays.toString(zooKeeper[2].getData(nodePath2, false, null)));
                logger.info(Arrays.toString(zooKeeper[2].getData(nodePath3, false, null)));
                zooKeeper[2].close();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });

        runner.runtime().enforceOrder("E5", () -> {
            try {
                logger.info(zooKeeper[0].getChildren(nodePath_f, false).toString());
                logger.info(Arrays.toString(zooKeeper[0].getData(nodePath1, false, null)));
                logger.info(Arrays.toString(zooKeeper[0].getData(nodePath2, false, null)));
                logger.info(Arrays.toString(zooKeeper[0].getData(nodePath3, false, null)));
                zooKeeper[0].close();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });

        runner.runtime().waitForRunSequenceCompletion(20);
        logger.info("completed !!!");
    }
}
