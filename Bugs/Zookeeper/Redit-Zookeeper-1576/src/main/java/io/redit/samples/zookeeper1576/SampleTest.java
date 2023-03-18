package io.redit.samples.zookeeper1576;

import io.redit.ReditRunner;
import io.redit.exceptions.RuntimeEngineException;
import org.apache.zookeeper.*;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import io.redit.helpers.ZookeeperHelper;
import io.redit.helpers.Utils;

import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeoutException;

public class SampleTest {
    private static final Logger logger = LoggerFactory.getLogger(SampleTest.class);
    protected static ReditRunner runner;
    private static ZookeeperHelper helper;
    private static ZooKeeper zooKeeper = null;
    private static String nodePath = "/test";
    private static String dropIp = null;

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
    public void testFailedToConnectToCluster() throws RuntimeEngineException, TimeoutException, InterruptedException {
        runner.runtime().enforceOrder("X1", () -> {
            ArrayList<String> list = new ArrayList<>();
            dropIp = runner.runtime().ip("server2");
            list.add("sudo iptables --flush");
            list.add("sudo iptables -I INPUT -s " + dropIp + " -j DROP");
            list.add("sudo iptables --list");
            Utils.runCmdList(list);
        });

        runner.runtime().enforceOrder("E1", () -> {
            CountDownLatch countDownLatch = new CountDownLatch(1);
            try {
                zooKeeper = new ZooKeeper(helper.connectionStr, 4000, watchedEvent -> countDownLatch.countDown());
                countDownLatch.await();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });

        runner.runtime().enforceOrder("E2", () -> {
            try {
                zooKeeper.create(nodePath, "nodePath data".getBytes(), ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.EPHEMERAL);
                logger.info(Arrays.toString(zooKeeper.getData(nodePath, false, null)));
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });

        runner.runtime().enforceOrder("X2", () -> {
            ArrayList<String> list2 = new ArrayList<>();
            list2.add("sudo iptables -D INPUT -s " + dropIp + " -j DROP");
            list2.add("sudo iptables --list");
            Utils.runCmdList(list2);
        });

        runner.runtime().waitForRunSequenceCompletion(20);
        logger.info("completed !!!");
    }
}
