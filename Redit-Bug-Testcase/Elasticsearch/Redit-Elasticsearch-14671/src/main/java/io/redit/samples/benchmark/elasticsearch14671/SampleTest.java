package io.redit.samples.benchmark.elasticsearch14671;

import io.redit.ReditRunner;
import io.redit.exceptions.RuntimeEngineException;
import io.redit.execution.NetPart;
import io.redit.helpers.ElasticsearchHelper;
import org.apache.http.HttpHost;
import org.apache.http.util.EntityUtils;
import org.elasticsearch.client.Request;
import org.elasticsearch.client.Response;
import org.elasticsearch.client.RestClient;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeoutException;

public class SampleTest {
    private static final Logger logger = LoggerFactory.getLogger(SampleTest.class);
    protected static ReditRunner runner;
    private static ElasticsearchHelper helper;
    private String masterServer;
    private Map<String, RestClient> clients;
    private final Request createIndexReq = new Request("PUT", "/foo?pretty");
    private Request createDocReq = new Request("PUT", "/foo/bar/1?pretty");
    private Request updateDocReq = new Request("POST", "/foo/bar/1/_update?pretty");
    private final Request queryShardReq = new Request("GET", "/_cat/shards");
    private final Request queryDocReq = new Request("GET", "/foo/bar/1?pretty");
    private RestClient masterClient;
    private RestClient primaryShardClient;
    private RestClient replicaShardClient;
    private String primaryShardServerName;
    private String replicaShardServerName;
    private NetPart netPart;

    @BeforeClass
    public static void before() throws RuntimeEngineException, IOException, InterruptedException {
        runner = ReditRunner.run(ReditHelper.getDeployment());
        ReditHelper.startNodes(runner);
        helper = new ElasticsearchHelper(runner, ReditHelper.getElasticsearchHomeDir(), logger, ReditHelper.getFileRW(), ReditHelper.numOfServers);
        helper.addElasticsearchYmlFile();

        helper.startServers();
        helper.checkJps();
    }

    @AfterClass
    public static void after() {
        if (runner != null) {
            runner.stop();
        }
    }

    @Test
    public void testStaleReplicasToBePromotedToPrimary() throws InterruptedException, RuntimeEngineException, TimeoutException {
        runner.runtime().enforceOrder("E1", () -> {
            helper.checkElasticsearchStatus();
        });

        runner.runtime().enforceOrder("E2", () -> {
            createLowLevelRestClients();
        });

        runner.runtime().enforceOrder("E3", () -> {
            try {
                refreshMasterServer();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });

        // 创建index和一个document
        runner.runtime().enforceOrder("E4", () -> {
            createDocReq.setJsonEntity("{ \"value\": \"origin\" }");
            updateDocReq.setJsonEntity("{\"doc\": { \"value\": \"something else\" }}");
            masterClient = this.clients.get(this.masterServer);
            try {
                doRequest(masterClient, createIndexReq);
                doRequest(masterClient, createDocReq);
                Thread.sleep(5000);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });

        // 筛选出主分片所在server
        runner.runtime().enforceOrder("E5", () -> {
            String shardStr;
            try {
                shardStr = doRequest(masterClient, queryShardReq);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            String[] shardStrList = shardStr.split("\n");
            for (String shardInfo : shardStrList) {
                String[] shardInfoList = shardInfo.trim().split(" ");
                String shardNodeName = shardInfoList[shardInfoList.length - 1];
                String shardIpAddress = shardInfoList[shardInfoList.length - 2];
                String curServerName = "server" + shardNodeName.substring(shardNodeName.length() - 1);
                if (shardInfo.contains(" p ")) {
                    primaryShardServerName = curServerName;
                    primaryShardClient = clients.get(primaryShardServerName);
                    logger.info(String.format("PrimaryShardServer: %s; Ip address: %s", primaryShardServerName, shardIpAddress));
                } else {
                    replicaShardServerName = curServerName;
                    replicaShardClient = clients.get(replicaShardServerName);
                    logger.info(String.format("ReplicaShardServer: %s; Ip address: %s", replicaShardServerName, shardIpAddress));
                }
            }
            assert primaryShardServerName != null && primaryShardServerName.length() != 0;
            assert replicaShardServerName != null && replicaShardServerName.length() != 0;
        });

        // 网络分区，隔离primaryShardServer和其余两个node
        runner.runtime().enforceOrder("X1", () -> {
            logger.info("--> partitioning node with primary shard from rest of cluster");
            netPart = NetPart.partitions(primaryShardServerName, this.masterServer + "," + replicaShardServerName).build();
            runner.runtime().networkPartition(netPart);
        });
        Thread.sleep(5000);

        // 在和master相连的replicaShardServer上更新文档，并查询更新后的值
        runner.runtime().enforceOrder("E6", () -> {
            logger.info("--> index a document into previous replica shard (that is now primary)");
            try {
                doRequest(replicaShardClient, updateDocReq);
                doRequest(replicaShardClient, queryDocReq);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });

        // 关闭replicaShardServer
        runner.runtime().enforceOrder("X2", () -> {
            logger.info("--> shut down node that has new acknowledged document");
            runner.runtime().killNode(replicaShardServerName);
        });
        Thread.sleep(1000);

        // 撤销网络分区
        runner.runtime().enforceOrder("X3", () -> {
            runner.runtime().removeNetworkPartition(netPart);
            logger.info("--> waiting for node with old primary shard to rejoin the cluster");
        });
        Thread.sleep(5000);

        // 检查有着过时数据的primaryShardServer是否重新被分配上了主分片
        runner.runtime().enforceOrder("E7", () -> {
            try {
                logger.info("--> check that old primary shard get promoted to primary again");
                for (int i = 0; i < 10; i++) {
                    String queryShardRes = doRequest(primaryShardClient, queryShardReq);
                    if(queryShardRes.contains("p STARTED")) break;
                    Thread.sleep(5000);
                }
                doRequest(masterClient, queryDocReq);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });

        runner.runtime().waitForRunSequenceCompletion(20);
        logger.info("completed !!!");
    }

    private void createLowLevelRestClients() {
        this.clients = new HashMap<>();
        for (int i = 1; i <= ReditHelper.numOfServers; i++) {
            this.clients.put("server" + i, RestClient.builder(new HttpHost(runner.runtime().ip("server" + i), 9200, "http")).build());
        }
    }

    private void refreshMasterServer() throws IOException {
        Request queryMasterReq = new Request("GET", "/_cat/master");
        String queryMasterRes = doRequest(clients.get("server1"), queryMasterReq);
        assert queryMasterRes != null && queryMasterRes.length() != 0;
        String[] commandResList = queryMasterRes.split("\\s");
        for (String s : commandResList) {
            if (s.startsWith("server")) {
                this.masterServer = s;
                logger.info("Current master: " + s + ". Ip address: " + runner.runtime().ip(s));
            }
        }
    }

    private String doRequest(RestClient restClient, Request request) throws IOException {
        Response response = restClient.performRequest(request);
        int statusCode = response.getStatusLine().getStatusCode();
        HttpHost responseHost = response.getHost();
        String responseBody = EntityUtils.toString(response.getEntity());
        if (!String.valueOf(statusCode).startsWith("2")) {
            logger.error(String.format("request to %s fails with code %d", responseHost.toHostString(), statusCode));
            logger.error("\n" + responseBody);
        } else {
            logger.info(String.format("request to %s successes with code %d", responseHost.toHostString(), statusCode));
            logger.info("\n" + responseBody);
        }
        return responseBody;
    }
}