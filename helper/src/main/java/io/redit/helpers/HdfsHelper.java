package io.redit.helpers;

import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import io.redit.ReditRunner;
import io.redit.dsl.entities.PortType;
import io.redit.exceptions.RuntimeEngineException;
import io.redit.execution.CommandResults;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.hdfs.DistributedFileSystem;
import org.slf4j.Logger;

import java.io.IOException;
import java.util.StringJoiner;

public class HdfsHelper {
    private final ReditRunner runner;
    private final String homeDir;
    private final Logger logger;
    private final int numOfServers;
    private final String CLUSTER_NAME = "mycluster";
    private static final int NN_HTTP_PORT = 50070;
    private static final int NN_RPC_PORT = 8020;

    public HdfsHelper(ReditRunner runner, String homeDir, Logger logger, int numOfServers) {
        this.runner = runner;
        this.homeDir = homeDir;
        this.logger = logger;
        this.numOfServers = numOfServers;
    }

    public DistributedFileSystem getDFS(ReditRunner runner) throws IOException {
        FileSystem fs = FileSystem.get(getConfiguration(runner));
        if (!(fs instanceof DistributedFileSystem)) {
            throw new IllegalArgumentException("FileSystem " + fs.getUri() + " is not an HDFS file system");
        } else {
            return (DistributedFileSystem)fs;
        }
    }

    public Configuration getConfiguration(ReditRunner runner) {
        Configuration conf = new Configuration();
        conf.set("fs.defaultFS", "hdfs://" + CLUSTER_NAME);
        conf.set("dfs.client.failover.proxy.provider."+ CLUSTER_NAME,
                "org.apache.hadoop.hdfs.server.namenode.ha.ConfiguredFailoverProxyProvider");
        conf.set("dfs.nameservices", CLUSTER_NAME);
        conf.set("dfs.ha.namenodes."+ CLUSTER_NAME, getNNString());

        for (int i = 1; i <= numOfServers; i++) {
            String nnIp = runner.runtime().ip("nn" + i);
            conf.set("dfs.namenode.rpc-address."+ CLUSTER_NAME +".nn" + i, nnIp + ":" +
                    runner.runtime().portMapping("nn" + i, NN_RPC_PORT, PortType.TCP));
            conf.set("dfs.namenode.http-address."+ CLUSTER_NAME +".nn" + i, nnIp + ":" +
                    runner.runtime().portMapping("nn" + i, NN_HTTP_PORT, PortType.TCP));
        }
        return conf;
    }

    private String getNNString() {
        StringJoiner stringJoiner = new StringJoiner(",");
        for (int i = 1; i <= numOfServers; i++) {
            stringJoiner.add("nn" + i);
        }
        return stringJoiner.toString();
    }

    public void transitionToActive(int nnNum, ReditRunner runner) throws RuntimeEngineException {
        logger.info("Transitioning nn{} to ACTIVE", nnNum);
        CommandResults res = runner.runtime().runCommandInNode("nn" + nnNum, homeDir + "/bin/hdfs haadmin -transitionToActive nn" + nnNum);
        if (res.exitCode() != 0) {
            throw new RuntimeException("Error while transitioning nn" + nnNum + " to ACTIVE.\n" + res.stdErr());
        }
    }

    public void checkNNs(ReditRunner runner) throws RuntimeEngineException {
        logger.info("start check NNs !!!");
        for(int nnNum = 1; nnNum <= numOfServers; nnNum++){
            CommandResults res = runner.runtime().runCommandInNode("nn" + nnNum, homeDir + "/bin/hdfs haadmin -getServiceState nn" + nnNum);
            logger.info("nn" + nnNum + " status: " + res.stdOut());
        }
    }

    public void checkJps() throws RuntimeEngineException {
        for (int i = 1; i <= numOfServers; i++) {
            CommandResults commandResults = runner.runtime().runCommandInNode("server" + i, "jps");
            printResult(commandResults);
        }
    }

    public void printResult(CommandResults commandResults){
        logger.info(commandResults.nodeName() + ": " + commandResults.command());
        if (commandResults.stdOut() != null){
            logger.info(commandResults.stdOut());
        }else {
            logger.warn(commandResults.stdErr());
        }
    }


    public void waitActive() throws RuntimeEngineException {
        for (int index=1; index<= 3; index++) {
            for (int retry=5; retry>0; retry--){
                logger.info("Checking if NN nn{} is UP (retries left {})", index, retry-1);
                if (assertNNisUpAndReceivingReport(index, 3))
                    break;
                if (retry > 1) {
                    try {
                        Thread.sleep(5000);
                    } catch (InterruptedException e) {
                        logger.warn("waitActive sleep got interrupted");
                    }
                } else {
                    throw new RuntimeException("NN nn" + index + " is not active or not receiving reports from DNs");
                }
            }
        }
        logger.info("The cluster is ACTIVE");
    }

    private boolean assertNNisUpAndReceivingReport(int index, int numOfDNs) throws RuntimeEngineException {
        if (!isNNUp(index))
            return false;

        String res = getNNJmxHaInfo(index);
        if (res == null) {
            logger.warn("Error while trying to get the status of name node");
            return false;
        }

        logger.info("NN {} is up. Checking datanode connections", "nn" + index);
        return res.contains("\"NumLiveDataNodes\" : " + numOfDNs);
    }

    private boolean isNNUp(int index) throws RuntimeEngineException {
        String res = getNNJmxHaInfo(index);
        if (res == null) {
            logger.warn("Error while trying to get the status of name node");
            return false;
        }
        return res.contains("\"tag.HAState\" : \"active\"") || res.contains("\"tag.HAState\" : \"standby\"");
    }

    private String getNNJmxHaInfo(int index) {
        OkHttpClient client = new OkHttpClient();
        try {
            return client.newCall(new Request.Builder()
                    .url("http://" + runner.runtime().ip("nn" + index) + ":" + NN_HTTP_PORT +
                            "/jmx?qry=Hadoop:service=NameNode,name=FSNamesystem")
                    .build()).execute().body().string();
        } catch (IOException e) {
            logger.warn("Error while trying to get the status of name node");
            return null;
        }
    }
}
