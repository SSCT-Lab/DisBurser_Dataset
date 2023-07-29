package io.redit.samples.hdfs14987;

import io.redit.ReditRunner;
import io.redit.exceptions.RuntimeEngineException;
import io.redit.execution.CommandResults;
import io.redit.helpers.HdfsHelper;
import io.redit.helpers.Utils;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.hdfs.DistributedFileSystem;
import org.apache.hadoop.hdfs.client.HdfsDataInputStream;
import org.apache.hadoop.hdfs.protocol.LocatedBlock;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.TimeoutException;

public class SampleTest {
    private static final Logger logger = LoggerFactory.getLogger(SampleTest.class);
    protected static ReditRunner runner;
    private static HdfsHelper helper;
    private static DistributedFileSystem dfs;
    private static final String policy = "XOR-2-1-1024k";
    private static final String ecFilePath = "/test_ec";
    private static final String replicaFilePath = "/test_replica";
    private static final String testFile = "aa.txt";
    private static final String data = "hello hadoop hello hdfs";
    private static long ecBlockId;
    private static long replicaBlockId;

    @BeforeClass
    public static void before() throws RuntimeEngineException, IOException, InterruptedException {
        runner = ReditRunner.run(ReditHelper.getDeployment());
        ReditHelper.startNodes(runner);
        helper = new HdfsHelper(runner, ReditHelper.getHadoopHomeDir(), logger, ReditHelper.numOfNNs);

        helper.waitActive();
        logger.info("The cluster is UP!");
        helper.transitionToActive(1, runner);
        helper.checkNNs(runner);
        dfs = helper.getDFS(runner);
    }

    @AfterClass
    public static void after() {
        if (runner != null) {
            runner.stop();
        }
    }

    @Test
    public void testECFileBlockIdLocationInfoDisplayingAsNull() throws RuntimeEngineException, TimeoutException {
        runner.runtime().enforceOrder("E1", () -> {
            try {
                logger.info("set ErasureCoding Policy: " + policy + " on " + ecFilePath);
                dfs.enableErasureCodingPolicy(policy);
                dfs.mkdirs(new Path(ecFilePath));
                dfs.mkdirs(new Path(replicaFilePath));
                dfs.setErasureCodingPolicy(new Path(ecFilePath), policy);
            } catch (IOException e) {
                e.printStackTrace();
            }
        });

        runner.runtime().enforceOrder("E2", () -> {
            logger.info("put " + testFile + " into HDFS ..." );
            runner.runtime().runCommandInNode("nn1", "touch " + testFile + " && echo \"" + data + "\" >> " + testFile);
            runner.runtime().runCommandInNode("nn1", ReditHelper.getHadoopHomeDir() + "/bin/hdfs dfs -put " + testFile + " " + ecFilePath);
            runner.runtime().runCommandInNode("nn1", ReditHelper.getHadoopHomeDir() + "/bin/hdfs dfs -put " + testFile + " " + replicaFilePath);
        });

        runner.runtime().enforceOrder("E3", () -> {
            try {
                ecBlockId = getBlockId(ecFilePath + "/" + testFile);
                replicaBlockId = getBlockId(replicaFilePath + "/" + testFile);
            } catch (IOException e) {
                e.printStackTrace();
            }
        });

        runner.runtime().enforceOrder("E4", () -> {
            String ViewBlocksCommand_ec = ReditHelper.getHadoopHomeDir() + "/bin/hdfs fsck " + ecFilePath + "/" + testFile + " -blockId blk_" + ecBlockId;
            CommandResults result1  = runner.runtime().runCommandInNode("nn1", ViewBlocksCommand_ec);
            Utils.printResult(result1, logger);

            String ViewBlocksCommand_replica = ReditHelper.getHadoopHomeDir() + "/bin/hdfs fsck " + replicaFilePath + "/" + testFile + " -blockId blk_" + replicaBlockId;
            CommandResults result2  = runner.runtime().runCommandInNode("nn1", ViewBlocksCommand_replica);
            Utils.printResult(result2, logger);
        });

        runner.runtime().waitForRunSequenceCompletion(20);
        logger.info("completed !!!");
    }

    private static Long getBlockId(String testPath) throws IOException {
        Path path = new Path(testPath);
        List<LocatedBlock> lbs2 = ((HdfsDataInputStream) dfs.open(path)).getAllBlocks();
        Long blockId = lbs2.get(0).getBlock().getBlockId();
        logger.info(testPath + " get blockId:" + blockId);
        return blockId;
    }
}