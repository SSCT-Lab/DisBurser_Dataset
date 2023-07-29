package io.redit.samples.cassandra14365;

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
    public void TestReplayCommitLog() throws RuntimeEngineException, InterruptedException, TimeoutException {
        runner.runtime().enforceOrder("E1", () -> {
            createTableAndInsert();
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

    private static void createTableAndInsert(){
        Cluster cluster = null;
        Session session = null;
        try {
            // 定义一个cluster类
            cluster = Cluster.builder().addContactPoint(runner.runtime().ip("server1")).build();
            // 获取session对象
            session = cluster.connect();
            // 创建键空间
            String createKeySpaceCQL = "create keyspace test WITH replication = {'class':'SimpleStrategy', 'replication_factor' : 3}";
            session.execute(createKeySpaceCQL);
            session.execute("use test");
            // 创建列族
            String createTableCQL = "CREATE TABLE test.x (id int, id2 frozen<map<text, text>>, st int static, PRIMARY KEY (id, id2));";
            session.execute(createTableCQL);

            String insertCQL = "INSERT INTO test.x (id, st) VALUES (1, 2);";
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
