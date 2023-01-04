package io.redit.samples.cassandra16836;

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
            runner.stop();
        }
    }

    @Test
    public void testMVIncorrectQuotingOfUDF() throws RuntimeEngineException, InterruptedException, TimeoutException {
        runner.runtime().enforceOrder("E1", () -> {
            createMVWithUDF();
        });

        runner.runtime().enforceOrder("X1", () -> {
            runner.runtime().restartNode("server2", 30);
            helper.checkStatus(1);
            helper.startServer(1);
        });

        Thread.sleep(5000);
        runner.runtime().enforceOrder("E2", () -> {
            helper.checkStatus(1);
        });

        runner.runtime().enforceOrder("E3", () -> {
            runAnInsertSQL();
        });

        runner.runtime().waitForRunSequenceCompletion(20);
        logger.info("completed !!!");
    }

    private static void createMVWithUDF(){
        Cluster cluster = null;
        Session session = null;
        try {
            cluster = Cluster.builder().addContactPoint(runner.runtime().ip("server1")).build();
            session = cluster.connect();

            String createKeySpaceCQL = "create keyspace test WITH replication = {'class':'SimpleStrategy', 'replication_factor' : 3}";
            session.execute(createKeySpaceCQL);
            session.execute("use test");

            String createTableCQL = "CREATE TABLE t (k int PRIMARY KEY, v int)";
            session.execute(createTableCQL);

            String createFunctionCQL = "CREATE FUNCTION \"Double\"(input int)" +
                    "  CALLED ON NULL INPUT" +
                    "  RETURNS int LANGUAGE java" +
                    "  AS 'return input*2;'";
            session.execute(createFunctionCQL);

            String createMaterialized = "CREATE MATERIALIZED VIEW mv AS" +
                    "  SELECT * FROM t WHERE k < test.\"Double\"(2)" +
                    "  AND k IS NOT NULL" +
                    "  AND v IS NOT NULL" +
                    "  PRIMARY KEY (v, k);";
            session.execute(createMaterialized);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            session.close();
            cluster.close();
        }
    }

    private static void runAnInsertSQL(){
        Cluster cluster = null;
        Session session = null;
        try {
            cluster = Cluster.builder().addContactPoint(runner.runtime().ip("server1")).build();
            session = cluster.connect();
            session.execute("use test");

            String createKeySpaceCQL = "INSERT INTO t(k, v) VALUES (3, 1)";
            session.execute(createKeySpaceCQL);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            session.close();
            cluster.close();
        }
    }

}
