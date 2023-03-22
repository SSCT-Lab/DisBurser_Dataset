package io.redit.helpers;

import io.redit.ReditRunner;
import io.redit.exceptions.RuntimeEngineException;
import io.redit.execution.CommandResults;
import org.slf4j.Logger;

import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class KafkaHelper {
    private final ReditRunner runner;
    private final String homeDir;
    private final Logger logger;
    private final ArrayList<Object> RWs;
    private final int numOfServers;
    public String KafkaPropConf = "zookeeper.connect=";
    public String BOOTSTRAP_SERVERS = "";
    public int kafkaControllerId = -1;
    public ArrayList<String> kafkaAliveServerIds = new ArrayList<>(Arrays.asList("1", "2", "3"));

    public KafkaHelper(ReditRunner runner, String homeDir, Logger logger, ArrayList<Object> RWs, int numOfServers, String addConf) {
        this.runner = runner;
        this.homeDir = homeDir;
        this.logger = logger;
        this.RWs = RWs;
        this.numOfServers = numOfServers;
        for(int i = 1; i <= numOfServers; i++){
            KafkaPropConf += runner.runtime().ip("server" + (i)) + ":2181" + addConf;
            if (i < numOfServers){
                KafkaPropConf += ",";
            }
        }
        BOOTSTRAP_SERVERS = runner.runtime().ip("server1") + ":9092," + runner.runtime().ip("server2") + ":9092," + runner.runtime().ip("server3") + ":9092";
    }

    public void startKafkas() {
        for(int i = 1; i <= numOfServers; i++){
            startKafka(i);
        }
    }

    public void startKafka(int serverId) {
        String command = "cd " + homeDir + " && bin/kafka-server-start.sh -daemon ./config/server.properties";
        logger.info("server" + serverId + " startKafka...");
        new Thread(() -> {
            try {
                runner.runtime().runCommandInNode("server" + serverId, command);
            } catch (RuntimeEngineException e) {
                e.printStackTrace();
            }
        }).start();
    }

    public void startBroker(int serverId) throws RuntimeEngineException {
        logger.info("============ start kafka service on server " + serverId + " ============");
        kafkaAliveServerIds.add(String.valueOf(serverId));
        String command = "cd " + homeDir + " && bin/kafka-server-start.sh -daemon ./config/server.properties";
        runner.runtime().runCommandInNode("server" + serverId, command);
    }

    public void shutdownBrokers() throws RuntimeEngineException {
        for(int i = 1; i <= numOfServers; i++){
            shutdownBroker(i);
        }
    }

    public void shutdownBroker(int serverId) throws RuntimeEngineException {
        logger.info("============ shutdown kafka service on server " + serverId + " ============");
        kafkaAliveServerIds.remove(String.valueOf(serverId));
        String command = "cd " + homeDir + " && bin/kafka-server-stop.sh";
        runner.runtime().runCommandInNode("server" + serverId, command);
    }

    public void createTopic(int serverId, String topicName) throws RuntimeEngineException {
        String dockerName = "server" + serverId;
        String command = "cd " + homeDir + " && bin/kafka-topics.sh --bootstrap-server " + BOOTSTRAP_SERVERS + " --create --replication-factor 3 --partitions 2 --topic " + topicName;
        CommandResults commandResults = runner.runtime().runCommandInNode(dockerName, command);
        Utils.printResult(commandResults, logger);
    }

    public void formatStorageLog(int serverId, String uuid) throws RuntimeEngineException {
        String command = "cd " + homeDir + " && bin/kafka-storage.sh format -t " + uuid + " -c  ./config/server.properties";
        CommandResults commandResults = runner.runtime().runCommandInNode("server" + serverId, command);
        Utils.printResult(commandResults, logger);
    }

    public void findController(){
        List<String> commands= new ArrayList<>();
        String id = kafkaAliveServerIds.get(0);
        commands.add("kafkacat -b " + runner.runtime().ip("server" + id) + ":9092 -L");
        kafkaControllerId = executeNewFlow(commands);
        logger.info("kafkaControllerId: " + kafkaControllerId);
    }

    //服务器执行命令行方法
    public int executeNewFlow(List<String> commands) {
        int brokerId = -1;
        Runtime run = Runtime.getRuntime();
        try {
            Process proc = run.exec("/bin/bash", null, null);
            BufferedReader in = new BufferedReader(new InputStreamReader(proc.getInputStream()));
            PrintWriter out = new PrintWriter(new BufferedWriter(new OutputStreamWriter(proc.getOutputStream())), true);
            for (String line : commands) {
                out.println(line);
            }
            out.println("exit");// 这个命令必须执行，否则in流不结束。
            String rspLine = "";
            logger.info("============ kafkacat :" + commands + "============");
            while ((rspLine = in.readLine()) != null) {
                logger.info(rspLine);
                if(rspLine.indexOf("controller") != -1){
                    int index = rspLine.indexOf("broker") + 7;
                    brokerId = Integer.parseInt(rspLine.substring(index, index+1));
                }
            }
            proc.waitFor();
            in.close();
            out.close();
            proc.destroy();
        } catch (IOException e1) {
            e1.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return brokerId;
    }

    public void addKafkaPropFile() throws IOException {
        boolean addConf = false;
        BufferedReader in = new BufferedReader((Reader) RWs.get(0));
        String str;
        while ((str = in.readLine()) != null) {
            if (str.startsWith("zookeeper.connect=")){
                addConf = true;
            }
        }
        in.close();
        if (addConf){
            logger.info("server.properties is already configured !!!");
        }
        else {
            for (int i = 1; i <= numOfServers; i++){
                BufferedWriter out = new BufferedWriter((Writer) RWs.get(i));
                String listenerConf = "listeners = PLAINTEXT://" + runner.runtime().ip("server" + i) + ":9092\n";
                out.write(listenerConf);
                out.write(KafkaPropConf);
                out.close();
                logger.info("add config to server.properties !!!");
            }
        }
    }

    public void checkJps() throws RuntimeEngineException {
        for(int i = 1; i <= numOfServers; i++){
            CommandResults commandResults = runner.runtime().runCommandInNode("server" + i, "jps");
            Utils.printResult(commandResults, logger);
        }
    }
}

