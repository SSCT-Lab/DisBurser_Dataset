#include <stdio.h>
#include <stdlib.h>
int main()
{
#ifdef HDFS_11379
    system("cp ./buggy/DFSInputStream.java ./hadoop-2.7.0-src/hadoop-hdfs-project/hadoop-hdfs/src/main/java/org/apache/hadoop/hdfs/");
    printf("inject HDFS_11379 ...\n");
#else
    system("cp ./fixed/DFSInputStream.java ./hadoop-2.7.0-src/hadoop-hdfs-project/hadoop-hdfs/src/main/java/org/apache/hadoop/hdfs/");
    printf("don't inject HDFS_11379 ...\n");
#endif


#ifdef HDFS_10987
    system("cp ./buggy/DecommissionManager.java ./hadoop-2.7.0-src/hadoop-hdfs-project/hadoop-hdfs/src/main/java/org/apache/hadoop/hdfs/server/blockmanagement/");
    printf("inject HDFS_10987 ...\n");
#else
    system("cp ./fixed/DecommissionManager.java ./hadoop-2.7.0-src/hadoop-hdfs-project/hadoop-hdfs/src/main/java/org/apache/hadoop/hdfs/server/blockmanagement/");
    printf("don't inject HDFS_10987 ...\n");
#endif


#ifdef HDFS_11609
    system("cp ./buggy/BlockManager.java ./hadoop-2.7.0-src/hadoop-hdfs-project/hadoop-hdfs/src/main/java/org/apache/hadoop/hdfs/server/blockmanagement/");
    system("cp ./buggy/UnderReplicatedBlocks.java ./hadoop-2.7.0-src/hadoop-hdfs-project/hadoop-hdfs/src/main/java/org/apache/hadoop/hdfs/server/blockmanagement/");
    printf("inject HDFS_11609 ...\n");
#else
    system("cp ./fixed/BlockManager.java ./hadoop-2.7.0-src/hadoop-hdfs-project/hadoop-hdfs/src/main/java/org/apache/hadoop/hdfs/server/blockmanagement/");
    system("cp ./fixed/UnderReplicatedBlocks.java ./hadoop-2.7.0-src/hadoop-hdfs-project/hadoop-hdfs/src/main/java/org/apache/hadoop/hdfs/server/blockmanagement/");
    printf("don't inject HDFS_11609 ...\n");
#endif
}
