package io.redit.samples.zookeeper4508;

import io.redit.ReditRunner;
import io.redit.exceptions.RuntimeEngineException;
import io.redit.execution.CommandResults;
import io.redit.helpers.ZookeeperHelper;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.ZooKeeper;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;

public class SampleTest {
    private static final Logger logger = LoggerFactory.getLogger(SampleTest.class);
    protected static ReditRunner runner;
    private static ZookeeperHelper helper;

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
    public void sampleTest() throws Exception {
        runner.runtime().enforceOrder("E1", () -> {
            try {
                testWatcherExpiredAfterAllServerDown();
            } catch (Exception e) {
                e.printStackTrace();
            }
        });

        runner.runtime().waitForRunSequenceCompletion(20);
        logger.info("completed !!!");
    }

    public void testWatcherExpiredAfterAllServerDown() throws Exception {
        final CountDownLatch countDownLatch = new CountDownLatch(1);
        ZooKeeper zk = new ZooKeeper(helper.connectionStr, 4000, watchedEvent -> countDownLatch.countDown());
        countDownLatch.await();
        CompletableFuture<Void> expired = new CompletableFuture<>();
        zk.register(event -> {
            if (event.getState() == Watcher.Event.KeeperState.Expired) {
                expired.complete(null);
            }
        });

        for (int i = 1; i < 4; i++) {
            runner.runtime().killNode("server" + i);
        }
        expired.join();
    }
}
