package io.redit.helpers;

import io.redit.ReditRunner;
import io.redit.exceptions.RuntimeEngineException;
import io.redit.execution.CommandResults;
import org.slf4j.Logger;

import java.io.*;
import java.util.ArrayList;

public class RocketmqHelper {
    private final ReditRunner runner;
    private final String homeDir;
    private final Logger logger;
    private final ArrayList<Object> RWs;
    private final int numOfServers;
    public String namesrvAddr = "";

    public RocketmqHelper(ReditRunner runner, String homeDir, Logger logger, ArrayList<Object> RWs, int numOfServers) {
        this.runner = runner;
        this.homeDir = homeDir;
        this.logger = logger;
        this.RWs = RWs;
        this.numOfServers = numOfServers;
        this.namesrvAddr = runner.runtime().ip("server1") + ":9876;" + runner.runtime().ip("server2") + ":9876";
    }

    public void givePermission() throws RuntimeEngineException {
        for (int i = 1; i <= numOfServers; i++){
            String command = "cd " + homeDir + " && chmod +x bin/*";
            logger.info("server" + i + " give permission ...");
            runner.runtime().runCommandInNode("server" + i, command);
        }
    }

    public void startServers() throws InterruptedException {
        for(int i = 1; i <= numOfServers; i++){
            startServer(i);
            Thread.sleep(1000);
        }
    }

    public void checkStatus(int serverId) throws RuntimeEngineException {
        String command = "cd " + homeDir + " && bin/mqadmin  clusterList -n " + runner.runtime().ip("server" + serverId) + ":9876";
        logger.info("server" + serverId + " checkStatus ...");
        CommandResults commandResults = runner.runtime().runCommandInNode("server" + serverId, command);
        Utils.printResult(commandResults, logger);
    }


    public void startServer(int serverId) {
        String command = "cd " + homeDir + " && bin/mqnamesrv > ./logs/mqnamesrv.log 2>&1";
        logger.info("server" + serverId + " startServer ...");
        new Thread(() -> {
            try {
                runner.runtime().runCommandInNode("server" + serverId, command);
            } catch (RuntimeEngineException e) {
                e.printStackTrace();
            }
        }).start();
    }

    public void startBroker(int serverId, String broker_id) {
        String command = "cd " + homeDir + " && bin/mqbroker -c ./conf/2m-2s-async/broker-" + broker_id + ".properties > ./logs/broker-" + broker_id + ".log 2>&1";
        logger.info("server" + serverId + " startBroker, broker_id: " + broker_id);
        new Thread(() -> {
            try {
                runner.runtime().runCommandInNode("server" + serverId, command);
            } catch (RuntimeEngineException e) {
                e.printStackTrace();
            }
        }).start();
    }

    public void addRocketPropFile() throws IOException {
        boolean addConf = false;
        BufferedReader in = new BufferedReader((Reader) RWs.get(0));
        String str;
        while ((str = in.readLine()) != null) {
            if (str.startsWith("namesrvAddr=")){
                addConf = true;
            }
        }
        in.close();
        if (addConf){
            logger.info("broker-x.properties is already configured !!!");
        }
        else {
            for (int i = 1; i < RWs.size(); i++){
                BufferedWriter out = new BufferedWriter((Writer) RWs.get(i));
                out.write("namesrvAddr=" + namesrvAddr);
                out.close();
            }
            logger.info("add config to broker-x.properties !!!");
        }
    }
}
