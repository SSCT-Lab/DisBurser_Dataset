#include <stdio.h>
#include <stdlib.h>
int main()
{
#ifdef ZK_2052
    system("cp ./buggy/PrepRequestProcessor.java ./zookeeper-3.5.0-src/src/java/main/org/apache/zookeeper/server/");
    printf("inject ZK_2052 ...\n");
#else
    system("cp ./fixed/PrepRequestProcessor.java ./zookeeper-3.5.0-src/src/java/main/org/apache/zookeeper/server/");
    printf("don't inject ZK_2052 ...\n");
#endif


#ifdef ZK_2212
    system("cp ./buggy/FastLeaderElection.java ./zookeeper-3.5.0-src/src/java/main/org/apache/zookeeper/server/quorum/");
    printf("inject ZK_2212 ...\n");
#else
    system("cp ./fixed/FastLeaderElection.java ./zookeeper-3.5.0-src/src/java/main/org/apache/zookeeper/server/quorum/");
    printf("don't inject ZK_2212 ...\n");
#endif


#ifdef ZK_2323
    system("cp ./buggy/ZooKeeperSaslClient.java ./zookeeper-3.5.0-src/src/java/main/org/apache/zookeeper/client/");
    printf("inject ZK_2323 ...\n");
#else
    system("cp ./fixed/ZooKeeperSaslClient.java ./zookeeper-3.5.0-src/src/java/main/org/apache/zookeeper/client/");
    printf("don't inject ZK_2323 ...\n");
#endif
}

