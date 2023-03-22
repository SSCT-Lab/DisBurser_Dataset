package io.redit.helpers;

import io.redit.ReditRunner;
import io.redit.exceptions.RuntimeEngineException;
import io.redit.execution.CommandResults;
import org.slf4j.Logger;

public class ActivemqHelper{
    private final ReditRunner runner;
    private final String[] homeDir;
    private final Logger logger;
    private final int numOfServers;
    public String ACTIVEMQ_URL1 = "tcp://";
    public String ACTIVEMQ_URL2 = "tcp://";

    public ActivemqHelper(ReditRunner runner, String[] homeDir, Logger logger, int numOfServers) {
        this.runner = runner;
        this.homeDir = homeDir;
        this.logger = logger;
        this.numOfServers = numOfServers;
        ACTIVEMQ_URL1 += runner.runtime().ip("server1") + ":61616";
        ACTIVEMQ_URL2 += runner.runtime().ip("server2") + ":61616";
    }

    public void startServers() throws InterruptedException, RuntimeEngineException {
        for (int i = 1; i <= numOfServers; i++) {
            startServer(i);
        }
    }

    public void startServer(int serverId) throws RuntimeEngineException {
        logger.info("server" + serverId + " startServer...");
        for(int i = 0; i <= numOfServers; i++){
            String command = "cd " + homeDir[i] + " && bin/activemq start";
            runner.runtime().runCommandInNode("server" + serverId, command);
        }
    }

    public void checkServers() throws RuntimeEngineException {
        for (int i = 1; i <= numOfServers; i++) {
            checkServer(i);
        }
    }

    public void checkServer(int serverId) throws RuntimeEngineException {
        logger.info("server" + serverId + " checkServer...");
        for(int i = 0; i <= numOfServers; i++){
            String command = "cd " + homeDir[i] + " && bin/activemq status";
            CommandResults commandResult = runner.runtime().runCommandInNode("server" + serverId, command);
            Utils.printResult(commandResult, logger);
        }
    }
}
