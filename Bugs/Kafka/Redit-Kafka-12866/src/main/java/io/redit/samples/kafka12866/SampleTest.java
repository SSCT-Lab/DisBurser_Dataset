package io.redit.samples.kafka12866;

import io.redit.ReditRunner;
import io.redit.exceptions.RuntimeEngineException;
import io.redit.helpers.KafkaHelper;
import io.redit.helpers.ZookeeperHelper;
import org.apache.zookeeper.*;
import org.apache.zookeeper.data.ACL;
import org.apache.zookeeper.data.Id;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.Base64;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeoutException;

public class SampleTest {
    private static final Logger logger = LoggerFactory.getLogger(SampleTest.class);
    protected static ReditRunner runner;
    private static ZookeeperHelper zookeeperHelper;
    private static KafkaHelper kafkaHelper;
    private static final int SESSION_TIMEOUT = 30000;
    private CountDownLatch countDownLatch = new CountDownLatch(1);
    private static ZooKeeper zooKeeper;

    @BeforeClass
    public static void before() throws RuntimeEngineException, IOException, InterruptedException {
        runner = ReditRunner.run(ReditHelper.getDeployment());
        ReditHelper.startNodes(runner);
        zookeeperHelper = new ZookeeperHelper(runner, ReditHelper.getZookeeperHomeDir(), logger, ReditHelper.getZookeeperFileRW(), ReditHelper.numOfServers);
        zookeeperHelper.addConfFile();
        kafkaHelper = new KafkaHelper(runner, ReditHelper.getKafkaHomeDir(), logger, ReditHelper.getKafkaFileRW(), ReditHelper.numOfServers, "/chroot");
        kafkaHelper.addKafkaPropFile();

        zookeeperHelper.startServers();
        Thread.sleep(5000);
        zookeeperHelper.checkServersStatus();
    }

    @AfterClass
    public static void after() {
        if (runner != null) {
            runner.stop();
        }
    }

    @Test
    public void testKafkaRequiresZKRootAccess() throws RuntimeEngineException, TimeoutException {
        runner.runtime().enforceOrder("E1", () -> {
            try {
                getConnect();
            } catch (IOException e) {
                e.printStackTrace();
            }
        });

        runner.runtime().enforceOrder("E2", () -> {
            try {
                createNode();
            } catch (KeeperException e) {
                e.printStackTrace();
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (NoSuchAlgorithmException e) {
                e.printStackTrace();
            }
        });

        runner.runtime().enforceOrder("E3", () -> {
            kafkaHelper.startKafkas();
            try {
                Thread.sleep(5000);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            kafkaHelper.checkJps();
        });

        runner.runtime().waitForRunSequenceCompletion(20);
        logger.info("completed !!!");
    }

    private void getConnect() throws IOException {
        Watcher watcher = event -> {
            if(Watcher.Event.KeeperState.SyncConnected == event.getState()){
                countDownLatch.countDown();
                String msg = String.format("process info,eventType:%s,eventState:%s,eventPath:%s", event.getType(),event.getState(),event.getPath());
                System.out.println(msg);
            }
        };
        zooKeeper = new ZooKeeper(zookeeperHelper.connectionStr, SESSION_TIMEOUT, watcher);
        try {
            countDownLatch.await();
            System.out.println("Zookeeper session establish success,sessionID = " + Long.toHexString(zooKeeper.getSessionId()));
        } catch (InterruptedException e) {
            e.printStackTrace();
            logger.debug("Zookeeper session establish fail");
        }
    }

    public void createNode() throws KeeperException, InterruptedException, NoSuchAlgorithmException {
        String result = null;
        try {
            result = zooKeeper.create("/chroot",//节点的全路径
                    "zk001-data".getBytes(),//节点中的数据->字节数据
                    ZooDefs.Ids.OPEN_ACL_UNSAFE,//指定访问控制列表
                    CreateMode.PERSISTENT //指定创建节点的类型
            );
            Thread.sleep(10000);
        } catch (Exception e) {
            logger.error(e.getMessage());
        }
        System.out.println("create node success,result = " + result);
        byte[] id = Base64.getEncoder().encode(MessageDigest.getInstance("SHA1").digest("test:12345".getBytes()));
        zooKeeper.addAuthInfo("digest", id);
        zooKeeper.setACL("/", Arrays.asList(new ACL(ZooDefs.Perms.ALL, new Id("digest", "test:" + id))),-1);
    }
}
