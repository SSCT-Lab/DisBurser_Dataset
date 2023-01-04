package io.redit.samples.zookeeper4491;

import io.redit.ReditRunner;
import io.redit.dsl.entities.Deployment;
import io.redit.dsl.entities.PathAttr;
import io.redit.dsl.entities.ServiceType;
import io.redit.exceptions.RuntimeEngineException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ReditHelper {
    public static final Logger logger = LoggerFactory.getLogger(ReditHelper.class);
    public static int numOfServers = 3;
    public static final int HTTP_PORT = 2181;

    public static String getHomeDir(){
        return "/zookeeper/apache-zookeeper-3.7.1-bin";
    }

    public static Deployment getDeployment() {

        String workDir = System.getProperty("user.dir");
        String dir = "apache-zookeeper-3.7.1-bin";
        Deployment.Builder builder = Deployment.builder("sample-zookeeper")
                .withService("zookeeper")
                .applicationPath(workDir + "/../Benchmark/zookeeper-3.7.1/zookeeper-3.7.1-build/zookeeper-dist/target/" + dir + ".tar.gz", "/zookeeper",  PathAttr.COMPRESSED)
                .applicationPath("conf/zoo.cfg", getHomeDir() + "/conf/zoo.cfg")
                .dockerImageName("mengpo1106/zookeeper:3.7.1").dockerFileAddress("docker/Dockerfile", true)
                .libraryPath(getHomeDir() + "/bin/*.sh")
                .libraryPath(getHomeDir() + "/lib/*.jar")
                .logDirectory(getHomeDir() + "/logs").serviceType(ServiceType.JAVA).and();

        builder.withService("server", "zookeeper").tcpPort(HTTP_PORT).and()
                .nodeInstances(numOfServers, "server", "server", true)
                .node("server1").applicationPath("conf/server1/myid", getHomeDir() + "/zkdata/myid").and()
                .node("server2").applicationPath("conf/server2/myid", getHomeDir() + "/zkdata/myid").and()
                .node("server3").applicationPath("conf/server3/myid", getHomeDir() + "/zkdata/myid").and();

        return builder.build();
    }

    public static void startNodesInOrder(ReditRunner runner) throws RuntimeEngineException {
        try {
            runner.runtime().startNode("server1");
            Thread.sleep(1000);
            if (numOfServers > 1) {
                for (int Index = 2; Index <= numOfServers; Index++) {
                    runner.runtime().startNode("server" + Index);
                }
            }
            for (String node : runner.runtime().nodeNames())
                if (node.startsWith("client")) runner.runtime().startNode(node);
        } catch (InterruptedException e) {
            logger.warn("startNodesInOrder sleep got interrupted");
        }
    }
}
