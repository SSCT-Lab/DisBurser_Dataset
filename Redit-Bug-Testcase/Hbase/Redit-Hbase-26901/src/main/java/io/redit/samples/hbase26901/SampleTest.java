package io.redit.samples.hbase26901;

import io.redit.ReditRunner;
import io.redit.exceptions.RuntimeEngineException;
import io.redit.helpers.HbaseHelper;
import io.redit.helpers.HdfsHelper;
import io.redit.helpers.ZookeeperHelper;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.HBaseConfiguration;
import org.apache.hadoop.hbase.TableName;
import org.apache.hadoop.hbase.client.*;
import org.apache.hadoop.hbase.client.Result;
import org.apache.hadoop.hbase.util.Bytes;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.io.*;

import static org.junit.Assert.assertTrue;

public class SampleTest {
    private static final Logger logger = LoggerFactory.getLogger(SampleTest.class);
    protected static ReditRunner runner;
    private static HdfsHelper hdfsHelper;
    private static ZookeeperHelper zookeeperHelper;
    private static HbaseHelper hbaseHelper;
    private static Connection connection;
    private static Admin admin;
    private static TableName tableName = TableName.valueOf("t1");
    private static final byte[] ROW = Bytes.toBytes("r1");
    private static final byte[] FAMILY = Bytes.toBytes("f");

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

        hbaseHelper.startSsh();
        hbaseHelper.startHbases();
        Thread.sleep(20000);
        hbaseHelper.checkJps();
    }

    @AfterClass
    public static void after() {
        if (runner != null) {
            runner.stop();
        }
    }

    @Test
    public void testDeleteWithNullColumnQualifierOccursNullPointerException() throws Exception {
        runner.runtime().enforceOrder("E1", () -> {
            getConnection();
        });
        Thread.sleep(2000);

        runner.runtime().enforceOrder("E2", () -> {
            try {
                testNullColumnQualifier();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });

        runner.runtime().waitForRunSequenceCompletion(20);
        logger.info("completed !!!");
    }

    public void getConnection() {
        Configuration conf = HBaseConfiguration.create();
        conf.set("hbase.zookeeper.property.clientPort", "2181");
        logger.info("hbase.zookeeper.quorum: " + hbaseHelper.HbaseSiteConf);
        conf.set("hbase.zookeeper.quorum", hbaseHelper.HbaseSiteConf);
        try {
            connection = ConnectionFactory.createConnection(conf);
            admin = connection.getAdmin();
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.println("connection: " + connection);
        System.out.println("admin: " + admin);
    }

    private static void testNullColumnQualifier() throws IOException {
        try (Table t = createTable()) {
            Delete del = new Delete(ROW);
            del.addColumn(FAMILY, null);
            t.delete(del);
            Result r = t.get(new Get(ROW));
            assertTrue(r.isEmpty());
        }
    }

    private static Table createTable() throws IOException {
        TableDescriptor tableDescriptor = TableDescriptorBuilder.newBuilder(tableName).setColumnFamily(ColumnFamilyDescriptorBuilder
                        .newBuilder(FAMILY).setNewVersionBehavior(true).setMaxVersions(3).build()).build();
        admin.createTable(tableDescriptor);
        return connection.getTable(tableName);
    }
}
