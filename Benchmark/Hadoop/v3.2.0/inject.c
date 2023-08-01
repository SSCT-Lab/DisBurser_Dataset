#include <stdio.h>
#include <stdlib.h>
int main()
{
#ifdef HDFS_14527
    system("cp ./buggy/BlockPlacementPolicyDefault.java ./hadoop-3.2.0-src/hadoop-hdfs-project/hadoop-hdfs/src/main/java/org/apache/hadoop/hdfs/server/blockmanagement/");
    system("cp ./buggy/BlockPlacementPolicyRackFaultTolerant.java ./hadoop-3.2.0-src/hadoop-hdfs-project/hadoop-hdfs/src/main/java/org/apache/hadoop/hdfs/server/blockmanagement/");
    printf("inject HDFS_14527 ...\n");
#else
    system("cp ./fixed/BlockPlacementPolicyDefault.java ./hadoop-3.2.0-src/hadoop-hdfs-project/hadoop-hdfs/src/main/java/org/apache/hadoop/hdfs/server/blockmanagement/");
    system("cp ./fixed/BlockPlacementPolicyRackFaultTolerant.java ./hadoop-3.2.0-src/hadoop-hdfs-project/hadoop-hdfs/src/main/java/org/apache/hadoop/hdfs/server/blockmanagement/");
    printf("don't inject HDFS_14527 ...\n");
#endif


#ifdef HDFS_14557
    system("cp ./buggy/EditLogFileInputStream.java ./hadoop-3.2.0-src/hadoop-hdfs-project/hadoop-hdfs/src/main/java/org/apache/hadoop/hdfs/server/namenode/");
    system("cp ./buggy/FSEditLogLoader.java ./hadoop-3.2.0-src/hadoop-hdfs-project/hadoop-hdfs/src/main/java/org/apache/hadoop/hdfs/server/namenode/");
    printf("inject HDFS_14557 ...\n");
#else
    system("cp ./fixed/EditLogFileInputStream.java ./hadoop-3.2.0-src/hadoop-hdfs-project/hadoop-hdfs/src/main/java/org/apache/hadoop/hdfs/server/namenode/");
    system("cp ./fixed/FSEditLogLoader.java ./hadoop-3.2.0-src/hadoop-hdfs-project/hadoop-hdfs/src/main/java/org/apache/hadoop/hdfs/server/namenode/");
    printf("don't inject HDFS_14557 ...\n");
#endif


#ifdef HDFS_14946
    system("cp ./buggy/BlockManager.java ./hadoop-3.2.0-src/hadoop-hdfs-project/hadoop-hdfs/src/main/java/org/apache/hadoop/hdfs/server/blockmanagement/");
    printf("inject HDFS_14946 ...\n");
#else
    system("cp ./fixed/BlockManager.java ./hadoop-3.2.0-src/hadoop-hdfs-project/hadoop-hdfs/src/main/java/org/apache/hadoop/hdfs/server/blockmanagement/");
    printf("don't inject HDFS_14946 ...\n");
#endif
}
