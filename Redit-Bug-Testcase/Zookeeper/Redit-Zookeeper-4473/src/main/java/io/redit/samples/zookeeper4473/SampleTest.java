package io.redit.samples.zookeeper4473;

import io.redit.ReditRunner;
import io.redit.exceptions.RuntimeEngineException;
import io.redit.execution.CommandResults;
import io.redit.helpers.ZookeeperHelper;
import org.apache.zookeeper.ZooKeeper;
import org.apache.zookeeper.inspector.manager.ZooInspectorManagerImpl;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.io.*;
import java.util.Properties;

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
                testNodeCreateRoot();
            } catch (IOException e) {
                e.printStackTrace();
            }
        });

        runner.runtime().enforceOrder("E2", () -> {
            try {
                testNodeCreateNormal();
            } catch (IOException e) {
                e.printStackTrace();
            }
        });

        runner.runtime().waitForRunSequenceCompletion(20);
        logger.info("completed !!!");
    }

    public void testNodeCreateRoot() throws IOException {
        Properties connectionProps = new Properties();
        connectionProps.setProperty("hosts", helper.connectionStr);
        connectionProps.setProperty("timeout", "5000");
        ZooInspectorManagerImpl manager = new ZooInspectorManagerImpl();
        manager.connect(connectionProps);
        boolean createSuccess = manager.createNode("/", "test");
        System.out.println("testNodeCreateRoot createSuccess: " + createSuccess);
    }

    public void testNodeCreateNormal() throws IOException {
        Properties connectionProps = new Properties();
        connectionProps.setProperty("hosts", helper.connectionStr);
        connectionProps.setProperty("timeout", "5000");
        ZooInspectorManagerImpl manager = new ZooInspectorManagerImpl();
        manager.connect(connectionProps);
        boolean createSuccess = manager.createNode("/parent", "test");
        System.out.println("testNodeCreateNormal createSuccess: " + createSuccess);
    }

}
