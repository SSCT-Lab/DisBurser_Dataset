package io.redit.samples.hdfs11379;

import io.redit.ReditRunner;
import io.redit.exceptions.RuntimeEngineException;
import io.redit.helpers.HdfsHelper;
import org.apache.hadoop.fs.FSDataInputStream;
import org.apache.hadoop.fs.FSDataOutputStream;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.hdfs.DistributedFileSystem;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.EOFException;
import java.io.IOException;
import java.util.concurrent.*;

import static org.junit.Assert.assertTrue;

public class SampleTest {
    private static final Logger logger = LoggerFactory.getLogger(SampleTest.class);
    protected static ReditRunner runner;
    private static HdfsHelper helper;
    private static DistributedFileSystem dfs;
    private static Path path = new Path("/testfile");
    private final int blockSize = 512;
    private static FSDataInputStream dis;
    private static Future<?> future;
    private static ExecutorService executor;

    @BeforeClass
    public static void before() throws RuntimeEngineException, IOException, InterruptedException {
        runner = ReditRunner.run(ReditHelper.getDeployment());
        ReditHelper.startNodes(runner);
        helper = new HdfsHelper(runner, ReditHelper.getHadoopHomeDir(), logger, ReditHelper.numOfNNs);

        helper.waitActiveForNNs();
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
    public void testDFSInputStreamMayInfiniteLoopRequestingBlockLocations() throws RuntimeEngineException, InterruptedException, TimeoutException {
        runner.runtime().enforceOrder("E1", () -> {
            try {
                FSDataOutputStream dos = dfs.create(path, true, blockSize, (short)1, blockSize);
                dos.write(new byte[blockSize * 3]);
                dos.close();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });

        runner.runtime().enforceOrder("X1", () -> {
            try {
                dis = dfs.open(path);
                while (!dfs.truncate(path, 10)) {
                    Thread.sleep(10);
                }
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });

        runner.runtime().enforceOrder("E2", () -> {
            executor = Executors.newFixedThreadPool(1);
            future = executor.submit((Callable<Void>) () -> {
                // read from 2nd block.
                dis.readFully(blockSize, new byte[4]);
                return null;
            });
        });

        runner.runtime().enforceOrder("E3", () -> {
            try {
                future.get(4, TimeUnit.SECONDS);
                Assert.fail();
            } catch (ExecutionException e) {
                assertTrue(e.toString(), e.getCause() instanceof EOFException);
            } catch (InterruptedException e){
                throw new RuntimeException(e);
            } catch (TimeoutException e){
                throw new RuntimeException(e);
            } finally {
                future.cancel(true);
                executor.shutdown();
            }
        });

        runner.runtime().waitForRunSequenceCompletion(20);
        logger.info("completed !!!");
    }
}