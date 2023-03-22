package io.redit.samples.hbase26742;

import io.redit.ReditRunner;
import io.redit.exceptions.RuntimeEngineException;
import io.redit.helpers.HbaseHelper;
import io.redit.helpers.HdfsHelper;
import io.redit.helpers.ZookeeperHelper;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.CompareOperator;
import org.apache.hadoop.hbase.HBaseConfiguration;
import org.apache.hadoop.hbase.TableName;
import org.apache.hadoop.hbase.client.*;
import org.apache.hadoop.hbase.util.Bytes;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;

import static org.junit.Assert.*;

public class SampleTest {
    private static final Logger logger = LoggerFactory.getLogger(SampleTest.class);
    protected static ReditRunner runner;
    private static HdfsHelper hdfsHelper;
    private static ZookeeperHelper zookeeperHelper;
    private static HbaseHelper hbaseHelper;

    private static Connection connection;
    private static Admin admin = null;
    private static final TableName tableName = TableName.valueOf("t1");
    private static final byte[] FAMILY = Bytes.toBytes("cf");

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
    public void sampleTest() throws Exception {
        runner.runtime().enforceOrder("E1", () -> {
            getConnection();
        });
        Thread.sleep(2000);

        runner.runtime().enforceOrder("E2", () -> {
            try {
                testCheckAndMutateForNull();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });

        runner.runtime().waitForRunSequenceCompletion(20);
        logger.info("completed !!!");
    }

    private static void testCheckAndMutateForNull() throws IOException {
        byte[] qualifier = Bytes.toBytes("Q");
        try (Table table = createTable()) {
            byte[] row1 = Bytes.toBytes("testRow1");
            Put put = new Put(row1);
            put.addColumn(FAMILY, qualifier, Bytes.toBytes("v0"));
            table.put(put);
            assertEquals("v0", Bytes.toString(
                    table.get(new Get(row1).addColumn(FAMILY, qualifier)).getValue(FAMILY, qualifier)));

            CheckAndMutate checkAndMutate1 = CheckAndMutate.newBuilder(row1)
                    .ifMatches(FAMILY, qualifier, CompareOperator.NOT_EQUAL, new byte[]{})
                    .build(new Put(row1).addColumn(FAMILY, qualifier, Bytes.toBytes("v1")));
            table.checkAndMutate(checkAndMutate1);
            assertEquals("v1", Bytes.toString(table.get(new Get(row1).addColumn(FAMILY, qualifier)).getValue(FAMILY, qualifier)));

            byte[] row2 = Bytes.toBytes("testRow2");
            put = new Put(row2);
            put.addColumn(FAMILY, qualifier, new byte[]{});
            table.put(put);
            assertEquals(0, table.get(new Get(row2).addColumn(FAMILY, qualifier)).getValue(FAMILY, qualifier).length);

            CheckAndMutate checkAndMutate2 = CheckAndMutate.newBuilder(row2)
                    .ifMatches(FAMILY, qualifier, CompareOperator.EQUAL, new byte[]{})
                    .build(new Put(row2).addColumn(FAMILY, qualifier, Bytes.toBytes("v2")));
            table.checkAndMutate(checkAndMutate2);
            assertEquals("v2", Bytes.toString(table.get(new Get(row2).addColumn(FAMILY, qualifier)).getValue(FAMILY, qualifier)));

            byte[] row3 = Bytes.toBytes("testRow3");
            put = new Put(row3).addColumn(FAMILY, qualifier, Bytes.toBytes("v0"));
            assertNull(table.get(new Get(row3).addColumn(FAMILY, qualifier)).getValue(FAMILY, qualifier));
            CheckAndMutate checkAndMutate3 = CheckAndMutate.newBuilder(row3)
                    .ifMatches(FAMILY, qualifier, CompareOperator.NOT_EQUAL, new byte[]{})
                    .build(put);
            table.checkAndMutate(checkAndMutate3);
            assertNull(table.get(new Get(row3).addColumn(FAMILY, qualifier)).getValue(FAMILY, qualifier));

            CheckAndMutate checkAndMutate4 = CheckAndMutate.newBuilder(row3)
                    .ifMatches(FAMILY, qualifier, CompareOperator.EQUAL, new byte[]{})
                    .build(put);
            table.checkAndMutate(checkAndMutate4);
            assertEquals("v0", Bytes.toString(table.get(new Get(row3).addColumn(FAMILY, qualifier)).getValue(FAMILY, qualifier)));
        }
    }

    private static Table createTable() throws IOException {
        TableDescriptor tableDescriptor = TableDescriptorBuilder.newBuilder(tableName).setColumnFamily(ColumnFamilyDescriptorBuilder
                        .newBuilder(FAMILY).build()).build();
        admin.createTable(tableDescriptor);
        return connection.getTable(tableName);
    }

    private static void getConnection() {
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
}
