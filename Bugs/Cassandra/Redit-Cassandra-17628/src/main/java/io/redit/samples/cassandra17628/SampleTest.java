package io.redit.samples.cassandra17628;

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

import java.io.IOException;
import java.util.concurrent.TimeoutException;

public class SampleTest {
    private static final Logger logger = LoggerFactory.getLogger(SampleTest.class);
    private static ReditRunner runner;
    private static CassandraHelper helper;
    private static Cluster cluster;
    private static Session session;

    @BeforeClass
    public static void before() throws RuntimeEngineException, InterruptedException, IOException {
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
            session.close();
            cluster.close();
            runner.stop();
        }
    }

    @Test
    public void testCQLWriteTimeAndTTLFunctions() throws RuntimeEngineException, TimeoutException {
        runner.runtime().enforceOrder("E1", () -> {
            createTypeAndTable();
        });

        runner.runtime().enforceOrder("E2", () -> {
            String selectCQL1 = "SELECT writetime(s) FROM test.test";
            selectCQLTest(selectCQL1);
        });
        runner.runtime().enforceOrder("E3", () -> {
            String selectCQL2 = "SELECT writetime(st) FROM test.test";
            selectCQLTest(selectCQL2);
        });
        runner.runtime().enforceOrder("E4", () -> {
            String selectCQL3 = "SELECT writetime(t) FROM test.test";
            selectCQLTest(selectCQL3);
        });
        runner.runtime().enforceOrder("E5", () -> {
            String selectCQL4 = "SELECT writetime(ft) FROM test.test";
            selectCQLTest(selectCQL4);
        });

        runner.runtime().waitForRunSequenceCompletion(20);
        logger.info("completed !!!");
    }

    public void selectCQLTest(String selectCQL) {
        try {
            logger.info("selectCQL: " + selectCQL);
            session.execute(selectCQL);
        }catch (Exception e){
            System.out.println(e.getMessage());
        }
    }

    private static void createTypeAndTable(){
        cluster = Cluster.builder().addContactPoint(runner.runtime().ip("server1")).build();
        session = cluster.connect();

        String createKeySpaceCQL = "CREATE KEYSPACE if not exists test WITH replication={'class':'SimpleStrategy','replication_factor':3}";
        session.execute(createKeySpaceCQL);

        String createType = "CREATE TYPE test.udt (a int, b int)";
        session.execute(createType);

        String createTableCQL = "CREATE TABLE if not exists test.test (k int PRIMARY KEY, s set<int>, fs frozen<set<int>>, t udt, ft frozen<udt>)";
        session.execute(createTableCQL);
    }
}
