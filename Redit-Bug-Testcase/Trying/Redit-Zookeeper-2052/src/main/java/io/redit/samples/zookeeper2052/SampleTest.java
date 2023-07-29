package io.redit.samples.zookeeper2052;

import io.redit.ReditRunner;
import io.redit.exceptions.RuntimeEngineException;
import org.apache.zookeeper.*;
import org.apache.zookeeper.data.Stat;
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

public class SampleTest {
    private static final Logger logger = LoggerFactory.getLogger(SampleTest.class);
    protected static ReditRunner runner;
    private static ZookeeperHelper helper;
    private static ZooKeeper[] zooKeeper = {null, null};
    private static String nodePath = "/metadata";
    private static String nodePath2 = "/metadata/resources";

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
    public void testWatcherExpiredAfterAllServerDown() throws RuntimeEngineException, TimeoutException, InterruptedException {
        runner.runtime().enforceOrder("E1", () -> {
            CountDownLatch countDownLatch = new CountDownLatch(1);
            try {
                zooKeeper[0] = new ZooKeeper(helper.connectionStr, 10000, watchedEvent -> countDownLatch.countDown());
                zooKeeper[0].create(nodePath, "nodePath data".getBytes(), ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
                zooKeeper[0].create(nodePath2, "nodePath2 data".getBytes(), ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
                logger.info(Arrays.toString(zooKeeper[0].getData(nodePath2, false, null)));
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });

        runner.runtime().enforceOrder("E2", () -> {
            CountDownLatch countDownLatch2 = new CountDownLatch(1);
            try {
                zooKeeper[1] = new ZooKeeper(runner.runtime().ip("server3") + ":2181", 4000, watchedEvent -> countDownLatch2.countDown());
                zooKeeper[1].create(nodePath2 + "/test1", "test1 data".getBytes(), ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.EPHEMERAL);
                zooKeeper[1].create(nodePath2 + "/test2", "test2 data".getBytes(), ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.EPHEMERAL);
                logger.info(zooKeeper[1].getChildren(nodePath2, false).toString());
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });

        runner.runtime().enforceOrder("E3", () -> {
            try {
                zooKeeper[0].delete(nodePath2, -1);
            } catch (Exception e) {
                System.out.println(e);;
            }
        });

        runner.runtime().enforceOrder("X1", () -> {
            try {
                runner.runtime().killNode("server3");
            } catch (Exception e) {
                System.out.println(e);;
            }
        });
        Thread.sleep(8000);

        runner.runtime().enforceOrder("E4", () -> {
            try {
                logger.info(zooKeeper[0].getChildren(nodePath2, false).toString());
                zooKeeper[0].delete(nodePath2, -1);
                zooKeeper[0].exists(nodePath2, false);
                Stat exists = zooKeeper[0].exists(nodePath2, false);
                logger.info("******************** Node have not been deleted : " + exists.toString());
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });

        runner.runtime().waitForRunSequenceCompletion(20);
        logger.info("completed !!!");
    }
}
