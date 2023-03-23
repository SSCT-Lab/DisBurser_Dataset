package io.redit.samples.hdfs15398;

import io.redit.ReditRunner;
import io.redit.exceptions.RuntimeEngineException;
import io.redit.helpers.HdfsHelper;
import org.apache.hadoop.fs.FSDataOutputStream;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.hdfs.DistributedFileSystem;
import org.apache.hadoop.io.IOUtils;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

public class SampleTest {
    private static final Logger logger = LoggerFactory.getLogger(SampleTest.class);
    protected static ReditRunner runner;
    private static HdfsHelper helper;
    private static DistributedFileSystem dfs;
    private static final String policy = "RS-6-3-1024k";
    private static final Path dir = new Path("/test");
    private static final Path filePath = new Path("/test/file");

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
    public void testHdfsClientHangsDueToExceptionDuringAddBlock() throws RuntimeEngineException, TimeoutException {
        runner.runtime().enforceOrder("E1", () -> {
            try {
                dfs.mkdirs(dir);
                dfs.enableErasureCodingPolicy(policy);
                dfs.setErasureCodingPolicy(dir, policy);
            } catch (IOException e) {
                e.printStackTrace();
            }
        });

        runner.runtime().enforceOrder("E2", () -> {
            try {
                FSDataOutputStream out = dfs.create(filePath);
                for (int i = 0; i < 1024 * 1024 * 2; i++) {
                    out.write(i);
                }
                dfs.setQuota(dir, 5, 0);
                for (int i = 0; i < 1024 * 1024 * 2; i++) {
                    out.write(i);
                }
                IOUtils.closeStream(out);
            } catch (IOException e) {
                e.printStackTrace();
            }
        });

        runner.runtime().waitForRunSequenceCompletion(20);
        logger.info("completed !!!");
    }
}