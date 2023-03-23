package io.redit.samples.cassandra12424;

import com.datastax.driver.core.Cluster;
import com.datastax.driver.core.ResultSet;
import com.datastax.driver.core.Row;
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
import java.util.List;
import java.util.concurrent.TimeoutException;
public class SampleTest {
    private static final Logger logger = LoggerFactory.getLogger(SampleTest.class);
    private static ReditRunner runner;
    private static CassandraHelper helper;
    private static Cluster cluster;
    private static Session session;

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
            session.close();
            cluster.close();
            runner.stop();
        }
    }

    @Test
    public void testAssertionFailureInViewUpdateGenerator() throws RuntimeEngineException, InterruptedException, TimeoutException {
        runner.runtime().enforceOrder("E1", () -> {
            createTable();
        });

        runner.runtime().enforceOrder("E2", () -> {
            createMaterializedView();
        });

        runner.runtime().enforceOrder("E3", () -> {
            insertTable();
        });

        runner.runtime().enforceOrder("E4", () -> {
            selectShow();
        });

        runner.runtime().enforceOrder("E5", () -> {
            updateTable();
        });

        runner.runtime().enforceOrder("E6", () -> {
            updateAnotherField();
        });

        runner.runtime().waitForRunSequenceCompletion(20);
        logger.info("completed !!!");
    }

    private static void createTable(){
        try {
            cluster = Cluster.builder().addContactPoint(runner.runtime().ip("server1")).build();
            session = cluster.connect();

            String createKeySpaceCQL = "CREATE KEYSPACE likes WITH durable_writes = true AND replication = {" +
                    "'class' : 'SimpleStrategy', 'replication_factor' : 2};";
            session.execute(createKeySpaceCQL);

            String createTableCQL = "CREATE TABLE likes.like (user_id int, user_id_contact int, acknowledged int," +
                    " last_contact timestamp, visible int, PRIMARY KEY (user_id, user_id_contact))";
            session.execute(createTableCQL);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void createMaterializedView(){
        String createMaterializedViewCQL = "CREATE MATERIALIZED VIEW likes.like_valid_mv AS" +
                " SELECT user_id, last_contact, user_id_contact, acknowledged, visible" +
                " FROM like WHERE user_id IS NOT NULL AND last_contact IS NOT NULL AND user_id_contact IS NOT NULL" +
                " PRIMARY KEY ( user_id, last_contact, user_id_contact )" +
                " WITH CLUSTERING ORDER BY ( last_contact DESC, user_id_contact ASC )";
        session.execute(createMaterializedViewCQL);
    }

    private static void insertTable(){
        String insertTableCQL = "INSERT INTO likes.like (user_id, user_id_contact, last_contact, acknowledged)" +
                " VALUES (34024269, 10635693, '2015-07-17', 2);";
        session.execute(insertTableCQL);
    }

    private static void selectShow(){
        String selectShowCQL = "SELECT * FROM likes.like WHERE user_id = 34024269 AND user_id_contact = 10635693;";
        ResultSet rs = session.execute(selectShowCQL);
        List<Row> dataList = rs.all();
        for (Row row : dataList) {
            System.out.println(row.toString());
        }
    }

    private static void updateTable(){
        String updateTableCQL = "UPDATE likes.like SET last_contact = null WHERE user_id = 34024269 AND user_id_contact = 10635693;";
        session.execute(updateTableCQL);
    }

    private static void updateAnotherField(){
        String updateAnotherFieldCQL = "UPDATE likes.like SET acknowledged = 2 WHERE user_id = 34024269 AND user_id_contact = 10635693;";
        session.execute(updateAnotherFieldCQL);
    }
}