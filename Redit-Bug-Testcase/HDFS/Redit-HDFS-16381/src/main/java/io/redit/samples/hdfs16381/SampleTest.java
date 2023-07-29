package io.redit.samples.hdfs16381;

import io.redit.ReditRunner;
import io.redit.exceptions.RuntimeEngineException;
import io.redit.helpers.HdfsHelper;
import org.apache.hadoop.fs.FSDataInputStream;
import org.apache.hadoop.fs.FSDataOutputStream;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.hdfs.AppendTestUtil;
import org.apache.hadoop.hdfs.DistributedFileSystem;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.io.IOException;

public class SampleTest {
    private static final Logger logger = LoggerFactory.getLogger(SampleTest.class);
    protected static ReditRunner runner;
    private static HdfsHelper helper;
    private static DistributedFileSystem dfs;
    private static String filePath = "/test/myFile.txt";
    private static Path path = new Path(filePath);
    private static final int BLOCK_SIZE = 4096;
    private static int truncateLength = 500;

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
    public void testTruncateFileWhenWrite() throws IOException {
        FSDataOutputStream outputStream = dfs.create(path);
        AppendTestUtil.write(outputStream, 0, BLOCK_SIZE / 2);
        outputStream.hflush();
//        outputStream.close();  If add this, test will pass.
        dfs.truncate(path, truncateLength);
    }

    @Test
    public void testClientRequestToAppendToTheFile() throws IOException {
        FSDataInputStream inputStream = dfs.open(path);
        logger.info(inputStream.readUTF());
        dfs.append(path, BLOCK_SIZE / 2, null);
    }
}