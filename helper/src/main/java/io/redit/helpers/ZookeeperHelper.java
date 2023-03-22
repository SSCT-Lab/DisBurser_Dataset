package io.redit.helpers;

import io.redit.ReditRunner;
import io.redit.exceptions.RuntimeEngineException;
import io.redit.execution.CommandResults;
import org.slf4j.Logger;

import java.io.*;
import java.util.ArrayList;

public class ZookeeperHelper {
    private final ReditRunner runner;
    private final String homeDir;
    private final Logger logger;
    private final ArrayList<Object> RWs;
    private final int numOfServers;
    public String connectionStr;
    public String serverConf = "";
    public int leaderId;

    public ZookeeperHelper(ReditRunner runner, String homeDir, Logger logger, ArrayList<Object> RWs, int numOfServers) {
        this.runner = runner;
        this.homeDir = homeDir;
        this.logger = logger;
        this.RWs = RWs;
        this.numOfServers = numOfServers;
        this.connectionStr = runner.runtime().ip("server1") + ":2181," + runner.runtime().ip("server2") + ":2181," + runner.runtime().ip("server3") + ":2181";
        for(int i = 1; i <= numOfServers; i++){
            this.serverConf += "server." + i + "=" + runner.runtime().ip("server" + (i)) + ":2888:3888\n";
        }
    }

    public void startServers() {
        for(int i = 1; i <= numOfServers; i++){
            startServer(i);
        }
    }

    public void checkServersStatus() throws RuntimeEngineException {
        for(int i = 1; i <= numOfServers; i++){
            String command = "cd " + homeDir + " && bin/zkServer.sh status";
            logger.info("server" + i + " checkStatus...");
            CommandResults commandResults = runner.runtime().runCommandInNode("server" + i, command);
            Utils.printResult(commandResults, logger);
            if (commandResults.stdOut().indexOf("leader") != -1){
                leaderId = i;
            }
        }
    }

    public void checkServerStatus(int serverId) throws RuntimeEngineException {
        String command = "cd " + homeDir + " && bin/zkServer.sh status";
        logger.info("server" + serverId + " checkStatus...");
        CommandResults commandResults = runner.runtime().runCommandInNode("server" + serverId, command);
        Utils.printResult(commandResults, logger);
    }

    public void startServer(int serverId) {
        new Thread(() -> {
            String command = "cd " + homeDir + " && bin/zkServer.sh start";
            logger.info("server" + serverId + " startServer...");
            try {
                runner.runtime().runCommandInNode("server" + serverId, command);
            } catch (RuntimeEngineException e) {
                e.printStackTrace();
            }
        }).start();
    }

    public void addConfFile() throws IOException {
        boolean addConf = false;
        BufferedReader in = new BufferedReader((FileReader) RWs.get(0));
        String str;
        while ((str = in.readLine()) != null) {
            if (str.startsWith("server")){
                addConf = true;
            }
        }
        in.close();
        if (addConf){
            logger.info("zoo.cfg is already configured !!!");
        }
        else {
            BufferedWriter out = new BufferedWriter((FileWriter) RWs.get(1));
            out.write(serverConf);
            out.close();
            logger.info("add config to zoo.cfg !!!");
        }
    }
}
