package io.redit.samples.hbase23682;

import io.redit.ReditRunner;
import io.redit.exceptions.RuntimeEngineException;
import io.redit.helpers.HbaseHelper;
import io.redit.helpers.HdfsHelper;
import io.redit.helpers.ZookeeperHelper;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.io.*;

public class SampleTest {
    private static final Logger logger = LoggerFactory.getLogger(SampleTest.class);
    protected static ReditRunner runner;
    private static HdfsHelper hdfsHelper;
    private static ZookeeperHelper zookeeperHelper;
    private static HbaseHelper hbaseHelper;

    @BeforeClass
    public static void before() throws RuntimeEngineException, IOException, InterruptedException {
        runner = ReditRunner.run(ReditHelper.getDeployment());
        ReditHelper.startHdfsNodes(runner);
        hdfsHelper = new HdfsHelper(runner, ReditHelper.getHadoopHomeDir(), logger, ReditHelper.numOfNNs);

        hdfsHelper.waitActive();
        logger.info("The Hdfs cluster is UP!");
        hdfsHelper.transitionToActive(1, runner);
        hdfsHelper.checkNNs(runner);

        ReditHelper.startServerNodes(runner);
        zookeeperHelper = new ZookeeperHelper(runner, ReditHelper.getZookeeperHomeDir(), logger, ReditHelper.getZookeeperFileRW(), ReditHelper.numOfServers);
        hbaseHelper = new HbaseHelper(runner, ReditHelper.getHbaseHomeDir(), logger, ReditHelper.getHbaseFileRW(), ReditHelper.numOfServers);
        zookeeperHelper.addConfFile();
        hbaseHelper.addRegionConf();

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
    public void testFixNPEWhenDisableDeadServerMetricRegionChore() throws RuntimeEngineException, InterruptedException {
        hbaseHelper.startSsh();
        hbaseHelper.startHbases();
        Thread.sleep(30000);
        hbaseHelper.checkJps();
        logger.info("completed !!!");
    }
}