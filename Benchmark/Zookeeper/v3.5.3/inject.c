#include <stdio.h>
#include <stdlib.h>
int main()
{
#ifdef ZK_2355
    system("cp ./buggy/Learner.java ./zookeeper-3.5.3-src/src/java/main/org/apache/zookeeper/server/quorum/");
    printf("inject ZK_2355 ...\n");
#else
    system("cp ./fixed/Learner.java ./zookeeper-3.5.3-src/src/java/main/org/apache/zookeeper/server/quorum/");
    printf("don't inject ZK_2355 ...\n");
#endif


#ifdef ZK_3531
    system("cp ./buggy/ReferenceCountedACLCache.java ./zookeeper-3.5.3-src/src/java/main/org/apache/zookeeper/server/");
    printf("inject ZK_3531 ...\n");
#else
    system("cp ./fixed/ReferenceCountedACLCache.java ./zookeeper-3.5.3-src/src/java/main/org/apache/zookeeper/server/");
    printf("don't inject ZK_3531 ...\n");
#endif
}

