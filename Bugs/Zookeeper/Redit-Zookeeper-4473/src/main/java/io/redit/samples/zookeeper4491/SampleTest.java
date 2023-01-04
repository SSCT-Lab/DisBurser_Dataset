package io.redit.samples.zookeeper4491;

import io.redit.ReditRunner;
import io.redit.exceptions.RuntimeEngineException;
import io.redit.execution.CommandResults;
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
    private static String confFile = "conf/zoo.cfg";
    private static String serverConf = "";
    private static String connectionStr = "";

    @BeforeClass
    public static void before() throws RuntimeEngineException, IOException {
        runner = ReditRunner.run(ReditHelper.getDeployment());
        ReditHelper.startNodesInOrder(runner);
        for(int i = 1; i <= ReditHelper.numOfServers; i++){
            serverConf += "server." + i + "=" + runner.runtime().ip("server" + (i)) + ":2888:3888\n";
        }
        connectionStr = runner.runtime().ip("server1") + ":2181," + runner.runtime().ip("server2") + ":2181," + runner.runtime().ip("server3") + ":2181";
        addConfFile();
    }

    @AfterClass
    public static void after() {
        if (runner != null) {
            runner.stop();
        }
    }

    @Test
    public void sampleTest() throws Exception {
        logger.info("wait for zookeeper...");
        startServers();
        Thread.sleep(20000);
        checkServersStatus();
        Thread.sleep(2000);
        testNodeCreateRoot();
        testNodeCreateNormal();
        logger.info("completed !!!");
    }

    public void testNodeCreateRoot() throws IOException {
        Properties connectionProps = new Properties();
        connectionProps.setProperty("hosts", connectionStr);
        connectionProps.setProperty("timeout", "5000");
        ZooInspectorManagerImpl manager = new ZooInspectorManagerImpl();
        manager.connect(connectionProps);
        boolean createSuccess = manager.createNode("/", "test");
        System.out.println("testNodeCreateRoot createSuccess: " + createSuccess);
    }

    public void testNodeCreateNormal() throws IOException {
        Properties connectionProps = new Properties();
        connectionProps.setProperty("hosts", connectionStr);
        connectionProps.setProperty("timeout", "5000");
        ZooInspectorManagerImpl manager = new ZooInspectorManagerImpl();
        manager.connect(connectionProps);
        boolean createSuccess = manager.createNode("/parent", "test");
        System.out.println("testNodeCreateNormal createSuccess: " + createSuccess);
    }


    private static void startServers() {
        for(int i = 1; i <= ReditHelper.numOfServers; i++){
            startServer(i);
        }
    }

    private static void checkServersStatus() throws RuntimeEngineException {
        for(int i = 1; i <= ReditHelper.numOfServers; i++){
            String command = "cd " + ReditHelper.getHomeDir() + " && bin/zkServer.sh status";
            logger.info("server" + i + " checkStatus...");
            CommandResults commandResults = runner.runtime().runCommandInNode("server" + i, command);
            printResult(commandResults);
        }
    }

    private static void startServer(int serverId) {
        String command = "cd " + ReditHelper.getHomeDir() + " && bin/zkServer.sh start";
        logger.info("server" + serverId + " startServer...");
        new Thread(() -> {
            try {
                runner.runtime().runCommandInNode("server" + serverId, command);
            } catch (RuntimeEngineException e) {
                e.printStackTrace();
            }
        }).start();
    }

    private static void addConfFile() throws IOException {
        boolean addConf = false;
        BufferedReader in = new BufferedReader(new FileReader(confFile));
        String str;
        while ((str = in.readLine()) != null) {
            if (str.startsWith("server")){
                addConf = true;
            }
        }
        in.close();
        if (addConf){
            logger.info("already add config in " + confFile);
        }
        else {
            BufferedWriter out = new BufferedWriter(new FileWriter(confFile, true));
            out.write(serverConf);
            out.close();
            logger.info("add config to " + confFile + ":\n" + serverConf);
        }
    }

    private static void printResult(CommandResults commandResults){
        logger.info(commandResults.nodeName() + ": " + commandResults.command());
        if (commandResults.stdOut() != null){
            logger.info(commandResults.stdOut());
        }else {
            logger.warn(commandResults.stdErr());
        }
    }

}
