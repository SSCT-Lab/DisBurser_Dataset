package io.redit.samples.cassandra13669;

import com.datastax.driver.core.Cluster;
import com.datastax.driver.core.Session;
import io.redit.ReditRunner;
import io.redit.exceptions.RuntimeEngineException;
import io.redit.helpers.CassandraHelper;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.concurrent.TimeoutException;

public class SampleTest {
    private static final Logger logger = LoggerFactory.getLogger(SampleTest.class);
    private static ReditRunner runner;
    private static CassandraHelper helper;

    @BeforeClass
    public static void before() throws RuntimeEngineException, IOException, InterruptedException {
        runner = ReditRunner.run(ReditHelper.getDeployment());
        ReditHelper.startNodes(runner);
        helper = new CassandraHelper(runner, ReditHelper.getCassandraHomeDir(), logger, ReditHelper.getFileRW(), ReditHelper.numOfServers);
        String seedsIp = runner.runtime().ip("server1");
        helper.addCassandraYamlFile(seedsIp);
        helper.makeCassandraDirs();

        logger.info("wait for Cassandra ...");
        helper.startServer(1);
        Thread.sleep(5000);
        helper.startServer(2);
        Thread.sleep(10000);
        helper.checkNetStatus(2);
        helper.checkStatus(1);
    }

    @AfterClass
    public static void after() {
        if (runner != null) {
            runner.stop();
        }
    }

    @Test
    public void testMakeUUIDFromSASIIndex() throws RuntimeEngineException, InterruptedException, TimeoutException {
        runner.runtime().enforceOrder("E1", () -> {
            makeUUID();
        });

        runner.runtime().enforceOrder("X1", () -> {
            runner.runtime().restartNode("server1", 30);
            helper.checkStatus(1);
            helper.startServer(1);
        });

        Thread.sleep(5000);
        runner.runtime().enforceOrder("E2", () -> {
            helper.checkStatus(1);
        });

        runner.runtime().waitForRunSequenceCompletion(20);
        logger.info("completed !!!");
    }

    private static void makeUUID(){
        Cluster cluster = null;
        Session session = null;
        try {
            // 定义一个cluster类
            cluster = Cluster.builder().addContactPoint(runner.runtime().ip("server1")).build();
            // 获取session对象
            session = cluster.connect();
            // 创建键空间
            String createKeySpaceCQL = "CREATE KEYSPACE test WITH replication = {'class': 'SimpleStrategy', 'replication_factor': '1'} AND durable_writes = true";
            session.execute(createKeySpaceCQL);
            session.execute("use test");
            // 创建列族
            String createTableCQL = "CREATE TABLE test_table(col1 uuid, col2 uuid, ts timeuuid, col3 uuid, PRIMARY KEY((col1, col2), ts)) with clustering order by (ts desc)";
            session.execute(createTableCQL);

            String createCustomIndexCQL = "CREATE CUSTOM INDEX col3_test_table_idx ON test_table(col3) USING 'org.apache.cassandra.index.sasi.SASIIndex' WITH OPTIONS = {'analyzer_class': 'org.apache.cassandra.index.sasi.analyzer.StandardAnalyzer', 'mode': 'PREFIX'}";
            session.execute(createCustomIndexCQL);

            String insertCQL = "INSERT INTO test_table(col1, col2, ts, col3) VALUES(898e0014-6161-11e7-b9b7-238ea83bd70b, 898e0014-6161-11e7-b9b7-238ea83bd70b, now(), 898e0014-6161-11e7-b9b7-238ea83bd70b)";
            session.execute(insertCQL);

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            // 关闭资源
            session.close();
            cluster.close();
        }
    }
}
