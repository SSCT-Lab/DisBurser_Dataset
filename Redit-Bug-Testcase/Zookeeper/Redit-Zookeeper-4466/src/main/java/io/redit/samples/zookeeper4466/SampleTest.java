package io.redit.samples.zookeeper4466;

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
import java.util.concurrent.*;

import static org.apache.zookeeper.AddWatchMode.PERSISTENT;
import static org.apache.zookeeper.AddWatchMode.PERSISTENT_RECURSIVE;
import static org.junit.Assert.*;
import static org.junit.Assert.assertEquals;

public class SampleTest {
    private static final Logger logger = LoggerFactory.getLogger(SampleTest.class);
    protected static ReditRunner runner;
    private static ZookeeperHelper helper;
    private static ZooKeeper[] zooKeeper = {null, null};

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
    public void testPathOverlapWithStandardWatcher() throws RuntimeEngineException, TimeoutException {
        BlockingQueue<WatchedEvent> events = new LinkedBlockingQueue<>();
        Watcher persistentWatcher = event -> {
            events.add(event);
            System.out.println("PERSISTENT_RECURSIVE watcher get event: " + event.toString());
            System.out.println("now events: " + events.toString());
        };
        CountDownLatch countDownLatch = new CountDownLatch(1);

        runner.runtime().enforceOrder("E1", () -> {
            try {
                zooKeeper[0] = new ZooKeeper(helper.connectionStr, 4000, watchedEvent -> countDownLatch.countDown());
                countDownLatch.await();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });

        runner.runtime().enforceOrder("E2", () -> {
            try {
                zooKeeper[0].addWatch("/a", persistentWatcher, PERSISTENT_RECURSIVE);
                zooKeeper[0].exists("/a", event -> {System.out.println("disposable watcher get event:"  + event.toString());});
                zooKeeper[0].create("/a", new byte[0], ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
                zooKeeper[0].create("/a/b", new byte[0], ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
                zooKeeper[0].delete("/a/b", -1);
                zooKeeper[0].delete("/a", -1);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });

        runner.runtime().enforceOrder("E3", () -> {
            try {
                assertEvent(events, Watcher.Event.EventType.NodeCreated, "/a");
                assertEvent(events, Watcher.Event.EventType.NodeDeleted, "/a/b");
                assertEvent(events, Watcher.Event.EventType.NodeDeleted, "/a");
                assertTrue(countDownLatch.await(5, TimeUnit.SECONDS));
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        });

        runner.runtime().waitForRunSequenceCompletion(20);
        logger.info("completed !!!");
    }

    @Test
    public void testPathOverlapWithPersistentWatcher() throws RuntimeEngineException, TimeoutException {
        BlockingQueue<WatchedEvent> events = new LinkedBlockingQueue<>();
        Watcher persistentWatcher = event -> {
            events.add(event);
            System.out.println("PERSISTENT_RECURSIVE watcher get event: " + event.toString());
            System.out.println("now events: " + events.toString());
        };
        CountDownLatch countDownLatch = new CountDownLatch(1);

        runner.runtime().enforceOrder("E1", () -> {
            try {
                zooKeeper[1] = new ZooKeeper(helper.connectionStr, 4000, watchedEvent -> countDownLatch.countDown());
                countDownLatch.await();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });

        runner.runtime().enforceOrder("E2", () -> {
            try {
                zooKeeper[1].addWatch("/a", persistentWatcher, PERSISTENT_RECURSIVE);
                zooKeeper[1].addWatch("/a/b", event -> {System.out.println("other PERSISTENT watcher get event:"  + event.toString());}, PERSISTENT);
                zooKeeper[1].create("/a", new byte[0], ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
                zooKeeper[1].create("/a/b", new byte[0], ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
                zooKeeper[1].create("/a/b/c", new byte[0], ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
                zooKeeper[1].delete("/a/b/c", -1);
                zooKeeper[1].delete("/a/b", -1);
                zooKeeper[1].delete("/a", -1);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });

        runner.runtime().enforceOrder("E3", () -> {
            try {
                assertEvent(events, Watcher.Event.EventType.NodeCreated, "/a");
                assertEvent(events, Watcher.Event.EventType.NodeCreated, "/a/b");
                assertEvent(events, Watcher.Event.EventType.NodeCreated, "/a/b/c");
                assertEvent(events, Watcher.Event.EventType.NodeChildrenChanged, "/a/b");
                assertEvent(events, Watcher.Event.EventType.NodeDeleted, "/a/b/c");
                assertEvent(events, Watcher.Event.EventType.NodeChildrenChanged, "/a/b");
                assertEvent(events, Watcher.Event.EventType.NodeDeleted, "/a/b");
                assertEvent(events, Watcher.Event.EventType.NodeDeleted, "/a");
                assertTrue(countDownLatch.await(5, TimeUnit.SECONDS));
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        });

        runner.runtime().waitForRunSequenceCompletion(20);
        logger.info("completed !!!");
    }


    private void assertEvent(BlockingQueue<WatchedEvent> events, Watcher.Event.EventType eventType, String path)
            throws InterruptedException {
        WatchedEvent event = events.poll(5, TimeUnit.SECONDS);
        assertNotNull(event);
        assertEquals(eventType, event.getType());
        assertEquals(path, event.getPath());
    }
}
