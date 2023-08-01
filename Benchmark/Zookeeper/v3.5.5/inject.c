#include <stdio.h>
#include <stdlib.h>
int main()
{
    FILE *fp1;
    FILE *fp2;
    int i = 0;
    char str[500];
    char str1[200]    = 	"        self.start_fle = Time.currentElapsedTime();\n";

#ifdef ZK_3479
    system("cp ./buggy/FastLeaderElection.java ./zookeeper-3.5.5-src/zookeeper-server/src/main/java/org/apache/zookeeper/server/quorum/");
    system("cp ./buggy/QuorumPeer.java ./zookeeper-3.5.5-src/zookeeper-server/src/main/java/org/apache/zookeeper/server/quorum/");
    printf("inject ZK_3479 ...\n");
#else
    // File 1: FastLeaderElection.java
    fp1 = fopen("./buggy/FastLeaderElection.java", "r");
    if (fp1 == NULL)
    {
        perror("open file error");
        return -1;
    }
    fp2 = fopen("./fixed/FastLeaderElection.java", "w+");
    if (fp2 == NULL)
    {
        perror("open file error");
        return -1;
    }
    while (fgets(str, 500, fp1) != NULL)
    {
        if (i == 869)
        {
            fputs(str1, fp2);
        }
        else if (i == 870 || i == 871)
        {
        }
        else
        {
            fputs(str, fp2);
        }
        i++;
    }
    i = 0;
    fclose(fp1);
    fclose(fp2);
    system("cp ./fixed/FastLeaderElection.java ./zookeeper-3.5.5-src/zookeeper-server/src/main/java/org/apache/zookeeper/server/quorum/");

    // File 2: QuorumPeer.java
    fp1 = fopen("./buggy/QuorumPeer.java", "r");
    if (fp1 == NULL)
    {
        perror("open file error");
        return -1;
    }
    fp2 = fopen("./fixed/QuorumPeer.java", "w+");
    if (fp2 == NULL)
    {
        perror("open file error");
        return -1;
    }
    while (fgets(str, 500, fp1) != NULL)
    {
        if (i == 1296)
        {
        }
        else
        {
            fputs(str, fp2);
        }
        i++;
    }
    i = 0;
    fclose(fp1);
    fclose(fp2);
    system("cp ./fixed/QuorumPeer.java ./zookeeper-3.5.5-src/zookeeper-server/src/main/java/org/apache/zookeeper/server/quorum/");
    printf("don't inject ZK_3479 ...\n");
#endif
}
