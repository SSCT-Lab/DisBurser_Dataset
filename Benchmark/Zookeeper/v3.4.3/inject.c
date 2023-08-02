#include <stdio.h>
#include <stdlib.h>
int main()
{
#ifdef ZK_1419
    system("cp ./buggy/FastLeaderElection.java ./zookeeper-3.4.3-src/src/java/main/org/apache/zookeeper/server/quorum/");
    printf("inject ZK_1419 ...\n");
#else
    system("cp ./fixed/FastLeaderElection.java ./zookeeper-3.4.3-src/src/java/main/org/apache/zookeeper/server/quorum/");
    printf("don't inject ZK_1419 ...\n");
#endif


#ifdef ZK_1489
    system("cp ./buggy/ZKDatabase.java ./zookeeper-3.4.3-src/src/java/main/org/apache/zookeeper/server/");
    system("cp ./buggy/FileTxnSnapLog.java ./zookeeper-3.4.3-src/src/java/main/org/apache/zookeeper/server/persistence/");
    printf("inject ZK_1489 ...\n");
#else
    system("cp ./fixed/ZKDatabase.java ./zookeeper-3.4.3-src/src/java/main/org/apache/zookeeper/server/");
    system("cp ./fixed/FileTxnSnapLog.java ./zookeeper-3.4.3-src/src/java/main/org/apache/zookeeper/server/persistence/");
    printf("don't inject ZK_1489 ...\n");
#endif


#ifdef ZK_1621
    system("cp ./buggy/FileTxnLog.java ./zookeeper-3.4.3-src/src/java/main/org/apache/zookeeper/server/persistence/");
    printf("inject ZK_1621 ...\n");
#else
    system("cp ./fixed/FileTxnLog.java ./zookeeper-3.4.3-src/src/java/main/org/apache/zookeeper/server/persistence/");
    printf("don't inject ZK_1621 ...\n");
#endif
}

