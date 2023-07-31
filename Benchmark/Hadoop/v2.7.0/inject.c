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
}
