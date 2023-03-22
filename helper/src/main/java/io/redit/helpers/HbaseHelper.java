package io.redit.helpers;

import io.redit.ReditRunner;
import io.redit.exceptions.RuntimeEngineException;
import io.redit.execution.CommandResults;
import org.slf4j.Logger;
import java.io.*;
import java.util.ArrayList;

public class HbaseHelper {
    private final ReditRunner runner;
    private final String homeDir;
    private final Logger logger;
    private final ArrayList<Object> RWs;
    private final int numOfServers;
    public String HbaseSiteConf = "";
    private String RegionConf = "";

    public HbaseHelper(ReditRunner runner, String homeDir, Logger logger, ArrayList<Object> RWs, int numOfServers) {
        this.runner = runner;
        this.homeDir = homeDir;
        this.logger = logger;
        this.RWs = RWs;
        this.numOfServers = numOfServers;
        getConfig();
    }
    private void getConfig(){
        for(int i = 1; i <= numOfServers; i++){
            HbaseSiteConf += runner.runtime().ip("server" + (i)) + ":2181";
            RegionConf += runner.runtime().ip("server" + (i));
            if (i < numOfServers){
                HbaseSiteConf += ",";
                RegionConf += "\n";
            }
        }
    }

    public void startHbases() {
        for(int i = 1; i <= numOfServers; i++){
            startHbase(i);
        }
    }

    public void startHbase(int serverId) {
        String command = "cd " + homeDir + " && bin/hbase-daemon.sh start master && bin/hbase-daemon.sh start regionserver";
        logger.info("server" + serverId + " startHbase...");
        new Thread(() -> {
            try {
                runner.runtime().runCommandInNode("server" + serverId, command);
            } catch (RuntimeEngineException e) {
                e.printStackTrace();
            }
        }).start();
    }

    public void startSsh() throws RuntimeEngineException {
        for(int i = 1; i <= numOfServers; i++){
            CommandResults commandResults = runner.runtime().runCommandInNode("server" + i, "service ssh start");
            Utils.printResult(commandResults, logger);
        }
    }

    public void addRegionConf() throws IOException {
        boolean addConf = false;
        BufferedReader in = new BufferedReader((Reader) RWs.get(0));
        String str;
        while ((str = in.readLine()) != null) {
            if (str.startsWith("10.")){
                addConf = true;
            }
        }
        in.close();
        if (addConf){
            logger.info("regionservers is already configured !!!");
        }
        else {
            BufferedWriter out = new BufferedWriter((Writer) RWs.get(1));
            out.write(RegionConf);
            out.close();
            logger.info("add config to regionservers !!!");
        }
    }

    public void checkJps() throws RuntimeEngineException {
        for (int i = 1; i <= numOfServers; i++) {
            CommandResults commandResults = runner.runtime().runCommandInNode("server" + i, "jps");
            Utils.printResult(commandResults, logger);
        }
    }
}