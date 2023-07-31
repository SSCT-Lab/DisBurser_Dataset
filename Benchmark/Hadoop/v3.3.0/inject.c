#include <stdio.h>
#include <stdlib.h>
int main()
{
#ifdef HDFS_15398
    system("cp ./buggy/DFSStripedOutputStream.java ./hadoop-3.3.0-src/hadoop-hdfs-project/hadoop-hdfs-client/src/main/java/org/apache/hadoop/hdfs/");
    printf("inject HDFS_15398 ...\n");
#else
    system("cp ./fixed/DFSStripedOutputStream.java ./hadoop-3.3.0-src/hadoop-hdfs-project/hadoop-hdfs-client/src/main/java/org/apache/hadoop/hdfs/");
    printf("don't inject HDFS_15398 ...\n");
#endif
}
